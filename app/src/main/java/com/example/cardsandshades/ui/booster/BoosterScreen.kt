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
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.UserProfile
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
            .background(Color(0xFF121212))
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
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    GameText("📦 Выберите пак ниже", color = Color.Gray)
                }
            }
        }

        // Выбор паков
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // ОБЫЧНЫЙ (100 золота)
            GameButton(
                text = "Обычный пак (100 🪙)",
                onClick = {
                    if (gold >= 100) {
                        UserProfile.gold.value -= 100
                        openedCards = CardCatalog.openBooster()
                        UserProfile.collection.addAll(openedCards)
                        UserProfile.collection.notifyChanges()
                        UserProfile.save()
                        message = "Получено 5 карт!"
                    } else message = "Недостаточно золота!"
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // ПРЕМИУМ (250 золота - выше шанс на Epic)
            GameButton(
                text = "Премиум пак (250 🪙)",
                onClick = {
                    if (gold >= 250) {
                        UserProfile.gold.value -= 250
                        val pack = mutableListOf<CardModel>()
                        repeat(3) { pack.add(generateByRarity(listOf(Rarity.COMMON, Rarity.RARE))) }
                        pack.add(generateByRarity(listOf(Rarity.RARE, Rarity.EPIC)))
                        pack.add(generateByRarity(listOf(Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY)))
                        
                        openedCards = pack
                        UserProfile.collection.addAll(openedCards)
                        UserProfile.collection.notifyChanges()
                        UserProfile.save()
                        message = "Редкие карты получены!"
                    } else message = "Недостаточно золота!"
                },
                containerColor = Color(0xFF673AB7),
                modifier = Modifier.fillMaxWidth()
            )
            
            // КРИСТАЛЛЬНЫЙ (50 кристаллов - Гарант Epic/Leg)
            GameButton(
                text = "Кристальный пак (50 💎)",
                onClick = {
                    if (crystals >= 50) {
                        UserProfile.crystals.value -= 50
                        val pack = mutableListOf<CardModel>()
                        repeat(2) { pack.add(generateByRarity(listOf(Rarity.RARE, Rarity.EPIC))) }
                        pack.add(generateByRarity(listOf(Rarity.EPIC)))
                        pack.add(generateByRarity(listOf(Rarity.EPIC, Rarity.LEGENDARY)))
                        pack.add(generateByRarity(listOf(Rarity.LEGENDARY)))
                        
                        openedCards = pack
                        UserProfile.collection.addAll(openedCards)
                        UserProfile.collection.notifyChanges()
                        UserProfile.save()
                        message = "Легендарные силы ваши!"
                    } else message = "Недостаточно кристаллов!"
                },
                containerColor = Color(0xFF0288D1),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }
    }
}

private fun generateByRarity(allowed: List<Rarity>): CardModel {
    val rarity = allowed.random()
    return CardCatalog.generateRandomCardByRarityOnly(rarity) ?: CardCatalog.generateRandomCardByRarityOnly(Rarity.COMMON)!!
}
