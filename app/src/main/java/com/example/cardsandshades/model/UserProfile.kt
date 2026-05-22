package com.example.cardsandshades.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object UserProfile {
    val gold = MutableStateFlow(500)
    val crystals = MutableStateFlow(0)
    
    // Используем SnapshotStateList для гарантированной реактивности в Compose
    val collection: SnapshotStateList<CardModel> = mutableStateListOf()
    val selectedDeck: SnapshotStateList<CardModel> = mutableStateListOf()
    
    val maxUnlockedLevel = MutableStateFlow(1)

    // ПОРОШОК ДЛЯ КРАФТА (по редкостям)
    val dustCommon = MutableStateFlow(0)
    val dustRare = MutableStateFlow(0)
    val dustEpic = MutableStateFlow(0)
    val dustLegendary = MutableStateFlow(0)

    // ДНЕВНЫЕ НАГРАДЫ
    val loginChainDays = MutableStateFlow(1)
    val lastLoginTimestamp = MutableStateFlow(0L)
    val rewardsClaimed = MutableStateFlow<Set<Int>>(emptySet())

    private const val PREFS_NAME = "cards_and_shades_prefs"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    private var appContext: Context? = null

    fun initDatabase(context: Context) {
        appContext = context.applicationContext
        scope.launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            if (prefs.contains("gold")) {
                gold.value = prefs.getInt("gold", 500)
                crystals.value = prefs.getInt("crystals", 0)
                maxUnlockedLevel.value = prefs.getInt("maxUnlockedLevel", 1)
                
                dustCommon.value = prefs.getInt("dustCommon", 0)
                dustRare.value = prefs.getInt("dustRare", 0)
                dustEpic.value = prefs.getInt("dustEpic", 0)
                dustLegendary.value = prefs.getInt("dustLegendary", 0)

                loginChainDays.value = prefs.getInt("loginChainDays", 1)
                lastLoginTimestamp.value = prefs.getLong("lastLoginTimestamp", 0L)
                val claimedJson = prefs.getString("rewardsClaimed", "[]") ?: "[]"
                rewardsClaimed.value = gson.fromJson(claimedJson, object : com.google.gson.reflect.TypeToken<Set<Int>>() {}.type) ?: emptySet()

                // ЛОГИКА ДНЕВНОГО ЗАХОДА
                val now = System.currentTimeMillis()
                val lastLogin = lastLoginTimestamp.value
                
                if (lastLogin > 0) {
                    val diff = now - lastLogin
                    val oneDayMs = 24 * 60 * 60 * 1000L
                    
                    if (diff > oneDayMs) {
                        if (diff < 2 * oneDayMs) {
                            var nextDay = loginChainDays.value + 1
                            if (nextDay > 30) nextDay = 1
                            loginChainDays.value = nextDay
                        } else {
                            loginChainDays.value = 1
                            rewardsClaimed.value = emptySet()
                        }
                    }
                }
                lastLoginTimestamp.value = now

                val collectionJson = prefs.getString("collection", "[]") ?: "[]"
                val deckJson = prefs.getString("deck", "[]") ?: "[]"

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(deckJson, listType) ?: emptyList()

                val rehydratedCollection = loadedCollection.map { card ->
                    if (card.imageResName == null) {
                        val resName = com.example.cardsandshades.catalog.CardCatalog.getVisualData(card.name)
                        card.copy(imageResName = resName)
                    } else card
                }
                
                val rehydratedDeck = loadedDeck.map { card ->
                    if (card.imageResName == null) {
                        val resName = com.example.cardsandshades.catalog.CardCatalog.getVisualData(card.name)
                        card.copy(imageResName = resName)
                    } else card
                }

                // Обновляем SnapshotStateList в главном потоке для безопасности
                launch(Dispatchers.Main) {
                    collection.clear()
                    collection.addAll(rehydratedCollection)

                    selectedDeck.clear()
                    selectedDeck.addAll(rehydratedDeck)
                    val hasIllegalDuplicates = loadedDeck.groupBy { it.name }.any { it.value.size > 2 }
                    if (hasIllegalDuplicates || loadedDeck.size != 20) {
                        selectedDeck.clear()
                    }
                }
            } else {
                val startCollection = mutableListOf<CardModel>()
                repeat(100) {
                    com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let {
                        startCollection.add(it)
                    }
                }
                
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("card_vampire_aristocrat")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("card_vampire_aristocrat")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("card_spirit_mentor")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("card_shadow_reaper")?.let { startCollection.add(it) }

                val validStartDeck = mutableListOf<CardModel>()
                for (card in startCollection) {
                    if (validStartDeck.size < 20) {
                        val countInDeck = validStartDeck.count { it.name == card.name }
                        if (countInDeck < 2) {
                            validStartDeck.add(card.copy(id = java.util.UUID.randomUUID().toString()))
                        }
                    } else break
                }

                launch(Dispatchers.Main) {
                    collection.clear()
                    collection.addAll(startCollection)

                    selectedDeck.clear()
                    selectedDeck.addAll(validStartDeck)
                    
                    save()
                }
            }
        }
    }

    fun save(context: Context? = null) {
        val targetContext = context?.applicationContext ?: appContext ?: return
        scope.launch {
            val prefs = targetContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt("gold", gold.value)
                putInt("crystals", crystals.value)
                putInt("maxUnlockedLevel", maxUnlockedLevel.value)
                
                putInt("dustCommon", dustCommon.value)
                putInt("dustRare", dustRare.value)
                putInt("dustEpic", dustEpic.value)
                putInt("dustLegendary", dustLegendary.value)

                putInt("loginChainDays", loginChainDays.value)
                putLong("lastLoginTimestamp", lastLoginTimestamp.value)
                putString("rewardsClaimed", gson.toJson(rewardsClaimed.value))

                putString("collection", gson.toJson(collection.toList()))
                putString("deck", gson.toJson(selectedDeck.toList()))
                apply()
            }
        }
    }

    fun dustExtras(): Int {
        val grouped = collection.groupBy { it.name }
        var totalDusted = 0
        val newCollection = mutableListOf<CardModel>()
        
        grouped.forEach { (name, cards) ->
            if (cards.size > 2) {
                val extras = cards.size - 2
                totalDusted += extras
                val rarity = cards.first().rarity
                val dustAmount = when (rarity) {
                    Rarity.COMMON -> 5
                    Rarity.RARE -> 20
                    Rarity.EPIC -> 50
                    Rarity.LEGENDARY -> 100
                }
                when (rarity) {
                    Rarity.COMMON -> dustCommon.value += extras * dustAmount
                    Rarity.RARE -> dustRare.value += extras * dustAmount
                    Rarity.EPIC -> dustEpic.value += extras * dustAmount
                    Rarity.LEGENDARY -> dustLegendary.value += extras * dustAmount
                }
                newCollection.addAll(cards.take(2))
            } else newCollection.addAll(cards)
        }
        
        if (totalDusted > 0) {
            collection.clear()
            collection.addAll(newCollection)
            save()
        }
        return totalDusted
    }

    fun craftCard(rarity: Rarity): Boolean {
        val cost = when (rarity) {
            Rarity.COMMON -> 40
            Rarity.RARE -> 100
            Rarity.EPIC -> 400
            Rarity.LEGENDARY -> 1600
        }
        val currentDust = when (rarity) {
            Rarity.COMMON -> dustCommon
            Rarity.RARE -> dustRare
            Rarity.EPIC -> dustEpic
            Rarity.LEGENDARY -> dustLegendary
        }
        
        if (currentDust.value >= cost) {
            val newCard = com.example.cardsandshades.catalog.CardCatalog.generateRandomCardByRarityOnly(rarity)
            if (newCard != null) {
                currentDust.value -= cost
                collection.add(newCard)
                save()
                return true
            }
        }
        return false
    }

    fun mergeDust(from: Rarity): Boolean {
        return when (from) {
            Rarity.COMMON -> {
                if (dustCommon.value >= 100) {
                    dustCommon.value -= 100
                    dustRare.value += 10
                    save(); true
                } else false
            }
            Rarity.RARE -> {
                if (dustRare.value >= 100) {
                    dustRare.value -= 100
                    dustEpic.value += 10
                    save(); true
                } else false
            }
            Rarity.EPIC -> {
                if (dustEpic.value >= 100) {
                    dustEpic.value -= 100
                    dustLegendary.value += 10
                    save(); true
                } else false
            }
            else -> false
        }
    }
}
