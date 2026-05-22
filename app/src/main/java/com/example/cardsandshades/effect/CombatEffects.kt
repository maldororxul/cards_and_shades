package com.example.cardsandshades.effect

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.GameState
import com.example.cardsandshades.model.PlayerModel

// ⚔️ РЫВОК: Карта не спит при призыве
class RushEffect : CardEffect {
    override val name = "effect_rush"
    override val description = "effect_rush_desc"
    override fun onSummon(state: GameState, owner: PlayerModel, card: CardModel) {
        card.isSleeping = false
    }
}

// 🛡️ ПРОВОКАЦИЯ: Просто маркер для валидации целей (логику проверки вынесем в движок)
class TauntEffect : CardEffect {
    override val name = "effect_taunt"
    override val description = "effect_taunt_desc"
}

// 🏹 СТРЕЛОК: Обнуляет ответный урон от цели
class RangedEffect : CardEffect {
    override val name = "effect_ranged"
    override val description = "effect_ranged_desc"
    override fun modifyCounterDamage(attacker: CardModel, target: CardModel, originalCounterDamage: Int): Int {
        return 0 // Ответный урон полностью поглощается
    }
}

// 🔥 МАГ (SPLASH): Наносит по 1 урона соседям цели
class SplashEffect : CardEffect {
    override val name = "effect_splash"
    override val description = "effect_splash_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        val enemyBoard = if (state.player.board.contains(target)) state.player.board else state.opponent.board
        val targetIndex = enemyBoard.indexOf(target)

        if (targetIndex != -1) {
            // Урон соседу слева
            if (targetIndex > 0) {
                val neighbor = enemyBoard[targetIndex - 1]
                neighbor.currentHealth -= 1
                neighbor.lastDamageTaken = 1
                neighbor.isTakingDamage = true
                attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, 1) }
            }
            // Урон соседу справа
            if (targetIndex < enemyBoard.lastIndex) {
                val neighbor = enemyBoard[targetIndex + 1]
                neighbor.currentHealth -= 1
                neighbor.lastDamageTaken = 1
                neighbor.isTakingDamage = true
                attacker.activeEffects.forEach { it.onDamageDealt(state, attacker, 1) }
            }
        }
    }
}

// 🦇 ВАМПИРИЗМ: Лечит героя при нанесении урона
class LifestealEffect : CardEffect {
    override val name = "effect_lifesteal"
    override val description = "effect_lifesteal_desc"
    override fun onDamageDealt(state: GameState, attacker: CardModel, amount: Int) {
        val owner = if (state.player.board.any { it.id == attacker.id }) state.player else state.opponent
        owner.currentHp = (owner.currentHp + amount).coerceAtMost(owner.maxHp)
    }
}

// ✨ БАФФ (БОНУС): Дает +2/+2 случайному союзнику на 2 хода при выходе
class BuffEffect : CardEffect {
    override val name = "effect_blessing"
    override val description = "effect_blessing_desc"
    override fun onSummon(state: GameState, owner: PlayerModel, card: CardModel) {
        val allies = owner.board.filter { it.id != card.id }
        if (allies.isNotEmpty()) {
            val target = allies.random()
            val buff = com.example.cardsandshades.model.BuffModel(
                id = java.util.UUID.randomUUID().toString(),
                name = "effect_buff_name",
                attackBonus = 2,
                healthBonus = 2,
                duration = 2
            )
            target.addBuff(buff)
            target.currentAttack += buff.attackBonus
            target.currentHealth += buff.healthBonus
        }
    }
}
