package com.example.cardsandshades.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText

@Composable
fun CollectionScreen(
    modifier: Modifier = Modifier
) {
    val userCards by UserProfile.collection.status.collectAsState()

    // Временный список для сборки колоды (максимум 20 карт)
    val currentDeck = remember { mutableStateListOf<CardModel>().apply { addAll(UserProfile.selectedDeck) } }
    var errorMessage by remember { mutableStateOf("Соберите колоду: ${currentDeck.size}/20 карт") }
    var inspectedCard by remember { mutableStateOf<CardModel?>(null) }

    val dustC by UserProfile.dustCommon.collectAsState()
    val dustR by UserProfile.dustRare.collectAsState()
    val dustE by UserProfile.dustEpic.collectAsState()
    val dustL by UserProfile.dustLegendary.collectAsState()

    // Группируем карты в коллекции по имени, чтобы показывать количество дубликатов
    val groupedCards = remember(userCards) {
        userCards.groupBy { it.name }.map { group -> group.value.first() to group.value.size }
    }
    
    val totalExtras = remember(userCards) {
        userCards.groupBy { it.name }.values.sumOf { if (it.size > 2) it.size - 2 else 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Хедер экрана
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameButton(
                    text = "Сохранить",
                    onClick = {
                        if (currentDeck.size == 20) {
                            UserProfile.selectedDeck.clear()
                            UserProfile.selectedDeck.addAll(currentDeck)
                            UserProfile.selectedDeck.notifyChanges()
                            UserProfile.save()
                        } else {
                            errorMessage = "❌ Нужно 20 карт!"
                        }
                    },
                    containerColor = if (currentDeck.size == 20) Color(0xFF388E3C) else Color.Gray
                )

                GameText(
                    text = errorMessage,
                    color = if (errorMessage.contains("❌")) Color.Red else Color.Yellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ПАНЕЛЬ ПОРОШКА
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DustInfo(Color.Gray, dustC)
                    DustInfo(Color(0xFF1E88E5), dustR)
                    DustInfo(Color(0xFF8E24AA), dustE)
                    DustInfo(Color(0xFFFDD835), dustL)
                }
                
                if (totalExtras > 0) {
                    GameButton(
                        text = "Распылить ($totalExtras)",
                        onClick = { UserProfile.dustExtras() },
                        containerColor = Color(0xFF5D4037),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Грид-сетка всех карт в коллекции игрока
        if (groupedCards.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                GameText("Ваша коллекция пуста! 📦", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groupedCards) { (cardSample, count) ->
                    val countInDeck = currentDeck.count { it.name == cardSample.name }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomCenter) {
                            CardComponent(
                                card = cardSample,
                                isPreview = true,
                                modifier = Modifier
                                    .graphicsLayer { alpha = if (countInDeck >= 2) 0.4f else 1f },
                                onClick = {
                                    inspectedCard = cardSample
                                }
                            )

                            // Четкий ККИ-счетчик поверх карты
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(if (countInDeck >= 2) Color(0xFF388E3C) else Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                GameText(
                                    text = "$countInDeck / 2",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        GameText("Owned: $count", fontSize = 10.sp, color = Color.Gray)

                        // ПОНЯТНЫЕ КНОПКИ УПРАВЛЕНИЯ ПОД КАРТОЙ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Кнопка МИНУС (Удалить из колоды)
                            if (countInDeck > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFFF5252), RoundedCornerShape(14.dp))
                                        .clickable {
                                            val cardToRemove = currentDeck.find { it.name == cardSample.name }
                                            if (cardToRemove != null) {
                                                currentDeck.remove(cardToRemove)
                                                errorMessage = "Соберите колоду: ${currentDeck.size}/20 карт"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    GameText("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Кнопка ПЛЮС (Добавить в колоду)
                            if (countInDeck < 2 && currentDeck.size < 20 && count > countInDeck) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(14.dp))
                                        .clickable {
                                            currentDeck.add(cardSample.copy(id = java.util.UUID.randomUUID().toString()))
                                            errorMessage = "Соберите колоду: ${currentDeck.size}/20 карт"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    GameText("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }

    if (inspectedCard != null) {
        CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
    }
}

@Composable
private fun DustInfo(color: Color, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        GameText(text = amount.toString(), fontSize = 12.sp, color = color)
    }
}
