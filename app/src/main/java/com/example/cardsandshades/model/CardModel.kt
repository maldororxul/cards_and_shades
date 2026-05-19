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
    // Текущие значения в бою (по умолчанию равны базовым)
    var currentAttack: Int = baseAttack,
    var currentHealth: Int = baseHealth
) {
    val isDead: Boolean get() = currentHealth <= 0

    // Метод для сброса состояния карты перед новым матчем
    fun reset() {
        currentAttack = baseAttack
        currentHealth = baseHealth
    }
}