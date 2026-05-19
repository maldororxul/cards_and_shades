package com.example.cardsandshades.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
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

    init {
        startNewGame()
    }

    // Инициализация матча
    fun startNewGame() {
        val player = PlayerModel(
            name = "Игрок",
            deck = CardCatalog.generateTestDeck()
        )
        val opponent = PlayerModel(
            name = "Темный Властелин",
            deck = CardCatalog.generateTestDeck()
        )

        val initialState = GameState(
            player = player,
            opponent = opponent,
            currentTurn = Turn.PLAYER,
            turnNumber = 1
        )

        // Раздача стартовой руки (по 4 карты)
        repeat(4) {
            GameEngine.drawCard(initialState.player)
            GameEngine.drawCard(initialState.opponent)
        }

        // Запуск первого хода (начисление маны и добор первой карты за ход)
        GameEngine.startTurn(initialState)

        _gameState.value = initialState
    }

    // Игрок разыгрывает карту из руки
    fun playCard(card: CardModel) {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedState = state.copy()
                    GameEngine.playCard(updatedState, card)
                    updatedState
                } else state
            }
        }
    }

    // Игрок атакует карту противника своей картой
    fun attackEnemyCard(attacker: CardModel, target: CardModel) {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedState = state.copy()
                    GameEngine.attackCard(updatedState, attacker, target)
                    updatedState
                } else state
            }
        }
    }

    // Игрок атакует "лицо" противника
    fun attackEnemyHero(attacker: CardModel) {
        _gameState.update { currentState ->
            currentState?.let { state ->
                if (state.currentTurn == Turn.PLAYER) {
                    val updatedState = state.copy()
                    GameEngine.attackHero(updatedState, attacker)
                    updatedState
                } else state
            }
        }
    }

    // Завершение хода игрока
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

        // Если ход перешел к ИИ, запускаем его логику
        val state = _gameState.value
        if (state != null && state.currentTurn == Turn.OPPONENT && !state.isGameOver) {
            executeOpponentTurn()
        }
    }

    // Логика поведения Искусственного Интеллекта
    private fun executeOpponentTurn() {
        viewModelScope.launch {
            // 1. Небольшая пауза, будто ИИ оценивает ситуацию
            delay(1000)

            // --- ФАЗА 1: Разыгрывание карт из руки ---
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedState = state.copy()
                    // Берем копию руки, чтобы не словить ConcurrentModificationException при удалении
                    val cardsInHand = updatedState.opponent.hand.toList()

                    // Пытаемся разыграть карты, пока хватает маны и места на столе
                    for (card in cardsInHand) {
                        GameEngine.playCard(updatedState, card)
                    }
                    updatedState
                }
            }

            delay(1200) // Пауза между разыгрыванием карт и атакой

            // --- ФАЗА 2: Проведение атак ---
            _gameState.update { currentState ->
                currentState?.let { state ->
                    val updatedState = state.copy()

                    // ИИ берет свои карты на столе для совершения атак
                    val attackerCards = updatedState.opponent.board.toList()

                    for (attacker in attackerCards) {
                        // Если у игрока есть карты на поле — бьем их
                        if (updatedState.player.board.isNotEmpty()) {
                            // ИИ выбирает случайную цель на столе игрока
                            val target = updatedState.player.board.random()
                            GameEngine.attackCard(updatedState, attacker, target)
                        } else {
                            // Если у игрока пусто на столе — бьем напрямую в лицо
                            GameEngine.attackHero(updatedState, attacker)
                        }
                    }
                    updatedState
                }
            }

            delay(1000) // Пауза перед завершением хода

            // --- ФАЗА 3: Завершение хода ИИ ---
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