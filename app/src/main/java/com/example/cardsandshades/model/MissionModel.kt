package com.example.cardsandshades.model

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf

data class MissionModel(
    val id: String,
    val nameKey: String,
    val goal: Int,
    val reward: RewardSetModel
)

data class PlaytimeRewardModel(
    val minutes: Int,
    val reward: RewardSetModel
)

data class MissionState(
    val id: String,
    var progress: Int = 0,
    var isClaimed: Boolean = false
)

object MissionManager {
    val dailyStates = mutableStateMapOf<String, MissionState>()
    val weeklyStates = mutableStateMapOf<String, MissionState>()
    
    var dailyPlaytimeSeconds by mutableLongStateOf(0L)
    val claimedPlaytimeRewards = mutableStateListOf<Int>() // Хранит "minutes"

    fun updateProgress(missionId: String, amount: Int, isWeekly: Boolean) {
        val states = if (isWeekly) weeklyStates else dailyStates
        states[missionId]?.let { 
            if (!it.isClaimed) {
                it.progress = (it.progress + amount).coerceAtMost(9999)
            }
        }
    }

    fun tickPlaytime(seconds: Long) {
        dailyPlaytimeSeconds += seconds
    }
}
