package com.example.cardsandshades.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent

@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userCards by UserProfile.collection.status.collectAsState()

    // Временный список для сборки колоды (максимум 20 карт)
    val currentDeck = remember { mutableStateListOf<CardModel>().apply { addAll(UserProfile.selectedDeck) } }
    var errorMessage by remember { mutableStateOf("Соберите колоду: ${currentDeck.size}/20 карт") }

    // Группируем карты в коллекции по имени, чтобы показывать количество дубликатов
    val groupedCards = remember(userCards) {
        userCards.groupBy { it.name }.map { group -> group.value.first() to group.value.size }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Хедер экрана
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (currentDeck.size == 20) {
                        // Сохраняем собранную колоду в профиль игрока
                        UserProfile.selectedDeck.clear()
                        UserProfile.selectedDeck.addAll(currentDeck)
                        onBack()
                    } else {
                        errorMessage = "❌ Нельзя выйти! В колоде должно быть ровно 20 карт."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (currentDeck.size == 20) Color(0xFF388E3C) else Color.Gray)
            ) {
                Text("Сохранить и Выйти")
            }

            Text(
                text = errorMessage,
                color = if (errorMessage.contains("❌")) Color.Red else Color.Yellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Грид-сетка всех карт в коллекции игрока
        if (groupedCards.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Ваша коллекция пуста. Зайдите в магазин и откройте бустеры! 📦", color = Color.Gray, textAlign = TextAlign.Center)
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

                    // Флаг: можно ли еще добавить эту карту в колоду
                    val canAddMore = countInDeck < 2 && currentDeck.size < 20

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
                                // Меняем прозрачность всей карты, если лимит в колоде уже достигнут
                                modifier = Modifier
                                    .graphicsLayer { alpha = if (countInDeck >= 2) 0.4f else 1f }
                                    .clickable {
                                        if (currentDeck.size >= 20) {
                                            errorMessage = "❌ Максимум 20 карт в колоде!"
                                        } else if (countInDeck >= 2) {
                                            errorMessage = "❌ Достигнут лимит: максимум 2 копии одной карты!"
                                        } else {
                                            currentDeck.add(cardSample.copy(id = java.util.UUID.randomUUID().toString()))
                                            errorMessage = "Соберите колоду: ${currentDeck.size}/20 карт"
                                        }
                                    }
                            )

                            // Четкий ККИ-счетчик поверх карты
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(if (countInDeck >= 2) Color(0xFF388E3C) else Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "В колоде: $countInDeck / 2",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Кнопка ПЛЮС (Добавить в колоду)
                            if (countInDeck < 2) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(if (currentDeck.size < 20) Color(0xFF4CAF50) else Color.Gray, RoundedCornerShape(14.dp))
                                        .clickable {
                                            if (currentDeck.size < 20) {
                                                currentDeck.add(cardSample.copy(id = java.util.UUID.randomUUID().toString()))
                                                errorMessage = "Соберите колоду: ${currentDeck.size}/20 карт"
                                            } else {
                                                errorMessage = "❌ Максимум 20 карт в колоде!"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}