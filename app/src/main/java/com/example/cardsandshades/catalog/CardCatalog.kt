package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.EffectTag
import com.example.cardsandshades.model.Rarity
import org.yaml.snakeyaml.Yaml
import java.util.UUID

object CardCatalog {

    private var templates: List<CardTemplate> = emptyList()

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("cards.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val cardsList = data["cards"] as List<Map<String, Any>>

        templates = cardsList.map { cardMap ->
            CardTemplate(
                name = cardMap["name"] as String,
                manaCost = cardMap["manaCost"] as Int,
                baseAttack = cardMap["baseAttack"] as Int,
                baseHealth = cardMap["baseHealth"] as Int,
                rarity = Rarity.valueOf(cardMap["rarity"] as String),
                effectTags = (cardMap["effectTags"] as? List<String>)?.map { EffectTag.valueOf(it) } ?: emptyList(),
                imageResName = cardMap["imageResName"] as? String
            )
        }
    }

    fun getVisualData(cardName: String): String? {
        val template = templates.find { it.name == cardName }
        return template?.imageResName
    }

    fun createCardInstance(templateName: String): CardModel? {
        val template = templates.find { it.name == templateName } ?: return null
        return CardModel(
            id = UUID.randomUUID().toString(),
            name = template.name,
            manaCost = template.manaCost,
            baseAttack = template.baseAttack,
            baseHealth = template.baseHealth,
            rarity = template.rarity,
            effectTags = template.effectTags,
            imageResName = template.imageResName
        )
    }

    fun generateTestDeck(): MutableList<CardModel> {
        val deck = mutableListOf<CardModel>()
        if (templates.isEmpty()) return deck
        
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
                    effectTags = randomTemplate.effectTags,
                    imageResName = randomTemplate.imageResName
                )
            )
        }
        deck.shuffle()
        return deck
    }

    // Открытие пака по математической модели вероятностей
    fun openBooster(): List<CardModel> {
        val booster = mutableListOf<CardModel>()
        if (templates.isEmpty()) return booster

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
    val effectTags: List<EffectTag> = emptyList(),
    val imageResName: String? = null
)
