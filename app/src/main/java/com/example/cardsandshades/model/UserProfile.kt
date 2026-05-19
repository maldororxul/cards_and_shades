package com.example.cardsandshades.model

import kotlinx.coroutines.flow.MutableStateFlow

object UserProfile {
    val gold = MutableStateFlow(500) // Стартовый капитал игрока
    val collection = MutableListFlow(mutableListOf<CardModel>()) // Вся коллекция карт игрока
    var maxUnlockedLevel = 1 // Текущий прогресс в кампании
}

// Вспомогательный класс для реактивного обновления списков в Compose
class MutableListFlow<T>(private val delegate: MutableList<T>) : MutableList<T> by delegate {
    private val stateFlow = MutableStateFlow(delegate.toList())
    val status = stateFlow
    fun notifyChanges() {
        stateFlow.value = delegate.toList()
    }
}
