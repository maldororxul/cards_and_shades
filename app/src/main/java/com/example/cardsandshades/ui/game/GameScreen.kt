package com.example.cardsandshades.ui.game

import BattleLogZone
import EnemyBoardZone
import com.example.cardsandshades.ui.battle.GameOverOverlay
import OpponentHeaderZone
import PlayerBoardZone
import PlayerControlsZone
import RenderAttackArrows
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Turn
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.DragAndDropContainer
import com.example.cardsandshades.ui.components.DropTarget
import com.example.cardsandshades.ui.components.CardInspectionDialog

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    var selectedCardForAttack by remember { mutableStateOf<CardModel?>(null) }
    var inspectedCard by remember { mutableStateOf<CardModel?>(null) }

    var startArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var currentArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var isDrawingArrow by remember { mutableStateOf(false) }

    val playerCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    val enemyCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    var playerHeroOffset by remember { mutableStateOf(Offset.Zero) }
    var enemyHeroOffset by remember { mutableStateOf(Offset.Zero) }

    var battleLog by remember { mutableStateOf("Ваш ход. Разыграйте карты или атакуйте врага!") }

    if (gameState == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Загрузка боя...", color = Color.White)
        }
        return
    }

    val state = gameState!!

    LaunchedEffect(state.currentTurn) {
        battleLog = if (state.currentTurn == Turn.PLAYER) "Ваш ход! Мана обновлена." else "Ход соперника..."
    }

    DragAndDropContainer(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ВРАГ: Передаем значения из viewModel напрямую в аргументы
                OpponentHeaderZone(
                    opponent = state.opponent,
                    isHeroTakingDamage = viewModel.opponentHeroTakingDamage,
                    damageValue = viewModel.opponentHeroDamageValue,
                    onEnemyHeroPositioned = { enemyHeroOffset = it },
                    onEnemyHeroClick = {
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

                // ПОЛЕ БОЯ ВРАГА
                EnemyBoardZone(
                    boardCards = state.opponent.board,
                    onCardPositioned = { id, offset -> enemyCardsOffsets[id] = offset },
                    onCardClick = { enemyCard ->
                        if (selectedCardForAttack != null) {
                            val attacker = selectedCardForAttack!!
                            viewModel.attackEnemyCard(attacker, enemyCard)
                            battleLog = "⚔️ ${attacker.name} атаковал ${enemyCard.name}!"
                            selectedCardForAttack = null
                            isDrawingArrow = false
                        } else {
                            // Если никто не выбран для атаки — открываем инфо карты
                            inspectedCard = enemyCard
                        }
                    }
                )

                // ЛОГ ИНФОРМАЦИИ БОЯ
                BattleLogZone(battleLog = battleLog)

                // ПОЛЕ БОЯ ИГРОКА (С ЗОНОЙ СБРОСА)
                DropTarget(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    onCardDropped = { droppedCard ->
                        val success = viewModel.playCard(droppedCard)
                        battleLog = if (success) "🃏 Вы разыграли карту ${droppedCard.name}"
                        else "❌ Не удалось разыграть карту! Проверьте ману или место."
                    }
                ) { isHovered ->
                    PlayerBoardZone(
                        boardCards = state.player.board,
                        selectedCard = selectedCardForAttack,
                        isHovered = isHovered,
                        onCardPositioned = { id, offset ->
                            playerCardsOffsets[id] = offset
                            if (selectedCardForAttack?.id == id) startArrowOffset = offset
                        },
                        onCardClick = { card, offset ->
                            if (state.currentTurn == Turn.PLAYER && !state.isAnimating) {
                                if (selectedCardForAttack?.id == card.id) {
                                    // Второе нажатие на выбранную карту открывает детали
                                    inspectedCard = card
                                    selectedCardForAttack = null
                                    isDrawingArrow = false
                                } else {
                                    val canAttack = com.example.cardsandshades.engine.GameEngine.canAttackHero(state, card)
                                    if (canAttack) {
                                        selectedCardForAttack = card
                                        startArrowOffset = offset
                                        currentArrowOffset = offset
                                        isDrawingArrow = true
                                        battleLog = "🎯 Выбрана ${card.name}. Нажмите на карту врага или его HP!"
                                    } else {
                                        // Если не может атаковать — просто смотрим детали
                                        inspectedCard = card
                                    }
                                }
                            } else {
                                // В чужой ход — только просмотр
                                inspectedCard = card
                            }
                        }
                    )
                }

                // ИГРОК: Контроллеры, мана и рука. Тоже передаем значения урона по лицу
                PlayerControlsZone(
                    player = state.player,
                    isPlayerTurn = state.currentTurn == Turn.PLAYER,
                    isHeroTakingDamage = viewModel.playerHeroTakingDamage,
                    damageValue = viewModel.playerHeroDamageValue,
                    onPlayerHeroPositioned = { playerHeroOffset = it },
                    onEndTurnClick = {
                        viewModel.endTurn()
                        selectedCardForAttack = null
                        isDrawingArrow = false
                    }
                )
            }

            // ВИЗУАЛЬНЫЙ СЛОЙ: Рендеринг стрелок атаки
            RenderAttackArrows(
                isPlayerDrawing = isDrawingArrow && selectedCardForAttack != null,
                playerStart = startArrowOffset,
                playerTargetOffset = state.opponent.board.firstOrNull()?.id?.let { enemyCardsOffsets[it] },
                enemyHeroOffset = enemyHeroOffset,
                isEnemyBoardEmpty = state.opponent.board.isEmpty(),
                aiAttackerId = viewModel.opponentAttackerId,
                aiTargetId = viewModel.opponentTargetId,
                isAiTargetingHero = viewModel.isOpponentTargetingHero,
                enemyCardsOffsets = enemyCardsOffsets,
                playerCardsOffsets = playerCardsOffsets,
                playerHeroOffset = playerHeroOffset
            )

            // ЭКРАН ЗАВЕРШЕНИЯ ИГРЫ
            GameOverOverlay(
                isGameOver = state.isGameOver,
                winnerName = state.winnerName,
                playerName = state.player.name,
                // ПЕРЕДАЕМ ДАННЫЕ НАГРАДЫ ТЕКУЩЕГО УРОВНЯ В ВЕРСТКУ
                rewardGold = viewModel.currentLevel?.rewardGold ?: 50,
                rewardCardName = viewModel.currentLevel?.rewardCardName,
                onExitClick = { isPlayerWin ->
                    viewModel.claimRewardsAndExit(isPlayerWin)
                    UserProfile.save(context)
                    onBackToMenu()
                }
            )

            if (inspectedCard != null) {
                CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
            }
        }
    }
}