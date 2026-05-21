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

    fun deepCopy(): PlayerModel {
        return this.copy(
            deck = this.deck.map { it.deepCopy() }.toMutableList(),
            hand = this.hand.map { it.deepCopy() }.toMutableList(),
            board = this.board.map { it.deepCopy() }.toMutableList()
        )
    }
}
