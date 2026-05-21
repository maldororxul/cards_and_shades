package com.example.cardsandshades.catalog

import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import java.util.UUID
import com.example.cardsandshades.model.EffectTag

object CardCatalog {

    private val templates = listOf(
        // === COMMON ===
        CardTemplate("Тень-новобранец", 1, 1, 1, Rarity.COMMON),
        CardTemplate("Проворный бес", 2, 2, 2, Rarity.COMMON, listOf(EffectTag.RUSH)),
        CardTemplate("Каменный страж", 3, 1, 5, Rarity.COMMON, listOf(EffectTag.TAUNT)),

        // === RARE ===
        CardTemplate("Огненный элементаль", 2, 3, 2, Rarity.RARE),
        CardTemplate("Эльфийский лучник", 3, 2, 3, Rarity.RARE, listOf(EffectTag.RANGED)),
        CardTemplate("Адепт тайной магии", 3, 4, 3, Rarity.RARE),

        // === EPIC ===
        CardTemplate("Оруженосец Света", 3, 3, 5, Rarity.EPIC, listOf(EffectTag.TAUNT)),
        CardTemplate("Чародей Пустоты", 4, 3, 4, Rarity.EPIC, listOf(EffectTag.SPLASH)),
        CardTemplate("Вампир-аристократ", 3, 2, 3, Rarity.EPIC, listOf(EffectTag.LIFESTEAL)),

        // === LEGENDARY ===
        CardTemplate("Король Теней", 4, 6, 5, Rarity.LEGENDARY),
        CardTemplate("Дракон Пустоты", 7, 9, 8, Rarity.LEGENDARY, listOf(EffectTag.SPLASH)),
        CardTemplate("Теневой жнец", 5, 4, 4, Rarity.LEGENDARY, listOf(EffectTag.LIFESTEAL, EffectTag.RUSH)),
        CardTemplate("Дух-наставник", 2, 1, 1, Rarity.LEGENDARY, listOf(EffectTag.BUFF))
    )

    fun createCardInstance(templateName: String): CardModel? {
        val template = templates.find { it.name == templateName } ?: return null
        return CardModel(
            id = UUID.randomUUID().toString(),
            name = template.name,
            manaCost = template.manaCost,
            baseAttack = template.baseAttack,
            baseHealth = template.baseHealth,
            rarity = template.rarity,
            effectTags = template.effectTags
        )
    }

    fun generateTestDeck(): MutableList<CardModel> {
        val deck = mutableListOf<CardModel>()
        repeat(20) {
            val randomTemplate = templates.random()
            deck.add(
                CardModel(
                    id = UUID.randomUUID().toString(),
                    name = randomTemplate.name,
                    manaCost = randomTemplate.manaCost,
                    baseAttack = randomTemplate.baseAttack,
                    baseHealth = randomTemplate.baseHealth,
                    rarity = randomTemplate.rarity,
                    effectTags = randomTemplate.effectTags
                )
            )
        }
        deck.shuffle()
        return deck
    }

    // Открытие пака по математической модели вероятностей
    fun openBooster(): List<CardModel> {
        val booster = mutableListOf<CardModel>()

        // Генерируем первые 4 карты по стандартным шансам
        repeat(4) {
            booster.add(generateRandomCardByRarity())
        }

        // 5-я карта: гарантированно Rare, Epic или Legendary (защита от неудач)
        booster.add(generateGuaranteedRareOrHigher())

        return booster
    }

    private fun generateRandomCardByRarity(): CardModel {
        val roll = (1..100).random()
        val targetRarity = when {
            roll <= 2 -> Rarity.LEGENDARY // 2%
            roll <= 10 -> Rarity.EPIC     // 8%
            roll <= 30 -> Rarity.RARE     // 20%
            else -> Rarity.COMMON         // 70%
        }

        val filteredTemplates = templates.filter { it.rarity == targetRarity }
        val template = if (filteredTemplates.isNotEmpty()) filteredTemplates.random() else templates.random()
        return createCardInstance(template.name)!!
    }

    private fun generateGuaranteedRareOrHigher(): CardModel {
        val roll = (1..100).random()
        val targetRarity = when {
            roll <= 5 -> Rarity.LEGENDARY  // 5%
            roll <= 25 -> Rarity.EPIC      // 20%
            else -> Rarity.RARE            // 75%
        }
        val filteredTemplates = templates.filter { it.rarity == targetRarity }
        val template = if (filteredTemplates.isNotEmpty()) filteredTemplates.random() else templates.random()
        return createCardInstance(template.name)!!
    }

}

private data class CardTemplate(
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    val effectTags: List<EffectTag> = emptyList()
)
