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
                selectedDeck.notifyChanges()
            } else {
                // Первый старт: генерируем начальный стартовый набор (30 карт)
                repeat(30) {
                    com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let {
                        collection.add(it)
                    }
                }
                collection.notifyChanges()

                // Автоматически собираем первую деку из первых 20 карт для фолбэка
                val initialDeck = collection.take(20)
                if (initialDeck.size == 20) {
                    selectedDeck.addAll(initialDeck)
                    selectedDeck.notifyChanges()
                }

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
