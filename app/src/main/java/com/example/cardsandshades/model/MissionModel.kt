package com.example.cardsandshades.model

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import java.util.Calendar

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
    val claimedPlaytimeRewards = mutableStateListOf<Int>()

    var lastResetTimestamp by mutableLongStateOf(0L)

    fun updateProgress(missionId: String, amount: Int, isWeekly: Boolean) {
        val states = if (isWeekly) weeklyStates else dailyStates
        states[missionId]?.let { 
            if (!it.isClaimed) {
                it.progress = (it.progress + amount).coerceAtMost(9999)
            }
        }
    }

    fun tickPlaytime(seconds: Long) {
        checkDailyReset()
        dailyPlaytimeSeconds += seconds
    }

    private fun checkDailyReset() {
        val now = Calendar.getInstance()
        val lastReset = Calendar.getInstance().apply { timeInMillis = lastResetTimestamp }
        
        // Define today's 6:00 AM
        val today6AM = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If it's currently before 6 AM today, the threshold was 6 AM yesterday
        if (now.before(today6AM)) {
            today6AM.add(Calendar.DAY_OF_YEAR, -1)
        }

        if (lastReset.before(today6AM)) {
            resetDailies()
            lastResetTimestamp = System.currentTimeMillis()
        }
    }

    private fun resetDailies() {
        dailyPlaytimeSeconds = 0
        claimedPlaytimeRewards.clear()
        dailyStates.values.forEach { 
            it.progress = 0
            it.isClaimed = false
        }
    }
}
