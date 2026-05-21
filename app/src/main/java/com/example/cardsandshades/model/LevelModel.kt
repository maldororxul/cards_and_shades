package com.example.cardsandshades.model

data class LevelModel(
    val id: Int,
    val name: String,
    val opponentName: String,
    val opponentMaxHp: Int,
    val opponentStartMana: Int,
    val difficultyDescription: String,
    val opponentDeckPreset: List<String>,
    val rewardGold: Int,
    val rewardCrystals: Int = 0,
    val rewardDustCommon: Int = 0,
    val rewardDustRare: Int = 0,
    val rewardDustEpic: Int = 0,
    val rewardDustLegendary: Int = 0,
    val rewardCardName: String? = null
)

data class ChapterModel(
    val id: Int,
    val name: String,
    val levels: List<LevelModel>
)
