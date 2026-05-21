package com.example.cardsandshades.model

data class RewardSetModel(
    val gold: Int = 0,
    val crystals: Int = 0,
    val dustCommon: Int = 0,
    val dustRare: Int = 0,
    val dustEpic: Int = 0,
    val dustLegendary: Int = 0,
    val cardName: String? = null
) {
    val isEmpty: Boolean get() = gold == 0 && crystals == 0 && dustCommon == 0 && 
            dustRare == 0 && dustEpic == 0 && dustLegendary == 0 && cardName == null
}
