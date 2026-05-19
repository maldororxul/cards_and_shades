package com.example.cardsandshades.model

import kotlinx.coroutines.flow.MutableStateFlow

object UserProfile {
    val gold = MutableStateFlow(500)
    // Инициализируем коллекцию стартовым набором из 15 случайных карт для первой сборки
    val collection = MutableListFlow(mutableListOf<CardModel>().apply {
        repeat(15) {
            com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let { add(it) }
        }
    })
    // Сюда запишется готовая колода из 20 карт
    val selectedDeck = mutableListOf<CardModel>()

    var maxUnlockedLevel = 1
}

// Вспомогательный класс для реактивного обновления списков в Compose
class MutableListFlow<T>(private val delegate: MutableList<T>) : MutableList<T> by delegate {
    private val stateFlow = MutableStateFlow(delegate.toList())
    val status = stateFlow
    fun notifyChanges() {
        stateFlow.value = delegate.toList()
    }
}
