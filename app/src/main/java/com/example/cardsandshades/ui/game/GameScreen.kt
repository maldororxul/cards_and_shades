package com.example.cardsandshades.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // ЕДИНСТВЕННЫЙ источник правды для состояния стрелки атаки
    var startArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var currentArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var isDrawingArrow by remember { mutableStateOf(false) }

    // Текстовый лог для информирования игрока
    var battleLog by remember { mutableStateOf("Ваш ход. Разыграйте карты или атакуйте врага!") }

    if (gameState == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Загрузка боя...", color = Color.White)
        }
    } else {
        val state = gameState!!

        // Сбрасываем лог, если ход перешел к ИИ
        LaunchedEffect(state.currentTurn) {
            battleLog = if (state.currentTurn == Turn.PLAYER) {
                "Ваш ход! Мана обновлена. Доступно карт на поле: ${state.player.board.size}"
            } else {
                "Ход соперника... Противник принимает решение."
            }
        }

        // Переносим контейнер DragAndDrop на самый верх, чтобы он накрывал экран
        DragAndDropContainer(modifier = modifier) {

            // Заставляем весь экран слушать движения пальца при активной стрелке
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isDrawingArrow) {
                        if (isDrawingArrow) {
                            detectDragGestures(
                                onDragStart = { localOffset ->
                                    // Обновляем наконечник относительно точки старта карты
                                    currentArrowOffset = startArrowOffset
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentArrowOffset = Offset(
                                        currentArrowOffset.x + dragAmount.x,
                                        currentArrowOffset.y + dragAmount.y
                                    )
                                }
                            )
                        }
                    }
            ) {
                // Основная игровая вертикальная разметка интерфейса
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
                                    .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clickable {
                                        selectedCardForAttack?.let { attacker ->
                                            if (state.opponent.board.isEmpty()) {
                                                viewModel.attackEnemyHero(attacker)
                                                battleLog = "💥 Вы атаковали героя врага на ${attacker.currentAttack} урона!"
                                                selectedCardForAttack = null
                                                isDrawingArrow = false // ИСПРАВЛЕНИЕ: Выключаем стрелку после удара
                                            } else {
                                                battleLog = "❌ Нельзя атаковать лицо, пока у врага есть существа на поле!"
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
                                        modifier = Modifier.padding(4.dp),
                                        onClick = {
                                            selectedCardForAttack?.let { attacker ->
                                                viewModel.attackEnemyCard(attacker, enemyCard)
                                                battleLog = "⚔️ ${attacker.name} атаковал ${enemyCard.name}!"
                                                selectedCardForAttack = null
                                                isDrawingArrow = false // ИСПРАВЛЕНИЕ: Убираем стрелку после боя
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ================= ИНФОРМАЦИОННЫЙ ЦЕНТР (ЛОГ) =================
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
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
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
                                "❌ Не удалось разыграть карту! Проверьте ману (${state.player.currentMana}/${state.player.maxMana}) или свободное место."
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
                                Text("Перетащите карту сюда из руки, чтобы призвать существо", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    items(state.player.board, key = { "pl_${it.id}" }) { playerCard ->
                                        val isSelected = selectedCardForAttack?.id == playerCard.id
                                        var cardOffset by remember { mutableStateOf(Offset.Zero) }
                                        var cardSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

                                        CardComponent(
                                            card = playerCard,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .onGloballyPositioned { coords ->
                                                    val pos = coords.positionInWindow()
                                                    cardSize = coords.size
                                                    cardOffset = Offset(pos.x + cardSize.width / 2, pos.y + cardSize.height / 2)
                                                }
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.Green else Color(0xFF4CAF50),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .pointerInput(playerCard.id) {
                                                    // НИЗКОУРОВНЕВЫЙ ТРЕКИНГ СТРЕЛКИ (БЕЗ ЗАДЕРЖЕК КЛИКА)
                                                    androidx.compose.foundation.gestures.awaitEachGesture {
                                                        val down = awaitFirstDown(requireUnconsumed = false)

                                                        if (state.currentTurn == Turn.PLAYER) {
                                                            selectedCardForAttack = playerCard
                                                            startArrowOffset = cardOffset
                                                            currentArrowOffset = cardOffset
                                                            isDrawingArrow = true
                                                            battleLog = "🎯 Наведение атаки из ${playerCard.name}..."
                                                        }

                                                        var finalFingerPosition = cardOffset

                                                        drag(down.id) { change ->
                                                            change.consume()
                                                            // positionInWindow() дает точную глобальную точку на экране
                                                            finalFingerPosition = change.positionInWindow()
                                                            currentArrowOffset = finalFingerPosition
                                                        }

                                                        // ЛОГИКА ДРОПА АТАКИ ПРИ ОТПУСКАНИИ ПАЛЬЦА
                                                        isDrawingArrow = false

                                                        // 1. Проверяем, попали ли в лицо врага (координаты HP врага)
                                                        // Для простоты ККИ механики: если палец в верхней трети экрана — это атака в лицо
                                                        if (finalFingerPosition.y < 300f && state.opponent.board.isEmpty()) {
                                                            viewModel.attackEnemyHero(playerCard)
                                                            battleLog = "💥 Вы атаковали героя врага на ${playerCard.currentAttack} урона!"
                                                            selectedCardForAttack = null
                                                        }
                                                        // 2. Обычный сброс стрелки (кликовая атака по врагу из старой логики также продолжит работать)
                                                        else {
                                                            battleLog = "🎯 Стрелка сброшена. Вы можете нажать на карту врага для атаки."
                                                        }
                                                    }
                                                },
                                            onClick = {
                                                if (state.currentTurn == Turn.PLAYER && isSelected) {
                                                    selectedCardForAttack = null
                                                    isDrawingArrow = false
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
                                Text("Ваша мана: ${state.player.currentMana}/${state.player.maxMana} 💧", color = Color(0xFF29B6F6), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Ваше HP: ${state.player.currentHp}/${state.player.maxHp} ❤️", color = Color(0xFF66BB6A), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                                Button(
                                    onClick = {
                                        viewModel.endTurn()
                                        selectedCardForAttack = null
                                        isDrawingArrow = false // Гарантированно гасим стрелку при передаче хода
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

                        // РУКА ИГРОКА
                        LazyRow(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.Start) {
                            items(state.player.hand, key = { "hand_${it.id}" }) { card ->
                                DragTarget(card = card, modifier = Modifier.padding(4.dp)) {
                                    CardComponent(card = card)
                                }
                            }
                        }
                    }
                }

                // === СЛОЙ 2: КРАСНАЯ СТРЕЛКА АТАКИ ПОВЕРХ ВСЕГО СТОЛА ===
                if (isDrawingArrow && selectedCardForAttack != null) {
                    AttackArrow(start = startArrowOffset, end = currentArrowOffset)
                }

                // Оверлей конца игры
                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Битва Завершена!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))

                            val isPlayerWin = state.winnerName == state.player.name
                            Text(
                                text = if (isPlayerWin) "ВЫ ПОБЕДИЛИ! 🎉" else "ВЫ ПРОИГРАЛИ 💀",
                                color = if (isPlayerWin) Color.Green else Color.Red,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPlayerWin) {
                                Text("+50 Золотых монет начислено", color = Color.Yellow, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    viewModel.claimRewardsAndExit(isPlayerWin)
                                    onBackToMenu()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text("Забрать награду и выйти")
                            }
                        }
                    }
                }
            }
        }
    }
}
