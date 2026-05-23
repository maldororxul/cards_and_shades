package com.example.cardsandshades.catalog

import android.content.Context
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.EffectTag
import com.example.cardsandshades.model.Rarity
import org.yaml.snakeyaml.Yaml
import java.util.UUID

object CardCatalog {

    private var _templates: List<CardTemplate> = emptyList()
    val templates: List<CardTemplate> get() = _templates

    fun init(context: Context) {
        val yaml = Yaml()
        val inputStream = context.assets.open("cards.yaml")
        val data: Map<String, Any> = yaml.load(inputStream)
        val cardsList = data["cards"] as List<Map<String, Any>>

        _templates = cardsList.map { cardMap ->
            CardTemplate(
                name = cardMap["name"] as String,
                manaCost = cardMap["manaCost"] as Int,
                baseAttack = cardMap["baseAttack"] as Int,
                baseHealth = cardMap["baseHealth"] as Int,
                rarity = Rarity.valueOf(cardMap["rarity"] as String),
                effectTags = (cardMap["effectTags"] as? List<String>)?.map { EffectTag.valueOf(it) } ?: emptyList()
            )
        }
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
            effectTags = template.effectTags
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
                    effectTags = randomTemplate.effectTags
                )
            )
        }
        deck.shuffle()
        return deck
    }

    fun generateRandomCardByRarityOnly(rarity: Rarity): CardModel? {
        val filteredTemplates = templates.filter { it.rarity == rarity }
        val template = if (filteredTemplates.isNotEmpty()) filteredTemplates.random() else templates.randomOrNull()
        return if (template != null) createCardInstance(template.name) else null
    }

    // ОТКРЫТИЕ ПАКА: 5 карт, гарантированная редкая или выше
    fun openBooster(): List<CardModel> {
        val booster = mutableListOf<CardModel>()
        if (templates.isEmpty()) return booster

        repeat(4) {
            val roll = (1..100).random()
            val targetRarity = when {
                roll <= 2 -> Rarity.LEGENDARY // 2%
                roll <= 10 -> Rarity.EPIC     // 8%
                roll <= 30 -> Rarity.RARE     // 20%
                else -> Rarity.COMMON         // 70%
            }
            booster.add(generateRandomCardByRarityOnly(targetRarity) ?: createCardInstance("card_shadow_recruit")!!)
        }

        // 5-я карта: гарантированно Rare+
        val roll = (1..100).random()
        val highRarity = when {
            roll <= 5 -> Rarity.LEGENDARY  // 5%
            roll <= 25 -> Rarity.EPIC      // 20%
            else -> Rarity.RARE            // 75%
        }
        booster.add(generateRandomCardByRarityOnly(highRarity) ?: generateRandomCardByRarityOnly(Rarity.RARE)!!)

        return booster
    }
}

data class CardTemplate(
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    val effectTags: List<EffectTag> = emptyList()
)
