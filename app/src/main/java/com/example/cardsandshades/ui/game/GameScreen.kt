package com.example.cardsandshades.ui.game

import com.example.cardsandshades.ui.battle.BattleLogZone
import com.example.cardsandshades.ui.battle.EnemyBoardZone
import com.example.cardsandshades.ui.battle.GameOverOverlay
import com.example.cardsandshades.ui.battle.OpponentHeaderZone
import com.example.cardsandshades.ui.battle.PlayerBoardZone
import com.example.cardsandshades.ui.battle.PlayerControlsZone
import com.example.cardsandshades.ui.battle.RenderAttackArrows
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Turn
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameDialog
import androidx.activity.compose.BackHandler
import com.example.cardsandshades.sound.SoundManager
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.utils.getStringResourceByName
import com.example.cardsandshades.ui.components.GameBackground
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val levelBg = viewModel.currentLevel?.backgroundRes ?: "bg_battle_default"

    GameBackground(screenId = "game", overrideRes = levelBg) {
        if (gameState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            GameScreenContent(viewModel, gameState!!, onBackToMenu, modifier)
        }
    }
}

@Composable
private fun GameScreenContent(
    viewModel: GameViewModel,
    state: com.example.cardsandshades.model.GameState,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCardForAttack by remember { mutableStateOf<CardModel?>(null) }
    
    // ПРОСМОТР КАРТ
    var inspectedCardsList by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var initialInspectedIndex by remember { mutableIntStateOf(0) }

    var showExitDialog by remember { mutableStateOf(false) }

    // Обработка кнопки Назад
    BackHandler {
        showExitDialog = true
    }

    var startArrowOffset by remember { mutableStateOf(Offset.Zero) }
    var isDrawingArrow by remember { mutableStateOf(false) }

    val playerCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    val enemyCardsOffsets = remember { mutableStateMapOf<String, Offset>() }
    var playerHeroOffset by remember { mutableStateOf(Offset.Zero) }
    var enemyHeroOffset by remember { mutableStateOf(Offset.Zero) }

    // ТРЕКИНГ ПАЛЬЦА ДЛЯ СТРЕЛКИ
    var fingerOffset by remember { mutableStateOf(Offset.Zero) }

    val battleStartMsg = stringResource(R.string.battle_start_msg)
    var battleLog by remember { mutableStateOf(battleStartMsg) }
    
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
                GameText(stringResource(R.string.battle_exit_desc), color = Color.Gray, textAlign = TextAlign.Center)
            },
            confirmButton = { onAction ->
                GameButton(text = stringResource(R.string.battle_exit_confirm), onClick = {
                    onAction()
                    onBackToMenu()
                }, containerColor = Color(0xFFD32F2F))
            },
            dismissButton = { onAction ->
                GameButton(text = stringResource(R.string.battle_exit_cancel), onClick = {
                    onAction()
                }, containerColor = Color.Gray)
            }
        )
    }

    val selectedHint = stringResource(R.string.battle_selected_hint)

    com.example.cardsandshades.ui.components.DragAndDropContainer(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    // Отслеживаем движение пальца даже если оно перехвачено дочерними элементами
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (isDrawingArrow) {
                                event.changes.firstOrNull()?.let {
                                    fingerOffset = it.position
                                }
                            }
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val attackHeroMsg = stringResource(R.string.battle_attack_hero)
                val attackHeroFailMsg = stringResource(R.string.battle_attack_hero_fail)
                
                OpponentHeaderZone(
                    opponent = state.opponent,
                    isHeroTakingDamage = viewModel.opponentHeroTakingDamage,
                    damageValue = viewModel.opponentHeroDamageValue,
                    onEnemyHeroPositioned = { enemyHeroOffset = it },
                    onEnemyHeroClick = {
                        selectedCardForAttack?.let { attacker ->
                            if (state.opponent.board.all { it == null }) {
                                viewModel.attackEnemyHero(attacker)
                                battleLog = attackHeroMsg.format(attacker.currentAttack)
                                selectedCardForAttack = null
                                isDrawingArrow = false
                                fingerOffset = Offset.Zero
                            } else {
                                battleLog = attackHeroFailMsg
                            }
                        }
                    }
                )

                val cardAttackMsg = stringResource(R.string.battle_card_attack)
                EnemyBoardZone(
                    boardSlots = state.opponent.board,
                    onCardPositioned = { id, offset -> enemyCardsOffsets[id] = offset },
                    onCardClick = { enemyCard ->
                        if (selectedCardForAttack != null) {
                            val attacker = selectedCardForAttack!!
                            viewModel.attackEnemyCard(attacker, enemyCard)
                            battleLog = cardAttackMsg.format(getStringResourceByName(context, attacker.name), getStringResourceByName(context, enemyCard.name))
                            selectedCardForAttack = null
                            isDrawingArrow = false
                            fingerOffset = Offset.Zero
                        } else {
                            // Клик по карте врага без выделенного атакующего - инспекция
                            val allBoardCards = (state.opponent.board.filterNotNull() + state.player.board.filterNotNull())
                            inspectedCardsList = allBoardCards
                            initialInspectedIndex = allBoardCards.indexOfFirst { it.id == enemyCard.id }.coerceAtLeast(0)
                        }
                    },
                    onCardLongClick = { card ->
                        val allBoardCards = (state.opponent.board.filterNotNull() + state.player.board.filterNotNull())
                        inspectedCardsList = allBoardCards
                        initialInspectedIndex = allBoardCards.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                    }
                )

                BattleLogZone(battleLog = battleLog)

                val cardPlayedMsg = stringResource(R.string.battle_card_played)
                val playFailMsg = stringResource(R.string.battle_play_fail)
                
                PlayerBoardZone(
                    boardSlots = state.player.board,
                    selectedCard = selectedCardForAttack,
                    onCardPositioned = { id, offset ->
                        playerCardsOffsets[id] = offset
                        if (selectedCardForAttack?.id == id) startArrowOffset = offset
                    },
                    onCardClick = { card, offset ->
                        if (state.currentTurn == Turn.PLAYER && !state.isAnimating) {
                            if (selectedCardForAttack?.id == card.id) {
                                selectedCardForAttack = null
                                isDrawingArrow = false
                                fingerOffset = Offset.Zero
                            } else {
                                val canAttack = com.example.cardsandshades.engine.GameEngine.canAttackHero(state, card)
                                if (canAttack) {
                                    selectedCardForAttack = card
                                    startArrowOffset = offset
                                    isDrawingArrow = true
                                    // Устанавливаем fingerOffset чуть выше карты, чтобы стрелка была сразу видна
                                    fingerOffset = Offset(offset.x, offset.y - 50f)
                                    val cardName = getStringResourceByName(context, card.name)
                                    battleLog = selectedHint.format(cardName)
                                } else {
                                    // Инспекция если не может атаковать
                                    val allBoardCards = (state.opponent.board.filterNotNull() + state.player.board.filterNotNull())
                                    inspectedCardsList = allBoardCards
                                    initialInspectedIndex = allBoardCards.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                                }
                            }
                        } else {
                            // Инспекция если не ваш ход
                            val allBoardCards = (state.opponent.board.filterNotNull() + state.player.board.filterNotNull())
                            inspectedCardsList = allBoardCards
                            initialInspectedIndex = allBoardCards.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                        }
                    },
                    onCardLongClick = { card ->
                        val allBoardCards = (state.opponent.board.filterNotNull() + state.player.board.filterNotNull())
                        inspectedCardsList = allBoardCards
                        initialInspectedIndex = allBoardCards.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                    },
                    onCardDroppedInSlot = { droppedCard, slotIndex ->
                        val success = viewModel.playCard(droppedCard, slotIndex)
                        battleLog = if (success) cardPlayedMsg.format(getStringResourceByName(context, droppedCard.name))
                        else playFailMsg
                    }
                )

                PlayerControlsZone(
                    player = state.player,
                    isPlayerTurn = state.currentTurn == Turn.PLAYER,
                    isHeroTakingDamage = viewModel.playerHeroTakingDamage,
                    damageValue = viewModel.playerHeroDamageValue,
                    onPlayerHeroPositioned = { playerHeroOffset = it },
                    onCardLongClick = { card ->
                        // Свайп по руке
                        inspectedCardsList = state.player.hand
                        initialInspectedIndex = state.player.hand.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                    },
                    viewModel = viewModel
                )
            }

            RenderAttackArrows(
                isPlayerDrawing = (isDrawingArrow && selectedCardForAttack != null),
                playerStart = startArrowOffset,
                playerTargetOffset = fingerOffset,
                enemyHeroOffset = enemyHeroOffset,
                isEnemyBoardEmpty = state.opponent.board.all { it == null },
                aiAttackerId = viewModel.opponentAttackerId,
                aiTargetId = viewModel.opponentTargetId,
                isAiTargetingHero = viewModel.isOpponentTargetingHero,
                enemyCardsOffsets = enemyCardsOffsets,
                playerCardsOffsets = playerCardsOffsets,
                playerHeroOffset = playerHeroOffset
            )

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

            if (inspectedCardsList.isNotEmpty()) {
                CardInspectionDialog(
                    cards = inspectedCardsList, 
                    initialIndex = initialInspectedIndex, 
                    onDismiss = { inspectedCardsList = emptyList() }
                )
            }
        }
    }
}
