package com.example.cardsandshades.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.catalog.MissionCatalog
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
    
    val collection: SnapshotStateList<CardModel> = mutableStateListOf()
    val selectedDeck: SnapshotStateList<CardModel> = mutableStateListOf()
    
    val maxUnlockedLevel = MutableStateFlow(1)

    // ПОРОШОК ДЛЯ КРАФТА
    val dustCommon = MutableStateFlow(0)
    val dustUncommon = MutableStateFlow(0)
    val dustRare = MutableStateFlow(0)
    val dustEpic = MutableStateFlow(0)
    val dustLegendary = MutableStateFlow(0)
    val dustMythic = MutableStateFlow(0)

    // МОЛОТЫ (НОВЫЙ РЕСУРС)
    val hammerCommon = MutableStateFlow(0)
    val hammerUncommon = MutableStateFlow(0)
    val hammerRare = MutableStateFlow(0)
    val hammerEpic = MutableStateFlow(0)
    val hammerLegendary = MutableStateFlow(0)
    val hammerMythic = MutableStateFlow(0)

    // ДНЕВНЫЕ НАГРАДЫ (СТАРАЯ СИСТЕМА - Оставляем как есть для совместимости или мигрируем)
    val loginChainDays = MutableStateFlow(1)
    val lastClaimTimestamp = MutableStateFlow(0L)
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
                dustUncommon.value = prefs.getInt("dustUncommon", 0)
                dustRare.value = prefs.getInt("dustRare", 0)
                dustEpic.value = prefs.getInt("dustEpic", 0)
                dustLegendary.value = prefs.getInt("dustLegendary", 0)
                dustMythic.value = prefs.getInt("dustMythic", 0)

                hammerCommon.value = prefs.getInt("hammerCommon", 0)
                hammerUncommon.value = prefs.getInt("hammerUncommon", 0)
                hammerRare.value = prefs.getInt("hammerRare", 0)
                hammerEpic.value = prefs.getInt("hammerEpic", 0)
                hammerLegendary.value = prefs.getInt("hammerLegendary", 0)
                hammerMythic.value = prefs.getInt("hammerMythic", 0)

                loginChainDays.value = prefs.getInt("loginChainDays", 1)
                lastClaimTimestamp.value = prefs.getLong("lastClaimTimestamp", 0L)
                val claimedJson = prefs.getString("rewardsClaimed", "[]") ?: "[]"
                rewardsClaimed.value = gson.fromJson(claimedJson, object : com.google.gson.reflect.TypeToken<Set<Int>>() {}.type) ?: emptySet()

                val collectionJson = prefs.getString("collection", "[]") ?: "[]"
                val deckJson = prefs.getString("deck", "[]") ?: "[]"
                val achJson = prefs.getString("achievements", "[]") ?: "[]"

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(deckJson, listType) ?: emptyList()
                
                val achStates: List<AchievementState> = gson.fromJson(achJson, object : com.google.gson.reflect.TypeToken<List<AchievementState>>() {}.type) ?: emptyList()
                AchievementManager.loadStates(achStates)

                // ЗАГРУЗКА ЗАДАНИЙ
                val dailyMissionsJson = prefs.getString("dailyMissions", "[]") ?: "[]"
                val weeklyMissionsJson = prefs.getString("weeklyMissions", "[]") ?: "[]"
                val dailyMissionsList: List<MissionState> = gson.fromJson(dailyMissionsJson, object : com.google.gson.reflect.TypeToken<List<MissionState>>() {}.type) ?: emptyList()
                val weeklyMissionsList: List<MissionState> = gson.fromJson(weeklyMissionsJson, object : com.google.gson.reflect.TypeToken<List<MissionState>>() {}.type) ?: emptyList()
                
                MissionManager.dailyStates.clear()
                dailyMissionsList.forEach { MissionManager.dailyStates[it.id] = it }
                MissionManager.weeklyStates.clear()
                weeklyMissionsList.forEach { MissionManager.weeklyStates[it.id] = it }

                MissionManager.dailyPlaytimeSeconds = prefs.getLong("dailyPlaytime", 0L)
                val claimedPlaytimeJson = prefs.getString("claimedPlaytime", "[]") ?: "[]"
                val claimedPlaytime: Set<Int> = gson.fromJson(claimedPlaytimeJson, object : com.google.gson.reflect.TypeToken<Set<Int>>() {}.type) ?: emptySet()
                MissionManager.claimedPlaytimeRewards.clear()
                MissionManager.claimedPlaytimeRewards.addAll(claimedPlaytime)

                launch(Dispatchers.Main) {
                    collection.clear()
                    collection.addAll(loadedCollection)
                    selectedDeck.clear()
                    selectedDeck.addAll(loadedDeck)
                }
            } else {
                // ПЕРВЫЙ ЗАПУСК
                try {
                    val yaml = Yaml()
                    val inputStream = context.assets.open("start_profile.yaml")
                    val data: Map<String, Any> = yaml.load(inputStream)
                    
                    @Suppress("UNCHECKED_CAST")
                    val resources = data["starting_resources"] as Map<String, Int>
                    gold.value = resources["gold"] ?: 50
                    crystals.value = resources["crystals"] ?: 10
                    dustCommon.value = resources["dust_common"] ?: 0
                    dustUncommon.value = resources["dust_uncommon"] ?: 0
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
                putInt("dustUncommon", dustUncommon.value)
                putInt("dustRare", dustRare.value)
                putInt("dustEpic", dustEpic.value)
                putInt("dustLegendary", dustLegendary.value)
                putInt("dustMythic", dustMythic.value)

                putInt("hammerCommon", hammerCommon.value)
                putInt("hammerUncommon", hammerUncommon.value)
                putInt("hammerRare", hammerRare.value)
                putInt("hammerEpic", hammerEpic.value)
                putInt("hammerLegendary", hammerLegendary.value)
                putInt("hammerMythic", hammerMythic.value)

                putInt("loginChainDays", loginChainDays.value)
                putLong("lastClaimTimestamp", lastClaimTimestamp.value)
                putString("rewardsClaimed", gson.toJson(rewardsClaimed.value))

                putString("collection", gson.toJson(collection.toList()))
                putString("deck", gson.toJson(selectedDeck.toList()))
                putString("achievements", gson.toJson(AchievementManager.getAllStates()))

                putString("dailyMissions", gson.toJson(MissionManager.dailyStates.values.toList()))
                putString("weeklyMissions", gson.toJson(MissionManager.weeklyStates.values.toList()))
                putLong("dailyPlaytime", MissionManager.dailyPlaytimeSeconds)
                putString("claimedPlaytime", gson.toJson(MissionManager.claimedPlaytimeRewards))

                apply()
            }
        }
    }

    fun applyReward(reward: RewardSetModel) {
        gold.value += reward.gold
        crystals.value += reward.crystals
        dustCommon.value += reward.dustCommon
        dustUncommon.value += reward.dustUncommon
        dustRare.value += reward.dustRare
        dustEpic.value += reward.dustEpic
        dustLegendary.value += reward.dustLegendary
        dustMythic.value += reward.dustMythic
        hammerCommon.value += reward.hammerCommon
        hammerUncommon.value += reward.hammerUncommon
        hammerRare.value += reward.hammerRare
        hammerEpic.value += reward.hammerEpic
        hammerLegendary.value += reward.hammerLegendary
        hammerMythic.value += reward.hammerMythic
        if (reward.cardName != null) {
            CardCatalog.createCardInstance(reward.cardName)?.let { collection.add(it) }
        }
        save()
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
                    Rarity.UNCOMMON -> 10
                    Rarity.RARE -> 20
                    Rarity.EPIC -> 50
                    Rarity.LEGENDARY -> 100
                    Rarity.MYTHIC -> 500
                }
                when (rarity) {
                    Rarity.COMMON -> dustCommon.value += extras * dustAmount
                    Rarity.UNCOMMON -> dustUncommon.value += extras * dustAmount
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
            MissionManager.updateProgress("daily_disenchant", totalDusted, false)
            save()
        }
        return totalDusted
    }

    fun craftCard(rarity: Rarity): Boolean {
        val cost = when (rarity) {
            Rarity.COMMON -> 40
            Rarity.UNCOMMON -> 80
            Rarity.RARE -> 100
            Rarity.EPIC -> 400
            Rarity.LEGENDARY -> 1600
            Rarity.MYTHIC -> 5000
        }
        val currentDust = when (rarity) {
            Rarity.COMMON -> dustCommon
            Rarity.UNCOMMON -> dustUncommon
            Rarity.RARE -> dustRare
            Rarity.EPIC -> dustEpic
            Rarity.LEGENDARY -> dustLegendary
            Rarity.MYTHIC -> dustMythic
        }
        val currentHammer = when (rarity) {
            Rarity.COMMON -> hammerCommon
            Rarity.UNCOMMON -> hammerUncommon
            Rarity.RARE -> hammerRare
            Rarity.EPIC -> hammerEpic
            Rarity.LEGENDARY -> hammerLegendary
            Rarity.MYTHIC -> hammerMythic
        }
        
        if (currentDust.value >= cost && currentHammer.value >= 1) {
            val newCard = com.example.cardsandshades.catalog.CardCatalog.generateRandomCardByRarityOnly(rarity)
            if (newCard != null) {
                currentDust.value -= cost
                currentHammer.value -= 1
                collection.add(newCard)
                MissionManager.updateProgress("daily_craft", 1, false)
                if (rarity == Rarity.EPIC || rarity == Rarity.LEGENDARY || rarity == Rarity.MYTHIC) {
                    MissionManager.updateProgress("weekly_craft_epic", 1, true)
                }
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
                    dustUncommon.value += 10
                    save(); true
                } else false
            }
            Rarity.UNCOMMON -> {
                if (dustUncommon.value >= 100) {
                    dustUncommon.value -= 100
                    rareMerge(); true
                } else false
            }
            Rarity.RARE -> {
                if (dustRare.value >= 100) {
                    dustRare.value -= 100
                    epicMerge(); true
                } else false
            }
            Rarity.EPIC -> {
                if (dustEpic.value >= 100) {
                    dustEpic.value -= 100
                    legendaryMerge(); true
                } else false
            }
            Rarity.LEGENDARY -> {
                if (dustLegendary.value >= 100) {
                    dustLegendary.value -= 100
                    mythicMerge(); true
                } else false
            }
            else -> false
        }
    }

    private fun rareMerge() { dustRare.value += 10 }
    private fun epicMerge() { dustEpic.value += 10 }
    private fun legendaryMerge() { dustLegendary.value += 10 }
    private fun mythicMerge() { dustMythic.value += 10 }
}
