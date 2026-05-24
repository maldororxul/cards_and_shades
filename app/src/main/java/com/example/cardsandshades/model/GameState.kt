package com.example.cardsandshades.model

enum class LogType { PLAYER, OPPONENT, SYSTEM }

data class LogEntry(
    val message: String,
    val type: LogType,
    val turnNumber: Int
)

data class GameState(
    val player: PlayerModel,
    val opponent: PlayerModel,
    var currentTurn: Turn = Turn.PLAYER,
    var turnNumber: Int = 1,
    var isGameOver: Boolean = false,
    var winnerName: String? = null,
    var isAnimating: Boolean = false, // Блокирует клики во время боя
    var logHistory: MutableList<LogEntry> = mutableListOf()
) {
    fun deepCopy(): GameState {
        return this.copy(
            player = this.player.deepCopy(),
            opponent = this.opponent.deepCopy(),
            logHistory = this.logHistory.toMutableList()
        )
    }
}

enum class Turn {
    PLAYER, OPPONENT
}
