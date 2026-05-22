package com.example.cardsandshades.model

import com.example.cardsandshades.effect.*

// Перечисление всех ККИ-эффектов для безопасного сохранения в JSON
enum class EffectTag {
    RUSH, TAUNT, RANGED, SPLASH, LIFESTEAL, BUFF
}

enum class Rarity { COMMON, RARE, EPIC, LEGENDARY }

data class BuffModel(
    val id: String,
    val name: String,
    val attackBonus: Int,
    val healthBonus: Int,
    var duration: Int // Ходов осталось
)

data class CardModel(
    val id: String,
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    var currentAttack: Int = baseAttack,
    var currentHealth: Int = baseHealth,

    // Gson может записать сюда null при десериализации старого кэша, если поле отсутствовало
    private val effectTags: List<EffectTag>? = emptyList(),
    private val activeBuffs: List<BuffModel>? = emptyList(),

    var isSleeping: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,
    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false
) {
    val isDead: Boolean get() = currentHealth <= 0

    // ИСПРАВЛЕНИЕ: Безопасный публичный доступ к тегам
    val activeTags: List<EffectTag> get() = effectTags ?: emptyList()

    // ИСПРАВЛЕНИЕ: Безопасный доступ к баффам (теперь они приватные и копируются при изменении)
    private var currentBuffs: List<BuffModel> = activeBuffs ?: emptyList()

    val buffs: List<BuffModel> get() = currentBuffs

    fun addBuff(buff: BuffModel) {
        currentBuffs = currentBuffs + buff
    }

    fun removeBuffs(expired: List<BuffModel>) {
        currentBuffs = currentBuffs.filter { !expired.contains(it) }
    }

    fun clearBuffs() {
        currentBuffs = emptyList()
    }

    // ИСПРАВЛЕНИЕ: Проверка танка через activeTags
    val hasTaunt: Boolean get() = activeTags.contains(EffectTag.TAUNT)

    // ИСПРАВЛЕНИЕ: Развертывание эффектов через activeTags
    val activeEffects: List<CardEffect>
        get() = activeTags.map { tag ->
            when (tag) {
                EffectTag.RUSH -> RushEffect()
                EffectTag.TAUNT -> TauntEffect()
                EffectTag.RANGED -> RangedEffect()
                EffectTag.SPLASH -> SplashEffect()
                EffectTag.LIFESTEAL -> LifestealEffect()
                EffectTag.BUFF -> BuffEffect()
            }
        }

    fun resetTurnState() {
        isSleeping = false
        hasAttackedThisTurn = false
    }

    fun deepCopy(): CardModel {
        val copy = this.copy()
        copy.currentBuffs = this.currentBuffs.map { it.copy() }
        return copy
    }

    fun reset() {
        currentAttack = baseAttack
        currentHealth = baseHealth
        clearBuffs()
        isAttacking = false
        isTakingDamage = false
        isDying = false
        lastDamageTaken = 0
        isSleeping = !activeTags.contains(EffectTag.RUSH)
    }
}
