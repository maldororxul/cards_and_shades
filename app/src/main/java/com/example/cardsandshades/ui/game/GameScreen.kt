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
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.DragTarget

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    var selectedCardForAttack by remember { mutableStateOf<CardModel?>(null) }

    if (gameState == null) {
        // Заглушка на случай, если экран пытается отрисоваться без данных
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Загрузка боя...", color = Color.White)
        }
    } else {
        val state = gameState!!
        // Оборачиваем весь экран в глобальный контейнер Drag & Drop
        DragAndDropContainer(modifier = modifier) {
            Column(
                modifier = Modifier
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
                        Text(state.opponent.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "HP: ${state.opponent.currentHp}/${state.opponent.maxHp} ❤️",
                            color = Color.Red,
                            fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                selectedCardForAttack?.let { attacker ->
                                    viewModel.attackEnemyHero(attacker)
                                    selectedCardForAttack = null
                                }
                            }
                        )
                    }
                    Text("Карт в руке: ${state.opponent.hand.size} | Мана: ${state.opponent.currentMana}/${state.opponent.maxMana}", color = Color.Gray, fontSize = 12.sp)
                }

                // ================= ПОЛЕ БОЯ ВРАГА =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.opponent.board.isEmpty()) {
                        Text("Поле врага пусто", color = Color.Gray)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            items(state.opponent.board) { enemyCard ->
                                CardComponent(
                                    card = enemyCard,
                                    modifier = Modifier.padding(4.dp),
                                    onClick = {
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

                // ================= ИСПРАВЛЕНИЕ: ПОЛЕ БОЯ ИГРОКА (DROP TARGET) =================
                DropTarget(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    onCardDropped = { droppedCard ->
                        // Когда карту отпускают над столом, вызываем метод её розыгрыша
                        viewModel.playCard(droppedCard)
                    }
                ) { isHovered ->
                    val boardBorderColor = if (isHovered) Color.Green else Color.DarkGray
                    val boardBgColor = if (isHovered) Color(0xFF1B3A1B) else Color(0xFF1E1E1E)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, boardBorderColor, RoundedCornerShape(8.dp))
                            .background(boardBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.player.board.isEmpty()) {
                            Text("Перетащите карту сюда, чтобы разыграть", color = Color.Gray)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
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
                                            selectedCardForAttack = if (isSelected) null else playerCard
                                        }
                                    )
                                }
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
                            Text(state.player.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Мана: ${state.player.currentMana}/${state.player.maxMana} 💧", color = Color(0xFF0288D1), fontSize = 14.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("HP: ${state.player.currentHp}/${state.player.maxHp} ❤️", color = Color.Green, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp))
                            Button(onClick = { viewModel.endTurn(); selectedCardForAttack = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19))) {
                                Text("Ход")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // РУКА ИГРОКА (ДРАГ-ТАГЕТЫ)
                    Text("Ваша рука (Зажмите карту для перетаскивания, долгий тап - осмотр):", color = Color.Gray, fontSize = 11.sp)
                    LazyRow(modifier = Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.Start) {
                        items(state.player.hand, key = { it.id }) { card ->
                            DragTarget(card = card, modifier = Modifier.padding(4.dp)) {
                                CardComponent(card = card)
                            }
                        }
                    }
                }
            }

            // Оверлей конца игры
            if (state.isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Игра Окончена!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Победитель: ${state.winnerName}", color = Color.Yellow, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.restartCurrentGame() }) { Text("Играть снова") }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (state.winnerName == state.player.name) {
                                    com.example.cardsandshades.model.UserProfile.gold.value += 50
                                }
                                onBackToMenu()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) { Text("В меню кампании") }
                    }
                }
            }
        }
    }
}