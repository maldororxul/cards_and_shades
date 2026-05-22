package com.example.cardsandshades.ui.booster

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    
    // Стейт для полноэкранной анимации редкой карты
    var highlightedCard by remember { mutableStateOf<CardModel?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GameText(text = message, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

        Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
            if (openedCards.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(openedCards) { card ->
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(card.id) {
                            isVisible = true
                        }
                        Box {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isVisible,
                                enter = scaleIn(initialScale = 0.5f, animationSpec = tween(400)) + fadeIn()
                            ) {
                                CardComponent(card = card)
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
                            
                            // Проверка на легендарку/эпик для анимации
                            val best = newCards.filter { it.rarity == Rarity.LEGENDARY || it.rarity == Rarity.EPIC }
                                .maxByOrNull { if (it.rarity == Rarity.LEGENDARY) 2 else 1 }
                            
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
        val color = if (highlightedCard!!.rarity == Rarity.LEGENDARY) Color(0xFFFDD835) else Color(0xFF8E24AA)
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { highlightedCard = null }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GameText(
                    text = if (highlightedCard!!.rarity == Rarity.LEGENDARY) "🌟 LEGENDARY! 🌟" else "✨ EPIC! ✨",
                    color = color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                CardComponent(card = highlightedCard!!, modifier = Modifier.size(200.dp, 300.dp))
            }
        }
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
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                recipe.inputs.forEach { input ->
                    val hasCount = userCards.count { it.name == input.key }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                        CardComponent(
                            card = CardCatalog.createCardInstance(input.key)!!, 
                            isPreview = true, 
                            modifier = Modifier.size(60.dp, 90.dp),
                            onClick = { inspectedCard = CardCatalog.createCardInstance(input.key) }
                        )
                        GameText("${hasCount}/${input.count}", fontSize = 10.sp, color = if (hasCount >= input.count) Color.Green else Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GameText(stringResource(R.string.fusion_result), fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CardComponent(
                    card = CardCatalog.createCardInstance(recipe.outputKey)!!, 
                    isPreview = true, 
                    modifier = Modifier.size(100.dp, 150.dp),
                    onClick = { inspectedCard = CardCatalog.createCardInstance(recipe.outputKey) }
                )
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
                    val newCard = CardCatalog.createCardInstance(recipe.outputKey)!!
                    UserProfile.collection.add(newCard)
                    UserProfile.save()
                    com.example.cardsandshades.sound.SoundManager.playSoundByName(context, "victory")
                },
                enabled = canFuse,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (inspectedCard != null) {
        CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
    }
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
        val roll = (1..100).random()
        val rarity = when {
            roll <= booster.chances.legendary -> Rarity.LEGENDARY
            roll <= booster.chances.legendary + booster.chances.epic -> Rarity.EPIC
            roll <= booster.chances.legendary + booster.chances.epic + booster.chances.rare -> Rarity.RARE
            else -> Rarity.COMMON
        }
        val card = CardCatalog.generateRandomCardByRarityOnly(rarity) ?: CardCatalog.generateRandomCardByRarityOnly(Rarity.COMMON)!!
        pack.add(card)
    }
    return pack
}
