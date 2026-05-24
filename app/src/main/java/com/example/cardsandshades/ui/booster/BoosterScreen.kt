package com.example.cardsandshades.ui.booster

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.catalog.BoosterCatalog
import com.example.cardsandshades.catalog.FusionCatalog
import com.example.cardsandshades.catalog.FusionRecipe
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.model.BoosterModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.utils.getStringResourceByName

@Composable
fun BoosterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gold by UserProfile.gold.collectAsState()
    val crystals by UserProfile.crystals.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.shop), stringResource(R.string.fusion_tree))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Хедер ресурсов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                GameText(stringResource(R.string.booster_gold, gold), color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                GameText(stringResource(R.string.booster_crystals, crystals), color = Color(0xFF03A9F4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF673AB7)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { GameText(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            PacksTab(gold, crystals, context)
        } else {
            FusionTab(context)
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun PacksTab(gold: Int, crystals: Int, context: android.content.Context) {
    var openedCards by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    val choosePackMsg = stringResource(R.string.booster_choose)
    var message by remember { mutableStateOf(choosePackMsg) }
    
    var highlightedCard by remember { mutableStateOf<CardModel?>(null) }
    var inspectedCardsList by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var initialInspectedIndex by remember { mutableIntStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GameText(text = message, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

        Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
            if (openedCards.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(openedCards) { index, card ->
                        Box {
                            val isVisible = remember { mutableStateOf(false) }
                            LaunchedEffect(card.id) {
                                isVisible.value = true
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isVisible.value,
                                enter = scaleIn(initialScale = 0.5f, animationSpec = tween(400)) + fadeIn()
                            ) {
                                CardComponent(
                                    card = card,
                                    onClick = {
                                        inspectedCardsList = openedCards
                                        initialInspectedIndex = index
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(200.dp)
                        .border(2.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(stringResource(R.string.booster_wait), color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val packOpenedMsg = stringResource(R.string.booster_opened)
            val noResourcesMsg = stringResource(R.string.booster_no_resources)
            
            BoosterCatalog.boosters.forEach { booster ->
                BoosterItem(
                    booster = booster,
                    canAfford = if (booster.costType == "gold") gold >= booster.costAmount else crystals >= booster.costAmount,
                    onBuy = {
                        val success = buyBooster(booster)
                        if (success) {
                            com.example.cardsandshades.sound.SoundManager.playSoundByName(context, "booster_open")
                            val newCards = generatePack(booster)
                            openedCards = newCards
                            UserProfile.collection.addAll(openedCards)
                            UserProfile.save()
                            message = packOpenedMsg.format(getStringResourceByName(context, booster.name))
                            
                            val best = newCards.filter { it.rarity == Rarity.MYTHIC || it.rarity == Rarity.LEGENDARY || it.rarity == Rarity.EPIC }
                                .maxByOrNull { 
                                    when(it.rarity) {
                                        Rarity.MYTHIC -> 3
                                        Rarity.LEGENDARY -> 2
                                        Rarity.EPIC -> 1
                                        else -> 0
                                    }
                                }
                            
                            if (best != null) {
                                highlightedCard = best
                            }
                        } else {
                            message = noResourcesMsg
                        }
                    }
                )
            }
        }
    }

    if (highlightedCard != null) {
        val rarityColor = when(highlightedCard!!.rarity) {
            Rarity.MYTHIC -> Color(0xFFFF3D00)
            Rarity.LEGENDARY -> Color(0xFFFDD835)
            Rarity.EPIC -> Color(0xFF8E24AA)
            else -> Color.White
        }
        
        val rarityText = when(highlightedCard!!.rarity) {
            Rarity.MYTHIC -> stringResource(R.string.mythic_alert)
            Rarity.LEGENDARY -> stringResource(R.string.legendary_alert)
            Rarity.EPIC -> stringResource(R.string.epic_alert)
            else -> ""
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { highlightedCard = null }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GameText(text = rarityText, color = rarityColor, fontSize = 36.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(24.dp))
                CardComponent(
                    card = highlightedCard!!, 
                    modifier = Modifier.size(240.dp, 360.dp),
                    onClick = {
                        inspectedCardsList = openedCards
                        initialInspectedIndex = openedCards.indexOfFirst { it.id == highlightedCard!!.id }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                GameText(stringResource(R.string.close), color = Color.Gray, fontSize = 14.sp)
            }
        }
    }

    if (inspectedCardsList.isNotEmpty()) {
        CardInspectionDialog(
            cards = inspectedCardsList,
            initialIndex = initialInspectedIndex,
            onDismiss = { inspectedCardsList = emptyList() }
        )
    }
}

@Composable
private fun FusionTab(context: android.content.Context) {
    val recipes = FusionCatalog.recipes

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        GameText(stringResource(R.string.fusion_desc), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        recipes.forEach { recipe ->
            FusionAccordion(recipe, context)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FusionAccordion(recipe: FusionRecipe, context: android.content.Context) {
    var expanded by remember { mutableStateOf(false) }
    var inspectedCard by remember { mutableStateOf<CardModel?>(null) }

    val userCards = UserProfile.collection
    val canFuse = recipe.inputs.all { input -> 
        userCards.count { it.name == input.key } >= input.count
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .border(1.dp, if (canFuse) Color(0xFF388E3C) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GameText(
                    text = getStringResourceByName(context, recipe.outputKey),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                GameText(
                    text = if (canFuse) stringResource(R.string.fusion_recipe_ready) else stringResource(R.string.fusion_recipe_missing),
                    fontSize = 11.sp,
                    color = if (canFuse) Color.Green else Color.Red
                )
            }
            GameText(if (expanded) "▲" else "▼", color = Color.Gray)
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            
            GameText(stringResource(R.string.fusion_cost), fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Start) {
                recipe.inputs.forEach { input ->
                    val hasCount = userCards.count { it.name == input.key }
                    val cardInstance = CardCatalog.createCardInstance(input.key)
                    if (cardInstance != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                            CardComponent(
                                card = cardInstance, 
                                isPreview = true, 
                                modifier = Modifier.size(60.dp, 90.dp),
                                onClick = { 
                                    // Для рецептов свайп не нужен, просто смотрим одну карту
                                }
                            )
                            GameText("${hasCount}/${input.count}", fontSize = 10.sp, color = if (hasCount >= input.count) Color.Green else Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GameText(stringResource(R.string.fusion_result), fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val outputInstance = CardCatalog.createCardInstance(recipe.outputKey)
                if (outputInstance != null) {
                    CardComponent(
                        card = outputInstance, 
                        isPreview = true, 
                        modifier = Modifier.size(100.dp, 150.dp),
                        onClick = { 
                            // Смотрим одну карту
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GameButton(
                text = stringResource(R.string.fusion_confirm),
                onClick = {
                    recipe.inputs.forEach { input ->
                        repeat(input.count) {
                            val card = UserProfile.collection.find { it.name == input.key }
                            if (card != null) UserProfile.collection.remove(card)
                        }
                    }
                    val newCard = CardCatalog.createCardInstance(recipe.outputKey)
                    if (newCard != null) {
                        UserProfile.collection.add(newCard)
                        UserProfile.save()
                        com.example.cardsandshades.sound.SoundManager.playSoundByName(context, "victory")
                    }
                },
                enabled = canFuse,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // В FusionTab используем одиночный CardInspectionDialog (со списком из 1 карты)
}

@Composable
private fun BoosterItem(booster: BoosterModel, canAfford: Boolean, onBuy: () -> Unit) {
    val context = LocalContext.current
    val color = if (booster.costType == "gold") Color(0xFFFDD835) else Color(0xFF03A9F4)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(1.dp, if (canAfford) color.copy(alpha = 0.5f) else Color.DarkGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                GameText(text = getStringResourceByName(context, booster.name), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                GameText(text = getStringResourceByName(context, booster.description), fontSize = 11.sp, color = Color.Gray)
            }
            GameButton(
                text = "${booster.costAmount} ${if (booster.costType == "gold") "🪙" else "💎"}",
                onClick = onBuy,
                containerColor = if (canAfford) color else Color.Gray,
                contentColor = if (booster.costType == "gold") Color.Black else Color.White,
                enabled = canAfford,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // ШАНСЫ ВЫПАДЕНИЯ
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ChanceInfo("C", Color.Gray, booster.chances.common)
            ChanceInfo("R", Color(0xFF1E88E5), booster.chances.rare)
            ChanceInfo("E", Color(0xFF8E24AA), booster.chances.epic)
            ChanceInfo("L", Color(0xFFFDD835), booster.chances.legendary)
            ChanceInfo("M", Color(0xFFFF3D00), 1) // Mythic chance
        }
    }
}

@Composable
private fun ChanceInfo(label: String, color: Color, chance: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GameText(text = "$label:", fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(2.dp))
        GameText(text = "$chance%", fontSize = 10.sp, color = Color.LightGray)
    }
}

private fun buyBooster(booster: BoosterModel): Boolean {
    if (booster.costType == "gold") {
        if (UserProfile.gold.value >= booster.costAmount) {
            UserProfile.gold.value -= booster.costAmount
            return true
        }
    } else {
        if (UserProfile.crystals.value >= booster.costAmount) {
            UserProfile.crystals.value -= booster.costAmount
            return true
        }
    }
    return false
}

private fun generatePack(booster: BoosterModel): List<CardModel> {
    val pack = mutableListOf<CardModel>()
    repeat(5) {
        val roll = (1..1000).random() // Higher resolution for Mythic
        val rarity = when {
            roll <= 10 -> Rarity.MYTHIC // 1%
            roll <= booster.chances.legendary * 10 -> Rarity.LEGENDARY
            roll <= (booster.chances.legendary + booster.chances.epic) * 10 -> Rarity.EPIC
            roll <= (booster.chances.legendary + booster.chances.epic + booster.chances.rare) * 10 -> Rarity.RARE
            else -> Rarity.COMMON
        }
        val card = CardCatalog.generateRandomCardByRarityOnly(rarity) ?: CardCatalog.generateRandomCardByRarityOnly(Rarity.COMMON)!!
        pack.add(card)
    }
    return pack
}
