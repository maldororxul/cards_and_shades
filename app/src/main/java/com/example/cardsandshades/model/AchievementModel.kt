package com.example.cardsandshades.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AchievementState(
    val groupId: String,
    var currentTierIndex: Int = 0,
    var progressValue: Int = 0,
    val claimedTiers: MutableSet<Int> = mutableSetOf()
)

object AchievementManager {
    private val _states = mutableMapOf<String, AchievementState>()
    
    fun getState(groupId: String): AchievementState {
        return _states.getOrPut(groupId) { AchievementState(groupId) }
    }

    fun updateProgress(type: com.example.cardsandshades.catalog.AchievementType, value: Int, isAbsolute: Boolean = false) {
        com.example.cardsandshades.catalog.AchievementCatalog.groups.filter { it.type == type }.forEach { group ->
            val state = getState(group.id)
            if (isAbsolute) {
                state.progressValue = value
            } else {
                state.progressValue += value
            }
        }
    }
    
    // Для сохранения/загрузки
    fun getAllStates() = _states.values.toList()
    fun loadStates(list: List<AchievementState>) {
        _states.clear()
        list.forEach { _states[it.groupId] = it }
    }
}
