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

    private const val PREFS_NAME = "cards_and_shades_prefs"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    fun initDatabase(context: Context) {
        scope.launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            if (prefs.contains("gold")) {
                gold.value = prefs.getInt("gold", 500)
                maxUnlockedLevel.value = prefs.getInt("maxUnlockedLevel", 1)

                val collectionJson = prefs.getString("collection", "[]") ?: "[]"
                val deckJson = prefs.getString("deck", "[]") ?: "[]"

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(deckJson, listType) ?: emptyList()

                collection.clear()
                collection.addAll(loadedCollection)
                collection.notifyChanges()

                selectedDeck.clear()
                selectedDeck.addAll(loadedDeck)
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
                // Чтобы не менять CardCatalog, просто даем игроку большой пул карт (80 штук),
                // среди которых точно гарантированно будут все копии для сборки.
                repeat(80) {
                    com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let {
                        startCollection.add(it)
                    }
                }

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

    fun save(context: Context) {
        scope.launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt("gold", gold.value)
                putInt("maxUnlockedLevel", maxUnlockedLevel.value)
                putString("collection", gson.toJson(collection.toList()))
                putString("deck", gson.toJson(selectedDeck.toList()))
                apply()
            }
        }
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
