package com.example.cardsandshades.model

data class LevelModel(
    val id: Int,
    val name: String,
    val opponentName: String,
    val opponentMaxHp: Int,
    val opponentStartMana: Int,
    val difficultyDescription: String,
    val opponentDeckPreset: List<String>,
    val firstTimeReward: RewardSetModel,
    val repeatReward: RewardSetModel,
    val backgroundRes: String? = null
)

data class ChapterModel(
    val id: Int,
    val name: String,
    val levels: List<LevelModel>,
    val backgroundRes: String? = null
)
