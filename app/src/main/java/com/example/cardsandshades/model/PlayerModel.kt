package com.example.cardsandshades.model

data class PlayerModel(
    val name: String,
    var maxHp: Int = 30,
    var currentHp: Int = 30,
    var maxMana: Int = 1,
    var currentMana: Int = 1,
    val deck: MutableList<CardModel> = mutableListOf(),
    val hand: MutableList<CardModel> = mutableListOf(),
    val board: MutableList<CardModel> = mutableListOf() // Максимум 5 карт на столе
) {
    val isDead: Boolean get() = currentHp <= 0
}
