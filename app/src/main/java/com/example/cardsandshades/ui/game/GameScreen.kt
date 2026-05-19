package com.example.cardsandshades.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Turn
import com.example.cardsandshades.ui.components.AttackArrow
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.DragAndDropContainer
import com.example.cardsandshades.ui.components.DragTarget
import com.example.cardsandshades.ui.components.DropTarget

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    var selectedCardForAttack by remember { mutableStateOf<CardModel?>(null) }

    // Координаты для статической стрелки между выбранными картами
    var startArrowOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var currentArrowOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var isDrawingArrow by remember { mutableStateOf(false) }

    // Хранилище координат всех карт противника на столе для наведения стрелки
    val enemyCardsOffsets = remember { mutableStateMapOf<String, androidx.compose.ui.geometry.Offset>() }
    var enemyHeroOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Текстовый лог для информирования игрока
    var battleLog by remember { mutableStateOf("Ваш ход. Разыграйте карты или атакуйте врага!") }

    if (gameState == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Загрузка боя...", color = Color.White)
        }
    } else {
        val state = gameState!!

        LaunchedEffect(state.currentTurn) {
            battleLog = if (state.currentTurn == Turn.PLAYER) {
                "Ваш ход! Мана обновлена."
            } else {
                "Ход соперника..."
            }
        }

        DragAndDropContainer(modifier = modifier) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141414))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ================= ВРАГ (ВЕРХНЯЯ ЧАСТЬ) =================
                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1414), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(state.opponent.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "HP: ${state.opponent.currentHp}/${state.opponent.maxHp} ❤️",
                                color = Color(0xFFFF5252),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        val pos = coords.positionInWindow()
                                        enemyHeroOffset = androidx.compose.ui.geometry.Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2)
                                    }
                                    .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clickable {
                                        selectedCardForAttack?.let { attacker ->
                                            if (state.opponent.board.isEmpty()) {
                                                viewModel.attackEnemyHero(attacker)
                                                battleLog = "💥 Вы атаковали героя врага на ${attacker.currentAttack} урона!"
                                                selectedCardForAttack = null
                                                isDrawingArrow = false
                                            } else {
                                                battleLog = "❌ Нельзя атаковать лицо, пока у врага есть существа!"
                                            }
                                        }
                                    }
                            )
                        }
                        Text("Карт в руке: ${state.opponent.hand.size} | Мана врага: ${state.opponent.currentMana}/${state.opponent.maxMana}", color = Color.Gray, fontSize = 12.sp)
                    }

                    // ================= ПОЛЕ БОЯ ВРАГА =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(1.dp, Color(0xFF3A2323), RoundedCornerShape(8.dp))
                            .background(Color(0xFF1F1818)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.opponent.board.isEmpty()) {
                            Text("Поле противника пусто. Его Лицо открыто для атак!", color = Color.DarkGray, fontSize = 12.sp)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                items(state.opponent.board, key = { "opp_${it.id}" }) { enemyCard ->
                                    CardComponent(
                                        card = enemyCard,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .onGloballyPositioned { coords ->
                                                val pos = coords.positionInWindow()
                                                enemyCardsOffsets[enemyCard.id] = androidx.compose.ui.geometry.Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2)
                                            },
                                        onClick = {
                                            selectedCardForAttack?.let { attacker ->
                                                viewModel.attackEnemyCard(attacker, enemyCard)
                                                battleLog = "⚔️ ${attacker.name} атаковал ${enemyCard.name}!"
                                                selectedCardForAttack = null
                                                isDrawingArrow = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ================= ЛОГ =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222), RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = battleLog,
                            color = if (battleLog.contains("❌")) Color.Red else Color.Yellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // ================= ПОЛЕ БОЯ ИГРОКА (DROP TARGET) =================
                    DropTarget(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        onCardDropped = { droppedCard ->
                            val success = viewModel.playCard(droppedCard)
                            battleLog = if (success) {
                                "🃏 Вы разыграли карту ${droppedCard.name}"
                            } else {
                                "❌ Не удалось разыграть карту! Проверьте ману или место."
                            }
                        }
                    ) { isHovered ->
                        val boardBorderColor = if (isHovered) Color.Green else Color(0xFF233A23)
                        val boardBgColor = if (isHovered) Color(0xFF142414) else Color(0xFF141F14)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, boardBorderColor, RoundedCornerShape(8.dp))
                                .background(boardBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.player.board.isEmpty()) {
                                Text("Перетащите карту сюда из руки", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    items(state.player.board, key = { "pl_${it.id}" }) { playerCard ->
                                        val isSelected = selectedCardForAttack?.id == playerCard.id
                                        var cardOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                                        CardComponent(
                                            card = playerCard,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .onGloballyPositioned { coords ->
                                                    val pos = coords.positionInWindow()
                                                    cardOffset = androidx.compose.ui.geometry.Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2)

                                                    // Если карта выбрана, динамически обновляем старт стрелки
                                                    if (isSelected) startArrowOffset = cardOffset
                                                }
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.Green else Color(0xFF4CAF50),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            onClick = {
                                                if (state.currentTurn == Turn.PLAYER) {
                                                    if (isSelected) {
                                                        selectedCardForAttack = null
                                                        isDrawingArrow = false
                                                    } else {
                                                        selectedCardForAttack = playerCard
                                                        startArrowOffset = cardOffset
                                                        currentArrowOffset = cardOffset
                                                        isDrawingArrow = true
                                                        battleLog = "🎯 Выбрана ${playerCard.name}. Нажмите на карту врага или его HP!"
                                                    }
                                                }
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
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF141A1E), RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(state.player.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Ваша мана: ${state.player.currentMana}/${state.player.maxMana} 💧", color = Color(0xFF29B6F6), fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("HP: ${state.player.currentHp}/${state.player.maxHp} ❤️", color = Color(0xFF66BB6A), fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))
                                Button(
                                    onClick = {
                                        viewModel.endTurn()
                                        selectedCardForAttack = null
                                        isDrawingArrow = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                                    shape = RoundedCornerShape(4.dp),
                                    enabled = state.currentTurn == Turn.PLAYER
                                ) {
                                    Text("Конец Хода")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.Start) {
                            items(state.player.hand, key = { "hand_${it.id}" }) { card ->
                                DragTarget(card = card, modifier = Modifier.padding(4.dp)) {
                                    CardComponent(card = card)
                                }
                            }
                        }
                    }
                }

                // === СЛОЙ СТРЕЛКИ ===
                if (isDrawingArrow && selectedCardForAttack != null) {
                    // Подтягиваем координаты конца стрелки к цели, если у игрока наведен фокус (для визуала)
                    val targetCardOffset = state.opponent.board.firstOrNull()?.id?.let { enemyCardsOffsets[it] }
                    val finalArrowEnd = targetCardOffset ?: enemyHeroOffset.takeIf { state.opponent.board.isEmpty() } ?: startArrowOffset

                    AttackArrow(start = startArrowOffset, end = finalArrowEnd)
                }

                // Оверлей конца игры
                AnimatedVisibility(visible = state.isGameOver, enter = fadeIn(), exit = fadeOut()) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Битва Завершена!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            val isPlayerWin = state.winnerName == state.player.name
                            Text(text = if (isPlayerWin) "ВЫ ПОБЕДИЛИ! 🎉" else "ВЫ ПРОИГРАЛИ 💀", color = if (isPlayerWin) Color.Green else Color.Red, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = { viewModel.claimRewardsAndExit(isPlayerWin); onBackToMenu() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)), modifier = Modifier.width(200.dp)) {
                                Text("Выйти")
                            }
                        }
                    }
                }
            }
        }
    }
}
