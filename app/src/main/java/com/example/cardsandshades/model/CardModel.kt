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

    // ИСПРАВЛЕНИЕ: Теперь храним только теги. Это на 100% защищает от крашей Gson!
    val effectTags: List<EffectTag> = emptyList(),

    var isSleeping: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,

    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false
) {
    val isDead: Boolean get() = currentHealth <= 0

    // Динамически опрашиваем тег без жесткого if-else
    val hasTaunt: Boolean get() = effectTags.contains(EffectTag.TAUNT)

    // ПОЛИМОРФИЗМ: Возвращаем реальные ООП-объекты эффектов на основе тегов карты
    val activeEffects: List<CardEffect>
        get() = effectTags.map { tag ->
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
        // Если у карты есть Рывок (RUSH), она не должна спать при перезапуске матча
        isSleeping = !effectTags.contains(EffectTag.RUSH)
    }
}