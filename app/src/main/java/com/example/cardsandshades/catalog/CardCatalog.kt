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
        @Suppress("UNCHECKED_CAST")
        val cardsList = data["cards"] as List<Map<String, Any>>

        _templates = cardsList.map { cardMap ->
            @Suppress("UNCHECKED_CAST")
            CardTemplate(
                name = cardMap["name"] as String,
                manaCost = cardMap["manaCost"] as Int,
                baseAttack = cardMap["baseAttack"] as Int,
                baseHealth = cardMap["baseHealth"] as Int,
                rarity = Rarity.valueOf(cardMap["rarity"] as String),
                effectTags = (cardMap["effectTags"] as? List<String>)?.map { EffectTag.valueOf(it) } ?: emptyList(),
                groupTags = (cardMap["groupTags"] as? List<String>)?.map { com.example.cardsandshades.model.GroupTag.valueOf(it) } ?: emptyList(),
                canDropFromBooster = cardMap["canDropFromBooster"] as? Boolean ?: true,
                canCraftFromDust = cardMap["canCraftFromDust"] as? Boolean ?: true,
                deckLimit = cardMap["deckLimit"] as? Int ?: 3,
                deathSound = cardMap["deathSound"] as? String ?: "card_death",
                playSound = cardMap["playSound"] as? String ?: "card_place",
                attackSound = cardMap["attackSound"] as? String ?: "attack",
                quotes = (cardMap["quotes"] as? List<String>) ?: emptyList()
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
            effectTags = template.effectTags,
            groupTags = template.groupTags,
            critMultiplier = if (template.effectTags.contains(EffectTag.CRIT)) 2.0f else 1.0f,
            limit = template.deckLimit,
            dSound = template.deathSound,
            pSound = template.playSound,
            aSound = template.attackSound,
            quoteList = template.quotes
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
                    groupTags = randomTemplate.groupTags,
                    limit = randomTemplate.deckLimit,
                    dSound = randomTemplate.deathSound,
                    pSound = randomTemplate.playSound,
                    aSound = randomTemplate.attackSound,
                    quoteList = randomTemplate.quotes
                )
            )
        }
        deck.shuffle()
        return deck
    }

    fun generateRandomCardByRarityOnly(rarity: Rarity): CardModel? {
        val filteredTemplates = templates.filter { it.rarity == rarity && it.canCraftFromDust }
        val template = if (filteredTemplates.isNotEmpty()) filteredTemplates.random() else templates.filter { it.canCraftFromDust }.randomOrNull()
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
                roll <= 25 -> Rarity.RARE     // 15%
                roll <= 50 -> Rarity.UNCOMMON // 25%
                else -> Rarity.COMMON         // 50%
            }
            val filtered = templates.filter { it.rarity == targetRarity && it.canDropFromBooster }
            val template = if (filtered.isNotEmpty()) filtered.random() else templates.filter { it.canDropFromBooster }.random()
            booster.add(createCardInstance(template.name)!!)
        }

        // 5-я карта: гарантированно Rare+
        val roll = (1..100).random()
        val highRarity = when {
            roll <= 5 -> Rarity.LEGENDARY  // 5%
            roll <= 25 -> Rarity.EPIC      // 20%
            else -> Rarity.RARE            // 75%
        }
        val filteredHigh = templates.filter { it.rarity == highRarity && it.canDropFromBooster }
        val finalTemplate = if (filteredHigh.isNotEmpty()) filteredHigh.random() else templates.filter { it.canDropFromBooster && it.rarity == Rarity.RARE }.random()
        booster.add(createCardInstance(finalTemplate.name)!!)

        return booster
    }
}

data class CardTemplate(
    val name: String,
    val manaCost: Int,
    val baseAttack: Int,
    val baseHealth: Int,
    val rarity: Rarity,
    val effectTags: List<EffectTag> = emptyList(),
    val groupTags: List<com.example.cardsandshades.model.GroupTag> = emptyList(),
    val canDropFromBooster: Boolean = true,
    val canCraftFromDust: Boolean = true,
    val deckLimit: Int = 3,
    val deathSound: String = "card_death",
    val playSound: String = "card_place",
    val attackSound: String = "attack",
    val quotes: List<String> = emptyList()
)
