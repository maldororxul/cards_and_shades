package com.example.cardsandshades.effect

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState

// ⚔️ РЫВОК: Карта не спит при призыве
class RushEffect : CardEffect {
    override val name = "Рывок"
    override val description = "Может атаковать сразу в ход призыва."
    override fun onSummon(card: CardModel) {
        card.isSleeping = false
    }
}

// 🛡️ ПРОВОКАЦИЯ: Просто маркер для валидации целей (логику проверки вынесем в движок)
class TauntEffect : CardEffect {
    override val name = "Провокация"
    override val description = "Враг обязан атаковать это существо в первую очередь."
}

// 🏹 СТРЕЛОК: Обнуляет ответный урон от цели
class RangedEffect : CardEffect {
    override val name = "Стрелок"
    override val description = "При атаке не получает ответного урона."
    override fun modifyCounterDamage(attacker: CardModel, target: CardModel, originalCounterDamage: Int): Int {
        return 0 // Ответный урон полностью поглощается
    }
}

// 🔥 МАГ (SPLASH): Наносит по 1 урона соседям цели
class SplashEffect : CardEffect {
    override val name = "Маг"
    override val description = "При атаке наносит 1 ед. урона соседним картам цели."
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        val enemyBoard = if (state.player.board.contains(target)) state.player.board else state.opponent.board
        val targetIndex = enemyBoard.indexOf(target)

        if (targetIndex != -1) {
            // Урон соседу слева
            if (targetIndex > 0) {
                enemyBoard[targetIndex - 1].currentHealth -= 1
                enemyBoard[targetIndex - 1].lastDamageTaken = 1
                enemyBoard[targetIndex - 1].isTakingDamage = true
            }
            // Урон соседу справа
            if (targetIndex < enemyBoard.lastIndex) {
                enemyBoard[targetIndex + 1].currentHealth -= 1
                enemyBoard[targetIndex + 1].lastDamageTaken = 1
                enemyBoard[targetIndex + 1].isTakingDamage = true
            }
        }
    }
}