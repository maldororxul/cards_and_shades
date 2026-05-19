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
            difficultyDescription = "Простой бой. Враг спамит мелкими существами.",
            opponentDeckPreset = listOf(
                "Тень-новобранец", "Тень-новобранец", "Тень-новобранец", "Тень-новобранец",
                "Проворный бес", "Проворный бес", "Проворный бес", "Проворный бес"
            ),
            rewardGold = 100, // Хватит на целый бустер!
            rewardCardName = "Проворный бес"
        ),
        LevelModel(
            id = 2,
            name = "Глава 1: Проклятый лес",
            opponentName = "Лесной Колдун",
            opponentMaxHp = 25,
            opponentStartMana = 1,
            difficultyDescription = "Враг использует Провокаторов и дальнобойных Стрелков.",
            opponentDeckPreset = listOf(
                "Каменный страж", "Каменный страж", "Эльфийский лучник", "Эльфийский лучник"
            ),
            rewardGold = 150,
            rewardCardName = "Эльфийский лучник"
        ),
        LevelModel(
            id = 3,
            name = "Глава 2: Старая цитадель",
            opponentName = "Рыцарь смерти",
            opponentMaxHp = 35,
            opponentStartMana = 2,
            difficultyDescription = "Высокая сложность. Сильные Эпические существа.",
            opponentDeckPreset = listOf(
                "Оруженосец Света", "Оруженосец Света", "Чародей Пустоты", "Чародей Пустоты"
            ),
            rewardGold = 200,
            rewardCardName = "Чародей Пустоты"
        ),
        LevelModel(
            id = 4,
            name = "Финал: Трон Теней",
            opponentName = "Архимаг Маллок",
            opponentMaxHp = 50,
            opponentStartMana = 3,
            difficultyDescription = "Босс игры. Мощные Легендарные драконы и АОЕ-магия.",
            opponentDeckPreset = listOf(
                "Король Теней", "Король Теней", "Дракон Пустоты", "Дракон Пустоты"
            ),
            rewardGold = 500,
            rewardCardName = "Дракон Пустоты" // Уникальная легендарка за финал!
        )
    )
}