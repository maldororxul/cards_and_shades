package com.example.cardsandshades.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: Any,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    // Храним выбранную карту игрока на столе для совершения атаки
    var selectedCardForAttack by remember { mutableStateOf<CardModel?>(null) }

    gameState?.let { state ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ================= ВРАГ (ВЕРХНЯЯ ЧАСТЬ) =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.opponent.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "HP: ${state.opponent.currentHp}/${state.opponent.maxHp} ❤️",
                        color = Color.Red,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            // Если у игрока выбрана карта, атакуем "лицо" врага
                            selectedCardForAttack?.let { attacker ->
                                viewModel.attackEnemyHero(attacker)
                                selectedCardForAttack = null
                            }
                        }
                    )
                }
                Text(
                    text = "Карт в руке: ${state.opponent.hand.size} | Мана: ${state.opponent.currentMana}/${state.opponent.maxMana}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            // ================= ПОЛЕ БОЯ ВРАГА =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                if (state.opponent.board.isEmpty()) {
                    Text("Поле врага пусто", color = Color.Gray)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.opponent.board) { enemyCard ->
                            CardComponent(
                                card = enemyCard,
                                modifier = Modifier.padding(4.dp),
                                onClick = {
                                    // Если у нас выбрана карта для атаки — бьем эту карту врага
                                    selectedCardForAttack?.let { attacker ->
                                        viewModel.attackEnemyCard(attacker, enemyCard)
                                        selectedCardForAttack = null
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ================= ПОЛЕ БОЯ ИГРОКА =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                if (state.player.board.isEmpty()) {
                    Text("Ваше поле пусто", color = Color.Gray)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.player.board) { playerCard ->
                            val isSelected = selectedCardForAttack == playerCard
                            CardComponent(
                                card = playerCard,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.Green else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                onClick = {
                                    // Выбираем (или отменяем выбор) карту для совершения атаки
                                    selectedCardForAttack = if (isSelected) null else playerCard
                                }
                            )
                        }
                    }
                }
            }

            // ================= ИГРОК (НИЖНЯЯ ЧАСТЬ) =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = state.player.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Мана: ${state.player.currentMana}/${state.player.maxMana} 💧",
                            color = Color(0xFF0288D1),
                            fontSize = 14.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HP: ${state.player.currentHp}/${state.player.maxHp} ❤️",
                            color = Color.Green,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.endTurn()
                                selectedCardForAttack = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19))
                        ) {
                            Text("Ход")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // РУКА ИГРОКА
                Text("Ваша рука:", color = Color.Gray, fontSize = 12.sp)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    items(state.player.hand) { card ->
                        CardComponent(
                            card = card,
                            modifier = Modifier.padding(4.dp),
                            onClick = { viewModel.playCard(card) }
                        )
                    }
                }
            }
        }

        // Экран завершения игры поверх основного UI
        if (state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Игра Окончена!",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Победитель: ${state.winnerName}",
                        color = Color.Yellow,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        // За победу начисляем 50 золота
                        if (state.winnerName == state.player.name) {
                            UserProfile.gold.value += 50
                        }
                        // Здесь мы прокинем лямбду для выхода в главное меню
                    }) { Text("В меню кампании") }
                    Button(onClick = { viewModel.startNewGame(
                        level = TODO()
                    ) }) {
                        Text("Играть снова")
                    }
                }
            }
        }
    }
}