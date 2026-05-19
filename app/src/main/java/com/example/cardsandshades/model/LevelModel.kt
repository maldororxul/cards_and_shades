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
    val rewardCardName: String?
)
