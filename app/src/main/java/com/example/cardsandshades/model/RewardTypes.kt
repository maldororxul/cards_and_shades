package com.example.cardsandshades.model

data class RewardSetModel(
    val gold: Int = 0,
    val crystals: Int = 0,
    val dustCommon: Int = 0,
    val dustUncommon: Int = 0,
    val dustRare: Int = 0,
    val dustEpic: Int = 0,
    val dustLegendary: Int = 0,
    val dustMythic: Int = 0,
    val hammerCommon: Int = 0,
    val hammerUncommon: Int = 0,
    val hammerRare: Int = 0,
    val hammerEpic: Int = 0,
    val hammerLegendary: Int = 0,
    val hammerMythic: Int = 0,
    val cardName: String? = null
) {
    val isEmpty: Boolean get() = gold == 0 && crystals == 0 && 
            dustCommon == 0 && dustUncommon == 0 && dustRare == 0 && dustEpic == 0 && dustLegendary == 0 && dustMythic == 0 &&
            hammerCommon == 0 && hammerUncommon == 0 && hammerRare == 0 && hammerEpic == 0 && hammerLegendary == 0 && hammerMythic == 0 &&
            cardName == null
}
