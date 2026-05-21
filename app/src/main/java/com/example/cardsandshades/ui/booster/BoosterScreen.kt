package com.example.cardsandshades.ui.booster

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent

@Composable
fun BoosterScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gold by UserProfile.gold.collectAsState()
    var openedCards by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var message by remember { mutableStateOf("Купите пак за 100 🪙") }

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) { Text("Назад") }
            Text("Баланс: $gold 🪙", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // Зона карт
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

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
                    Text("📦 Нажмите Купить", color = Color.Gray)
                }
            }
        }

        // Кнопка покупки
        Button(
            onClick = {
                if (gold >= 100) {
                    UserProfile.gold.value -= 100
                    val newCards = CardCatalog.openBooster()
                    openedCards = newCards

                    // Добавляем карты в общую коллекцию игрока
                    UserProfile.collection.addAll(newCards)
                    UserProfile.collection.notifyChanges()
                    UserProfile.save()

                    message = "Вы получили 5 карт!"
                } else {
                    message = "Недостаточно золота! ❌"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Text("Купить Бустер (100 🪙)", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}