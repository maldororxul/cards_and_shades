package com.example.cardsandshades.model

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
    // Поля для анимации (не участвуют в copy по умолчанию)
    var isAttacking: Boolean = false,
    var isTakingDamage: Boolean = false,
    var lastDamageTaken: Int = 0,
    var isDying: Boolean = false
) {
    val isDead: Boolean get() = currentHealth <= 0

    // Метод для сброса состояния карты перед новым матчем
    fun reset() {
        currentAttack = baseAttack
        currentHealth = baseHealth
    }
}