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

// 🩸 КРОВОТЕЧЕНИЕ: Наносит 1 урона в начале каждого хода в течение 3 ходов
class BleedEffect : CardEffect {
    override val name = "effect_bleed"
    override val description = "effect_bleed_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        if (!target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_BLEED)) {
            target.addBuff(com.example.cardsandshades.model.BuffModel(
                id = java.util.UUID.randomUUID().toString(),
                name = "effect_bleed",
                attackBonus = 0,
                healthBonus = 0,
                duration = 3,
                tag = com.example.cardsandshades.model.EffectTag.BLEED
            ))
        }
    }
}

// 🤢 ЯД: Наносит 2 урона в начале каждого хода в течение 2 ходов
class PoisonEffect : CardEffect {
    override val name = "effect_poison"
    override val description = "effect_poison_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        if (!target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_POISON)) {
            target.addBuff(com.example.cardsandshades.model.BuffModel(
                id = java.util.UUID.randomUUID().toString(),
                name = "effect_poison",
                attackBonus = 0,
                healthBonus = 0,
                duration = 2,
                tag = com.example.cardsandshades.model.EffectTag.POISON
            ))
        }
    }
}

// 🔥 ГОРЕНИЕ: Наносит 3 урона в начале каждого хода в течение 2 ходов
class BurnEffect : CardEffect {
    override val name = "effect_burn"
    override val description = "effect_burn_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        if (!target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_BURN)) {
            target.addBuff(com.example.cardsandshades.model.BuffModel(
                id = java.util.UUID.randomUUID().toString(),
                name = "effect_burn",
                attackBonus = 0,
                healthBonus = 0,
                duration = 2,
                tag = com.example.cardsandshades.model.EffectTag.BURN
            ))
        }
    }
}

// ❄️ СЛАБОСТЬ (DEBUFF_ATTACK): Снижает атаку цели на 2 на 2 хода
class DebuffAttackEffect : CardEffect {
    override val name = "effect_debuff_attack"
    override val description = "effect_debuff_attack_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        val debuff = com.example.cardsandshades.model.BuffModel(
            id = java.util.UUID.randomUUID().toString(),
            name = "effect_debuff_attack",
            attackBonus = -2,
            healthBonus = 0,
            duration = 2,
            tag = com.example.cardsandshades.model.EffectTag.DEBUFF_ATTACK
        )
        target.addBuff(debuff)
        target.currentAttack += debuff.attackBonus
    }
}

// ИММУНИТЕТЫ (Пустые классы, логика в других эффектах)
class ImmuneBleedEffect : CardEffect {
    override val name = "effect_immune_bleed"
    override val description = "effect_immune_bleed_desc"
}
class ImmunePoisonEffect : CardEffect {
    override val name = "effect_immune_poison"
    override val description = "effect_immune_poison_desc"
}
class ImmuneBurnEffect : CardEffect {
    override val name = "effect_immune_burn"
    override val description = "effect_immune_burn_desc"
}

// ❄️ ЗАМОРОЗКА: Шанс 30% заморозить цель на 1 ход (не может атаковать)
class FreezeEffect : CardEffect {
    override val name = "effect_freeze"
    override val description = "effect_freeze_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        if ((1..100).random() <= 30 && !target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_FREEZE)) {
            target.isFrozen = true
        }
    }
}

// 💫 ОГЛУШЕНИЕ: Шанс 20% оглушить цель на 1 ход
class StunEffect : CardEffect {
    override val name = "effect_stun"
    override val description = "effect_stun_desc"
    override fun onAfterAttack(state: GameState, attacker: CardModel, target: CardModel) {
        if ((1..100).random() <= 20 && !target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_STUN)) {
            target.isStunned = true
        }
    }
}

// 💥 КРИТ: Шанс 25% нанести двойной урон (или по мультипликатору)
class CritEffect : CardEffect {
    override val name = "effect_crit"
    override val description = "effect_crit_desc"
    override fun modifyOutgoingDamage(attacker: CardModel, target: CardModel, originalDamage: Int): Int {
        if ((1..100).random() <= 25 && !target.activeTags.contains(com.example.cardsandshades.model.EffectTag.IMMUNE_CRIT)) {
            return (originalDamage * attacker.critMultiplier).toInt()
        }
        return originalDamage
    }
}

// ⚔️ ВОЗМЕЗДИЕ: Если атакуют соседа, эта карта наносит ответный урон атакующему
class RetributionEffect : CardEffect {
    override val name = "effect_retribution"
    override val description = "effect_retribution_desc"
}

// 💚 ИСЦЕЛЕНИЕ: Лечит 2 ед. здоровья в начале хода
class HealEffect : CardEffect {
    override val name = "effect_heal"
    override val description = "effect_heal_desc"
    override fun onStartTurn(state: GameState, owner: PlayerModel, card: CardModel) {
        if (!card.isUndead) {
            card.currentHealth = (card.currentHealth + 2).coerceAtMost(card.baseHealth)
        }
    }
}

// 🌟 МАССОВОЕ ИСЦЕЛЕНИЕ: Лечит всех союзников на 1 при выходе
class MassHealEffect : CardEffect {
    override val name = "effect_mass_heal"
    override val description = "effect_mass_heal_desc"
    override fun onSummon(state: GameState, owner: PlayerModel, card: CardModel) {
        owner.board.filterNotNull().forEach { ally ->
            if (!ally.isUndead) {
                ally.currentHealth = (ally.currentHealth + 1).coerceAtMost(ally.baseHealth)
            }
        }
    }
}

// 🏹 АВТО-АТАКА: Атакует карту, которую враг ставит напротив или рядом
class AutoAttackPlayedEffect : CardEffect {
    override val name = "effect_auto_attack"
    override val description = "effect_auto_attack_desc"
}

// ➕ БОНУС СОСЕДЯМ (АТАКА)
class NeighborBuffAttackEffect : CardEffect {
    override val name = "effect_neighbor_atk"
    override val description = "effect_neighbor_atk_desc"
}

// ➕ БОНУС СОСЕДЯМ (ЗДОРОВЬЕ)
class NeighborBuffHealthEffect : CardEffect {
    override val name = "effect_neighbor_hp"
    override val description = "effect_neighbor_hp_desc"
}

class ImmuneFreezeEffect : CardEffect {
    override val name = "effect_immune_freeze"
    override val description = "effect_immune_freeze_desc"
}
class ImmuneStunEffect : CardEffect {
    override val name = "effect_immune_stun"
    override val description = "effect_immune_stun_desc"
}
class ImmuneCritEffect : CardEffect {
    override val name = "effect_immune_crit"
    override val description = "effect_immune_crit_desc"
}
