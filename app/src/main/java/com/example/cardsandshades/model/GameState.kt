package com.example.cardsandshades.model

data class GameState(
    val player: PlayerModel,
    val opponent: PlayerModel,
    var currentTurn: Turn = Turn.PLAYER,
    var turnNumber: Int = 1,
    var isGameOver: Boolean = false,
    var winnerName: String? = null,
    var isAnimating: Boolean = false // Блокирует клики во время боя
)

enum class Turn {
    PLAYER, OPPONENT
}
