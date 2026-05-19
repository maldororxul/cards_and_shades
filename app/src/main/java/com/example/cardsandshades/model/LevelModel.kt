package com.example.cardsandshades.model

data class LevelModel(
    val id: Int,
    val name: String,
    val opponentName: String,
    val opponentMaxHp: Int,
    val opponentStartMana: Int, // Для усложнения ИИ на поздних этапах
    val difficultyDescription: String
)
