package com.example.cardsandshades.catalog

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import java.util.UUID

object CardCatalog {

    // Базовые шаблоны карт (коллекция игры)
    private val templates = listOf(
        // === COMMON (Мана * 2) ===
        CardTemplate("Тень-новобранец", 1, 1, 1, Rarity.COMMON),
        CardTemplate("Проворный бес", 2, 2, 2, Rarity.COMMON),
        CardTemplate("Каменный страж", 3, 1, 5, Rarity.COMMON),
        CardTemplate("Грифон-вестник", 4, 4, 4, Rarity.COMMON),

        // === RARE (Мана * 2 + 1) ===
        CardTemplate("Огненный элементаль", 2, 3, 2, Rarity.RARE),
        CardTemplate("Адепт тайной магии", 3, 4, 3, Rarity.RARE),
        CardTemplate("Вестник заката", 5, 5, 6, Rarity.RARE),

        // === EPIC (Мана * 2 + 2) ===
        CardTemplate("Оруженосец Света", 3, 3, 5, Rarity.EPIC),
        CardTemplate("Древний энт", 6, 6, 8, Rarity.EPIC),

        // === LEGENDARY (Мана * 2 + 3) ===
        CardTemplate("Король Теней", 4, 6, 5, Rarity.LEGENDARY),
        CardTemplate("Дракон Пустоты", 7, 9, 8, Rarity.LEGENDARY)
    )

    // Функция генерации уникального экземпляра карты для колоды
    fun createCardInstance(templateName: String): CardModel? {
        val template = templates.find { it.name == templateName } ?: return null
        return CardModel(
            id = UUID.randomUUID().toString(), // Каждой карте нужен свой ID в бою
            name = template.name,
            manaCost = template.manaCost,
            baseAttack = template.baseAttack,
            baseHealth = template.baseHealth,
            rarity = template.rarity
        )
    }

    // Создание стартовой тестовой колоды из 20 карт
    fun generateTestDeck(): MutableList<CardModel> {
        val deck = mutableListOf<CardModel>()

        // Наполняем колоду случайными картами из каталога для тестов
        repeat(20) {
            val randomTemplate = templates.random()
            deck.add(
                CardModel(
                    id = UUID.randomUUID().toString(),
                    name = randomTemplate.name,
                    manaCost = randomTemplate.manaCost,
                    baseAttack = randomTemplate.baseAttack,
                    baseHealth = randomTemplate.baseHealth,
                    rarity = randomTemplate.rarity
                )
            )
        }
        // Перемешиваем перед боем
        deck.shuffle()
        return deck
    }
}

// Вспомогательный класс для описания шаблонов в каталоге
private data class CardTemplate(
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity
)
