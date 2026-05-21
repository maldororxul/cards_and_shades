package com.example.cardsandshades.model

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object UserProfile {
    val gold = MutableStateFlow(500)
    val collection = MutableListFlow(mutableListOf<CardModel>())
    val selectedDeck = MutableListFlow(mutableListOf<CardModel>()) // Теперь тоже реактивный MutableListFlow
    val maxUnlockedLevel = MutableStateFlow(1)

    // ПОРОШОК ДЛЯ КРАФТА (по редкостям)
    val dustCommon = MutableStateFlow(0)
    val dustRare = MutableStateFlow(0)
    val dustEpic = MutableStateFlow(0)
    val dustLegendary = MutableStateFlow(0)

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
                maxUnlockedLevel.value = prefs.getInt("maxUnlockedLevel", 1)
                
                dustCommon.value = prefs.getInt("dustCommon", 0)
                dustRare.value = prefs.getInt("dustRare", 0)
                dustEpic.value = prefs.getInt("dustEpic", 0)
                dustLegendary.value = prefs.getInt("dustLegendary", 0)

                val collectionJson = prefs.getString("collection", "[]") ?: "[]"
                val deckJson = prefs.getString("deck", "[]") ?: "[]"

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(deckJson, listType) ?: emptyList()

                // ИСПРАВЛЕНИЕ: Восстанавливаем ссылки на картинки для карт из старого кэша
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

                collection.clear()
                collection.addAll(rehydratedCollection)
                collection.notifyChanges()

                selectedDeck.clear()
                selectedDeck.addAll(rehydratedDeck)
                val hasIllegalDuplicates = loadedDeck.groupBy { it.name }.any { it.value.size > 2 }
                if (hasIllegalDuplicates || loadedDeck.size != 20) {
                    // Если колода сломана кэшем, принудительно очищаем её для безопасного рендера 0/2
                    selectedDeck.clear()
                }
                selectedDeck.notifyChanges()
            } else {
                // ИСПРАВЛЕНИЕ: Выдаем базовый набор ККИ — гарантированно по 2 копии каждой карты из каталога
                val startCollection = mutableListOf<CardModel>()

                // Нам нужен доступ к шаблонам карт. Используем трюк: генерируем деку, чтобы вытащить шаблоны,
                // либо наполняем коллекцию гарантированным набором через генератор.
                // Чтобы не менять CardCatalog, просто даем игроку большой пул карт (100 штук),
                // среди которых точно гарантированно будут все копии для сборки.
                repeat(100) {
                    com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let {
                        startCollection.add(it)
                    }
                }
                
                // Гарантированно добавляем новые механики в коллекцию для теста
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("Вампир-аристократ")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("Вампир-аристократ")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("Дух-наставник")?.let { startCollection.add(it) }
                com.example.cardsandshades.catalog.CardCatalog.createCardInstance("Теневой жнец")?.let { startCollection.add(it) }

                collection.clear()
                collection.addAll(startCollection)
                collection.notifyChanges()

                // Автоматически собираем первую легальную деку из 20 карт (строго по 2 копии максимум)
                val validStartDeck = mutableListOf<CardModel>()
                for (card in startCollection) {
                    if (validStartDeck.size < 20) {
                        val countInDeck = validStartDeck.count { it.name == card.name }
                        if (countInDeck < 2) {
                            validStartDeck.add(card.copy(id = java.util.UUID.randomUUID().toString()))
                        }
                    } else {
                        break
                    }
                }

                selectedDeck.clear()
                selectedDeck.addAll(validStartDeck)
                selectedDeck.notifyChanges()

                save(context)
            }
        }
    }

    fun save(context: Context? = null) {
        val targetContext = context?.applicationContext ?: appContext ?: return
        scope.launch {
            val prefs = targetContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt("gold", gold.value)
                putInt("maxUnlockedLevel", maxUnlockedLevel.value)
                
                putInt("dustCommon", dustCommon.value)
                putInt("dustRare", dustRare.value)
                putInt("dustEpic", dustEpic.value)
                putInt("dustLegendary", dustLegendary.value)

                putString("collection", gson.toJson(collection.toList()))
                putString("deck", gson.toJson(selectedDeck.toList()))
                apply()
            }
        }
    }

    // РАСПЫЛЕНИЕ ЛИШНИХ КАРТ (более 2-х копий одного типа)
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
            } else {
                newCollection.addAll(cards)
            }
        }
        
        if (totalDusted > 0) {
            collection.clear()
            collection.addAll(newCollection)
            collection.notifyChanges()
            save()
        }
        
        return totalDusted
    }
}


// Вспомогательный класс для реактивного обновления списков в Compose
class MutableListFlow<T>(private val delegate: MutableList<T>) : MutableList<T> by delegate {
    private val stateFlow = MutableStateFlow(delegate.toList())
    val status = stateFlow
    fun notifyChanges() {
        stateFlow.value = delegate.toList()
    }
}
