package com.example.cardsandshades.catalog

import com.example.cardsandshades.effect.CardEffect
import com.example.cardsandshades.effect.RushEffect
import com.example.cardsandshades.effect.SplashEffect
import com.example.cardsandshades.effect.TauntEffect
import com.example.cardsandshades.effect.RangedEffect
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import java.util.UUID

object CardCatalog {

    private val templates = listOf(
        // === COMMON ===
        CardTemplate("Тень-новобранец", 1, 1, 1, Rarity.COMMON),
        CardTemplate("Проворный бес", 2, 2, 2, Rarity.COMMON, listOf(RushEffect())), // Сразу в бой!
        CardTemplate("Каменный страж", 3, 1, 5, Rarity.COMMON, listOf(TauntEffect())), // Танк

        // === RARE ===
        CardTemplate("Огненный элементаль", 2, 3, 2, Rarity.RARE),
        CardTemplate("Эльфийский лучник", 3, 2, 3, Rarity.RARE, listOf(RangedEffect())), // Без ответки
        CardTemplate("Адепт тайной магии", 3, 4, 3, Rarity.RARE),

        // === EPIC ===
        CardTemplate("Оруженосец Света", 3, 3, 5, Rarity.EPIC, listOf(TauntEffect())), // Танк
        CardTemplate("Чародей Пустоты", 4, 3, 4, Rarity.EPIC, listOf(SplashEffect())), // Бьет по соседям

        // === LEGENDARY ===
        CardTemplate("Король Теней", 4, 6, 5, Rarity.LEGENDARY),
        CardTemplate("Дракон Пустоты", 7, 9, 8, Rarity.LEGENDARY, listOf(SplashEffect()))
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
            effects = template.effects
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
                    effects = randomTemplate.effects
                )
            )
        }
        deck.shuffle()
        return deck
    }
}

private data class CardTemplate(
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    val effects: List<CardEffect> = emptyList()
)
