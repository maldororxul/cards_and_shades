package com.example.cardsandshades.model

import com.example.cardsandshades.effect.CardEffect
import com.example.cardsandshades.effect.RangedEffect
import com.example.cardsandshades.effect.RushEffect
import com.example.cardsandshades.effect.SplashEffect
import com.example.cardsandshades.effect.TauntEffect

// Перечисление всех ККИ-эффектов для безопасного сохранения в JSON
enum class EffectTag {
    RUSH, TAUNT, RANGED, SPLASH
}

enum class Rarity { COMMON, RARE, EPIC, LEGENDARY }

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
            }
        }

    fun resetTurnState() {
        isSleeping = false
        hasAttackedThisTurn = false
    }

    fun reset() {
        currentAttack = baseAttack
        currentHealth = baseHealth
        isAttacking = false
        isTakingDamage = false
        isDying = false
        lastDamageTaken = 0
        isSleeping = !activeTags.contains(EffectTag.RUSH)
    }
}