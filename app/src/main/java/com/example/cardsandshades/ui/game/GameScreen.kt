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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Turn
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.DropTarget
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameDialog
import androidx.activity.compose.BackHandler
import com.example.cardsandshades.sound.SoundManager
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.utils.getStringResourceByName

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
    var showExitDialog by remember { mutableStateOf(false) }

    // Обработка кнопки Назад
    BackHandler {
        showExitDialog = true
    }

    var startArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var currentArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var isDrawingArrow by remember { mutableStateOf(false) }

    val playerCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    val enemyCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    var playerHeroOffset by remember { mutableStateOf(Offset.Zero) }
    var enemyHeroOffset by remember { mutableStateOf(Offset.Zero) }

    val battleStartMsg = stringResource(R.string.battle_start_msg)
    var battleLog by remember { mutableStateOf(battleStartMsg) }

    if (gameState == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            GameText(stringResource(R.string.battle_loading), color = Color.White)
        }
        return
    }

    val state = gameState!!
    
    val yourTurnMsg = stringResource(R.string.battle_your_turn)
    val opponentTurnMsg = stringResource(R.string.battle_opponent_turn)

    LaunchedEffect(state.currentTurn) {
        battleLog = if (state.currentTurn == Turn.PLAYER) yourTurnMsg else opponentTurnMsg
    }

    if (showExitDialog) {
        GameDialog(
            onDismiss = { showExitDialog = false },
            title = stringResource(R.string.battle_exit_title),
            content = {
                GameText(stringResource(R.string.battle_exit_desc), color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            },
            confirmButton = { onAction ->
                GameButton(text = stringResource(R.string.battle_exit_confirm), onClick = {
                    onAction()
                    onBackToMenu()
                }, containerColor = Color(0xFFD32F2F))
            },
            dismissButton = { onAction ->
                GameButton(text = stringResource(R.string.battle_exit_cancel), onClick = onAction, containerColor = Color.Gray)
            }
        )
    }

    com.example.cardsandshades.ui.components.DragAndDropContainer(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ВРАГ: Передаем значения из viewModel напрямую в аргументы
                val attackHeroMsg = stringResource(R.string.battle_attack_hero)
                val attackHeroFailMsg = stringResource(R.string.battle_attack_hero_fail)
                
                OpponentHeaderZone(
                    opponent = state.opponent,
                    isHeroTakingDamage = viewModel.opponentHeroTakingDamage,
                    damageValue = viewModel.opponentHeroDamageValue,
                    onEnemyHeroPositioned = { enemyHeroOffset = it },
                    onEnemyHeroClick = {
                        selectedCardForAttack?.let { attacker ->
                            if (state.opponent.board.isEmpty()) {
                                viewModel.attackEnemyHero(attacker)
                                battleLog = attackHeroMsg.format(attacker.currentAttack)
                                selectedCardForAttack = null
                                isDrawingArrow = false
                            } else {
                                battleLog = attackHeroFailMsg
                            }
                        }
                    }
                )

                // ПОЛЕ БОЯ ВРАГА
                val cardAttackMsg = stringResource(R.string.battle_card_attack)
                EnemyBoardZone(
                    boardCards = state.opponent.board,
                    onCardPositioned = { id, offset -> enemyCardsOffsets[id] = offset },
                    onCardClick = { enemyCard ->
                        if (selectedCardForAttack != null) {
                            val attacker = selectedCardForAttack!!
                            viewModel.attackEnemyCard(attacker, enemyCard)
                            battleLog = cardAttackMsg.format(getStringResourceByName(context, attacker.name), getStringResourceByName(context, enemyCard.name))
                            selectedCardForAttack = null
                            isDrawingArrow = false
                        }
                    }
                )

                // ЛОГ ИНФОРМАЦИИ БОЯ
                BattleLogZone(battleLog = battleLog)

                // ПОЛЕ БОЯ ИГРОКА (С ЗОНОЙ СБРОСА)
                val cardPlayedMsg = stringResource(R.string.battle_card_played)
                val playFailMsg = stringResource(R.string.battle_play_fail)
                DropTarget(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    onCardDropped = { droppedCard ->
                        val success = viewModel.playCard(droppedCard)
                        battleLog = if (success) cardPlayedMsg.format(getStringResourceByName(context, droppedCard.name))
                        else playFailMsg
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
                                    // Второе нажатие снимает выбор
                                    selectedCardForAttack = null
                                    isDrawingArrow = false
                                } else {
                                    val canAttack = com.example.cardsandshades.engine.GameEngine.canAttackHero(state, card)
                                    if (canAttack) {
                                        selectedCardForAttack = card
                                        startArrowOffset = offset
                                        currentArrowOffset = offset
                                        isDrawingArrow = true
                                        battleLog = context.getString(R.string.battle_selected_hint, getStringResourceByName(context, card.name))
                                    }
                                }
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
            val unlockedLevel by UserProfile.maxUnlockedLevel.collectAsState()
            val isFirstTimeWin = (viewModel.currentLevel?.id ?: 0) >= unlockedLevel
            val rewards = if (isFirstTimeWin) viewModel.currentLevel?.firstTimeReward else viewModel.currentLevel?.repeatReward

            GameOverOverlay(
                isGameOver = state.isGameOver,
                winnerName = state.winnerName,
                playerName = state.player.name,
                rewards = rewards,
                onExitClick = { isPlayerWin ->
                    viewModel.claimRewardsAndExit(isPlayerWin)
                    UserProfile.save(context)
                    SoundManager.startMusic(context)
                    onBackToMenu()
                }
            )

            if (inspectedCard != null) {
                CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
            }
        }
    }
}
