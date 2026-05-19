package com.example.cardsandshades.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
import com.example.cardsandshades.model.LevelModel
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.model.Turn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    var currentLevel: LevelModel? = null
        private set

    // Убрали принудительный вызов из init, чтобы игра не крашилась при старте до выбора уровня
    init {
        _gameState.value = null
    }

    // Инициализация матча для конкретного уровня кампании
    fun startNewGame(level: LevelModel) {
        currentLevel = level

        // Генерируем новые чистые колоды (глубокие копии объектов)
        val playerDeck = CardCatalog.generateTestDeck()
        val opponentDeck = CardCatalog.generateTestDeck()

        val player = PlayerModel(
            name = "Игрок",
            deck = playerDeck,
            hand = mutableListOf(),
            board = mutableListOf()
        )

        val opponent = PlayerModel(
            name = level.opponentName,
            maxHp = level.opponentMaxHp,
            currentHp = level.opponentMaxHp,
            deck = opponentDeck,
            hand = mutableListOf(),
            board = mutableListOf()
        )

        val initialState = GameState(
            player = player,
            opponent = opponent,
            currentTurn = Turn.PLAYER,
            turnNumber = 1,
            isGameOver = false,
            winnerName = null
        )

        // Раздаем стартовые карты
        repeat(4) {
            GameEngine.drawCard(initialState.player)
            GameEngine.drawCard(initialState.opponent)
        }

        // Начисляем стартовую ману для первого хода
        GameEngine.startTurn(initialState)

        _gameState.value = initialState
    }

    fun claimRewardsAndExit(isPlayerWin: Boolean) {
        if (isPlayerWin) {
            // Безопасное начисление золота через инкапсулированный профиль
            com.example.cardsandshades.model.UserProfile.gold.value += 50
        }
        // Сбрасываем стейт боя, подготавливая ViewModel к следующему матчу кампании
        _gameState.value = null
    }

    fun restartCurrentGame() {
        currentLevel?.let { startNewGame(it) }
    }

    // Разыгрывание карты игроком (с глубоким копированием для Compose)
    fun playCard(card: CardModel): Boolean {
        var isPlayed = false
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedPlayer = state.player.copy(
                        hand = state.player.hand.toMutableList(),
                        board = state.player.board.toMutableList()
                    )
                    val updatedState = state.copy(player = updatedPlayer)

                    // Находим карту по ID именно в актуальной руке игрока из стейта
                    val cardInHand = updatedPlayer.hand.find { it.id == card.id }
                    if (cardInHand != null && updatedPlayer.currentMana >= cardInHand.manaCost && updatedPlayer.board.size < 5) {
                        com.example.cardsandshades.engine.GameEngine.playCard(updatedState, cardInHand)
                        isPlayed = true
                        updatedState
                    } else state
                } else state
            }
        }
        return isPlayed
    }

    // Атака карты на карту с глубоким клонированием стейта статов существ
    fun attackEnemyCard(attacker: CardModel, target: CardModel) {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedPlayer = state.player.copy(board = state.player.board.map { it.copy() }.toMutableList())
                    val updatedOpponent = state.opponent.copy(board = state.opponent.board.map { it.copy() }.toMutableList())
                    val updatedState = state.copy(player = updatedPlayer, opponent = updatedOpponent)

                    val newAttacker = updatedPlayer.board.find { it.id == attacker.id }
                    val newTarget = updatedOpponent.board.find { it.id == target.id }

                    if (newAttacker != null && newTarget != null) {
                        GameEngine.attackCard(updatedState, newAttacker, newTarget)
                    }
                    updatedState
                } else state
            }
        }
    }

    fun attackEnemyHero(attacker: CardModel) {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedPlayer = state.player.copy(board = state.player.board.map { it.copy() }.toMutableList())
                    val updatedOpponent = state.opponent.copy()
                    val updatedState = state.copy(player = updatedPlayer, opponent = updatedOpponent)

                    val newAttacker = updatedPlayer.board.find { it.id == attacker.id }
                    if (newAttacker != null) {
                        GameEngine.attackHero(updatedState, newAttacker)
                    }
                    updatedState
                } else state
            }
        }
    }

    fun endTurn() {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedState = state.copy()
                    GameEngine.endTurn(updatedState)
                    updatedState
                } else state
            }
        }

        val state = _gameState.value
        if (state != null && state.currentTurn == Turn.OPPONENT && !state.isGameOver) {
            executeOpponentTurn()
        }
    }

    private fun executeOpponentTurn() {
        viewModelScope.launch {
            delay(1000)

            // ФАЗА ИИ 1: Розыгрыш карт
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedOpponent = state.opponent.copy(
                        hand = state.opponent.hand.toMutableList(),
                        board = state.opponent.board.toMutableList()
                    )
                    val updatedState = state.copy(opponent = updatedOpponent)
                    val cardsInHand = updatedOpponent.hand.toList()

                    for (card in cardsInHand) {
                        GameEngine.playCard(updatedState, card)
                    }
                    updatedState
                }
            }

            delay(1200)

            // ФАЗА ИИ 2: Атака
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedPlayer = state.player.copy(board = state.player.board.map { it.copy() }.toMutableList())
                    val updatedOpponent = state.opponent.copy(board = state.opponent.board.map { it.copy() }.toMutableList())
                    val updatedState = state.copy(player = updatedPlayer, opponent = updatedOpponent)

                    val attackerCards = updatedOpponent.board.toList()
                    for (attacker in attackerCards) {
                        val activeAttacker = updatedOpponent.board.find { it.id == attacker.id } ?: continue
                        if (updatedState.player.board.isNotEmpty()) {
                            val target = updatedState.player.board.random()
                            GameEngine.attackCard(updatedState, activeAttacker, target)
                        } else {
                            GameEngine.attackHero(updatedState, activeAttacker)
                        }
                    }
                    updatedState
                }
            }

            delay(1000)

            // ФАЗА ИИ 3: Передача хода
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedState = state.copy()
                    GameEngine.endTurn(updatedState)
                    updatedState
                }
            }
        }
    }
}