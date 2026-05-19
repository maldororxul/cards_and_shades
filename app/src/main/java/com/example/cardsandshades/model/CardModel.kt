package com.example.cardsandshades.model

import com.example.cardsandshades.effect.CardEffect
import com.example.cardsandshades.effect.TauntEffect

enum class Rarity {
    COMMON, RARE, EPIC, LEGENDARY
}

data class CardModel(
    val id: String,
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    var currentAttack: Int = baseAttack,
    var currentHealth: Int = baseHealth,

    // Новая ООП структура эффектов
    val effects: List<CardEffect> = emptyList(),

    // Боевые состояния хода
    var isSleeping: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,

    // Состояния анимации
    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false
) {
    val isDead: Boolean get() = currentHealth <= 0

    // Вспомогательные проверки наличия эффектов у карты без явных if-else
    val hasTaunt: Boolean get() = effects.any { it is TauntEffect }

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
    }
}