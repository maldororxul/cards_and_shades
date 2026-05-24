package com.example.cardsandshades.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.cardsandshades.catalog.CardCatalog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.yaml.snakeyaml.Yaml
import java.util.UUID

object UserProfile {
    val gold = MutableStateFlow(50)
    val crystals = MutableStateFlow(10)
    
    // Используем SnapshotStateList для гарантированной реактивности в Compose
    val collection: SnapshotStateList<CardModel> = mutableStateListOf()
    val selectedDeck: SnapshotStateList<CardModel> = mutableStateListOf()
    
    val maxUnlockedLevel = MutableStateFlow(1)

    // ПОРОШОК ДЛЯ КРАФТА (по редкостям)
    val dustCommon = MutableStateFlow(0)
    val dustRare = MutableStateFlow(0)
    val dustEpic = MutableStateFlow(0)
    val dustLegendary = MutableStateFlow(0)
    val dustMythic = MutableStateFlow(0)

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
                gold.value = prefs.getInt("gold", 50)
                crystals.value = prefs.getInt("crystals", 0)
                maxUnlockedLevel.value = prefs.getInt("maxUnlockedLevel", 1)
                
                dustCommon.value = prefs.getInt("dustCommon", 0)
                dustRare.value = prefs.getInt("dustRare", 0)
                dustEpic.value = prefs.getInt("dustEpic", 0)
                dustLegendary.value = prefs.getInt("dustLegendary", 0)
                dustMythic.value = prefs.getInt("dustMythic", 0)

                loginChainDays.value = prefs.getInt("loginChainDays", 1)
                lastLoginTimestamp.value = prefs.getLong("lastLoginTimestamp", 0L)
                val claimedJson = prefs.getString("rewardsClaimed", "[]") ?: "[]"
                rewardsClaimed.value = gson.fromJson(claimedJson, object : com.google.gson.reflect.TypeToken<Set<Int>>() {}.type) ?: emptySet()

                // ЛОГИКА ДНЕВНОГО ЗАХОДА (Сброс в 6:00 утра)
                val now = System.currentTimeMillis()
                val lastLogin = lastLoginTimestamp.value
                
                if (lastLogin > 0) {
                    val offset = 6 * 60 * 60 * 1000L // 6 часов утра
                    val gameDayNow = (now - offset) / (24 * 60 * 60 * 1000L)
                    val gameDayLast = (lastLogin - offset) / (24 * 60 * 60 * 1000L)
                    
                    if (gameDayNow > gameDayLast) {
                        if (gameDayNow == gameDayLast + 1) {
                            // Следующий день — продолжаем цепочку
                            var nextDay = loginChainDays.value + 1
                            if (nextDay > 30) {
                                nextDay = 1
                                rewardsClaimed.value = emptySet()
                            }
                            loginChainDays.value = nextDay
                        } else {
                            // Пропустили день — сброс
                            loginChainDays.value = 1
                            rewardsClaimed.value = emptySet()
                        }
                    }
                }
                lastLoginTimestamp.value = now

                val collectionJson = prefs.getString("collection", "[]") ?: "[]"
                val deckJson = prefs.getString("deck", "[]") ?: "[]"
                val achJson = prefs.getString("achievements", "[]") ?: "[]"

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(deckJson, listType) ?: emptyList()
                
                val achStates: List<AchievementState> = gson.fromJson(achJson, object : com.google.gson.reflect.TypeToken<List<AchievementState>>() {}.type) ?: emptyList()
                AchievementManager.loadStates(achStates)

                // Обновляем SnapshotStateList в главном потоке для безопасности
                launch(Dispatchers.Main) {
                    collection.clear()
                    collection.addAll(loadedCollection)

                    selectedDeck.clear()
                    selectedDeck.addAll(loadedDeck)
                    val hasIllegalDuplicates = loadedDeck.groupBy { it.name }.any { it.value.size > 2 }
                    if (hasIllegalDuplicates || loadedDeck.size != 20) {
                        selectedDeck.clear()
                    }
                }
            } else {
                // ПЕРВЫЙ ЗАПУСК: Загрузка из start_profile.yaml
                try {
                    val yaml = Yaml()
                    val inputStream = context.assets.open("start_profile.yaml")
                    val data: Map<String, Any> = yaml.load(inputStream)
                    
                    @Suppress("UNCHECKED_CAST")
                    val resources = data["starting_resources"] as Map<String, Int>
                    gold.value = resources["gold"] ?: 50
                    crystals.value = resources["crystals"] ?: 10
                    dustCommon.value = resources["dust_common"] ?: 0
                    dustRare.value = resources["dust_rare"] ?: 0
                    dustEpic.value = resources["dust_epic"] ?: 0
                    dustLegendary.value = resources["dust_legendary"] ?: 0
                    dustMythic.value = resources["dust_mythic"] ?: 0

                    @Suppress("UNCHECKED_CAST")
                    val starterDeckKeys = data["starter_deck"] as List<String>
                    val startCollection = starterDeckKeys.mapNotNull { key ->
                        CardCatalog.createCardInstance(key)
                    }

                    launch(Dispatchers.Main) {
                        collection.clear()
                        collection.addAll(startCollection)

                        selectedDeck.clear()
                        selectedDeck.addAll(startCollection.map { it.copy(id = UUID.randomUUID().toString()) })
                        
                        save(context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
                putInt("dustMythic", dustMythic.value)

                putInt("loginChainDays", loginChainDays.value)
                putLong("lastLoginTimestamp", lastLoginTimestamp.value)
                putString("rewardsClaimed", gson.toJson(rewardsClaimed.value))

                putString("collection", gson.toJson(collection.toList()))
                putString("deck", gson.toJson(selectedDeck.toList()))
                putString("achievements", gson.toJson(AchievementManager.getAllStates()))
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
                    Rarity.MYTHIC -> 500
                }
                when (rarity) {
                    Rarity.COMMON -> dustCommon.value += extras * dustAmount
                    Rarity.RARE -> dustRare.value += extras * dustAmount
                    Rarity.EPIC -> dustEpic.value += extras * dustAmount
                    Rarity.LEGENDARY -> dustLegendary.value += extras * dustAmount
                    Rarity.MYTHIC -> dustMythic.value += extras * dustAmount
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
            Rarity.MYTHIC -> 5000
        }
        val currentDust = when (rarity) {
            Rarity.COMMON -> dustCommon
            Rarity.RARE -> dustRare
            Rarity.EPIC -> dustEpic
            Rarity.LEGENDARY -> dustLegendary
            Rarity.MYTHIC -> dustMythic
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
            Rarity.LEGENDARY -> {
                if (dustLegendary.value >= 100) {
                    dustLegendary.value -= 100
                    dustMythic.value += 10
                    save(); true
                } else false
            }
            else -> false
        }
    }
}
