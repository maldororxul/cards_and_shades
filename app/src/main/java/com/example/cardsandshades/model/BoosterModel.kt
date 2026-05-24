package com.example.cardsandshades.model

data class BoosterChances(
    val common: Int,
    val uncommon: Int,
    val rare: Int,
    val epic: Int,
    val legendary: Int,
    val mythic: Int = 1
)

data class BoosterModel(
    val id: String,
    val name: String,
    val costAmount: Int,
    val costType: String, // "gold" or "crystals"
    val description: String,
    val chances: BoosterChances
)
