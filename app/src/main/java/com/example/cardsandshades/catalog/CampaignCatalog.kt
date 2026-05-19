package com.example.cardsandshades.catalog

import com.example.cardsandshades.model.LevelModel


object CampaignCatalog {
    val levels = listOf(
        LevelModel(
            id = 1,
            name = "Пролог: Забытые земли",
            opponentName = "Гоблин-разведчик",
            opponentMaxHp = 15,
            opponentStartMana = 1,
            difficultyDescription = "Простой бой для ознакомления"
        ),
        LevelModel(
            id = 2,
            name = "Глава 1: Проклятый лес",
            opponentName = "Лесной Колдун",
            opponentMaxHp = 25,
            opponentStartMana = 1,
            difficultyDescription = "Враг использует редкие карты"
        ),
        LevelModel(
            id = 3,
            name = "Глава 2: Старая цитадель",
            opponentName = "Рыцарь смерти",
            opponentMaxHp = 35,
            opponentStartMana = 2, // Начинает сразу с 2 маны!
            difficultyDescription = "Высокая сложность, сильный старт врага"
        ),
        LevelModel(
            id = 4,
            name = "Финал: Трон Теней",
            opponentName = "Архимаг Маллок",
            opponentMaxHp = 50,
            opponentStartMana = 3,
            difficultyDescription = "Босс игры. Максимальная сложность"
        )
    )
}