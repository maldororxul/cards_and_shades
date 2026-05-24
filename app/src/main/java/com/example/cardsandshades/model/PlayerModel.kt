package com.example.cardsandshades.model

data class PlayerModel(
    val name: String,
    var maxHp: Int = 30,
    var currentHp: Int = 30,
    var maxMana: Int = 1,
    var currentMana: Int = 1,
    val deck: MutableList<CardModel> = mutableListOf(),
    val hand: MutableList<CardModel> = mutableListOf(),
    val board: Array<CardModel?> = arrayOfNulls(5), // Максимум 5 карт на столе (фиксированные слоты)
    var buffs: List<BuffModel> = emptyList() // Баффы/дебаффы героя (например, кровотечение)
) {
    val isDead: Boolean get() = currentHp <= 0

    fun deepCopy(): PlayerModel {
        return this.copy(
            deck = this.deck.map { it.deepCopy() }.toMutableList(),
            hand = this.hand.map { it.deepCopy() }.toMutableList(),
            board = this.board.map { it?.deepCopy() }.toTypedArray()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerModel) return false
        if (name != other.name) return false
        if (maxHp != other.maxHp) return false
        if (currentHp != other.currentHp) return false
        if (maxMana != other.maxMana) return false
        if (currentMana != other.currentMana) return false
        if (deck != other.deck) return false
        if (hand != other.hand) return false
        if (!board.contentEquals(other.board)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + maxHp
        result = 31 * result + currentHp
        result = 31 * result + maxMana
        result = 31 * result + currentMana
        result = 31 * result + deck.hashCode()
        result = 31 * result + hand.hashCode()
        result = 31 * result + board.contentHashCode()
        return result
    }
}
