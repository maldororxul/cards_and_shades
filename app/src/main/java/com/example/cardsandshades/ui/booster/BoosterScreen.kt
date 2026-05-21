package com.example.cardsandshades.ui.booster

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.catalog.BoosterCatalog
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.model.BoosterModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText

@Composable
fun BoosterScreen(
    modifier: Modifier = Modifier
) {
    val gold by UserProfile.gold.collectAsState()
    val crystals by UserProfile.crystals.collectAsState()
    var openedCards by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var message by remember { mutableStateOf("Выберите набор карт!") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Хедер
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                GameText("Золото: $gold 🪙", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                GameText("Кристаллы: $crystals 💎", color = Color(0xFF03A9F4), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Зона карт
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameText(text = message, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

            if (openedCards.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(openedCards) { card ->
                        CardComponent(card = card)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(280.dp)
                        .border(2.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    GameText("📦 Выберите пак ниже", color = Color.Gray)
                }
            }
        }

        // ВЫБОР ПАКОВ (из YAML)
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BoosterCatalog.boosters.forEach { booster ->
                BoosterItem(
                    booster = booster,
                    canAfford = if (booster.costType == "gold") gold >= booster.costAmount else crystals >= booster.costAmount,
                    onBuy = {
                        val success = buyBooster(booster)
                        if (success) {
                            openedCards = generatePack(booster)
                            UserProfile.collection.addAll(openedCards)
                            UserProfile.save()
                            message = "Пак [${booster.name}] открыт!"
                        } else {
                            message = "Недостаточно ресурсов!"
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun BoosterItem(booster: BoosterModel, canAfford: Boolean, onBuy: () -> Unit) {
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
                GameText(text = booster.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                GameText(text = booster.description, fontSize = 11.sp, color = Color.Gray)
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
