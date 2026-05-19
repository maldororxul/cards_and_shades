package com.example.cardsandshades.ui.components

import androidx.lifecycle.ViewModel
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.engine.GameEngine
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.model.Turn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


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

                    // Временная заглушка: ИИ сразу завершает свой ход,
                    // чтобы вернуть ход игроку для тестов механики
                    if (updatedState.currentTurn == Turn.OPPONENT) {
                        GameEngine.endTurn(updatedState)
                    }

                    updatedState
                } else state
            }
        }
    }
}