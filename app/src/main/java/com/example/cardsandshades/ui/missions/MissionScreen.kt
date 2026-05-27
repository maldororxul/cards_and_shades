package com.example.cardsandshades.ui.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.catalog.MissionCatalog
import com.example.cardsandshades.model.*
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameDialog
import com.example.cardsandshades.utils.getStringResourceByName
import com.example.cardsandshades.sound.SoundManager
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MissionScreen(
    modifier: Modifier = Modifier,
    isWeekly: Boolean = false
) {
    val missions = if (isWeekly) MissionCatalog.weeklyMissions else MissionCatalog.dailyMissions
    val states = if (isWeekly) MissionManager.weeklyStates else MissionManager.dailyStates

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isWeekly) {
            PlaytimeProgressBar()
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(missions) { mission ->
                val state = states.getOrPut(mission.id) { MissionState(mission.id) }
                MissionItem(mission, state)
            }
        }
    }
}

@Composable
fun PlaytimeProgressBar() {
    val context = LocalContext.current
    val totalSeconds = MissionManager.dailyPlaytimeSeconds
    val totalMinutes = (totalSeconds / 60).toInt()
    
    val rewards = MissionCatalog.playtimeRewards
    val nextReward = rewards.find { it.minutes > totalMinutes && !MissionManager.claimedPlaytimeRewards.contains(it.minutes) }

    var tooltipMessage by remember { mutableStateOf<String?>(null) }
    var claimDialogReward by remember { mutableStateOf<RewardSetModel?>(null) }

    val playReqLabel = stringResource(R.string.playtime_requirement)

    LaunchedEffect(tooltipMessage) {
        if (tooltipMessage != null) {
            delay(2000)
            tooltipMessage = null
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                GameText(stringResource(R.string.daily_playtime), fontSize = 12.sp, color = Color.Gray)
                GameText(stringResource(R.string.playtime_format, totalMinutes, 60), fontSize = 14.sp, color = Color.Cyan, fontWeight = FontWeight.Bold)
            }
            
            if (nextReward != null) {
                val remainingTotalSecs = (nextReward.minutes.toLong() * 60L) - totalSeconds
                val remMins = (remainingTotalSecs / 60).toInt().coerceAtLeast(0)
                val remSecs = (remainingTotalSecs % 60).toInt().coerceAtLeast(0)
                
                Column(horizontalAlignment = Alignment.End) {
                    GameText(stringResource(R.string.next_reward_in), fontSize = 10.sp, color = Color.Gray)
                    GameText(
                        text = String.format(Locale.US, "%02d:%02d", remMins, remSecs),
                        fontSize = 16.sp,
                        color = Color.Yellow,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        if (tooltipMessage != null) {
            GameText(tooltipMessage!!, color = Color.Cyan, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterStart) {
            LinearProgressIndicator(
                progress = { (totalMinutes.toFloat() / 60f).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                color = Color.Cyan,
                trackColor = Color.DarkGray
            )
            
            rewards.forEach { reward ->
                val pos = (reward.minutes.toFloat() / 60f).coerceAtMost(1f)
                val isClaimed = MissionManager.claimedPlaytimeRewards.contains(reward.minutes)
                val isAvailable = (totalMinutes >= reward.minutes) && !isClaimed

                Box(
                    modifier = Modifier
                        .fillMaxWidth(pos)
                        .wrapContentWidth(Alignment.End)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = 18.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isClaimed) Color.Gray else if (isAvailable) Color.Green else Color.Black.copy(alpha = 0.5f))
                            .border(2.dp, if (isAvailable) Color.White else Color.Transparent, CircleShape)
                            .clickable {
                                if (isAvailable) {
                                    UserProfile.applyReward(reward.reward)
                                    MissionManager.claimedPlaytimeRewards.add(reward.minutes)
                                    UserProfile.save()
                                    claimDialogReward = reward.reward
                                    SoundManager.playSoundByName(context, "victory")
                                } else {
                                    tooltipMessage = playReqLabel.format(reward.minutes) + ": " + formatReward(reward.reward)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(if (isClaimed) "✔" else "🎁", fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (claimDialogReward != null) {
        GameDialog(
            onDismiss = { claimDialogReward = null },
            title = stringResource(R.string.reward_claimed_title),
            content = {
                GameText(stringResource(R.string.reward_claimed_msg, formatReward(claimDialogReward!!)), color = Color.Green, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            },
            confirmButton = { onAction ->
                GameButton(text = "OK", onClick = { onAction(); claimDialogReward = null })
            }
        )
    }
}

private fun formatReward(reward: RewardSetModel): String {
    val parts = mutableListOf<String>()
    if (reward.gold > 0) parts.add("${reward.gold} Gold")
    if (reward.crystals > 0) parts.add("${reward.crystals} Crystals")
    if (reward.dustCommon > 0) parts.add("${reward.dustCommon} Dust")
    reward.cardName?.let { parts.add("Card") }
    return parts.joinToString(", ")
}

@Composable
fun MissionItem(mission: MissionModel, state: MissionState) {
    val context = LocalContext.current
    val progress = state.progress
    val goal = mission.goal
    val isComplete = progress >= goal
    val isClaimed = state.isClaimed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .border(1.dp, if (isClaimed) Color.Gray else if (isComplete) Color.Green else Color.DarkGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            GameText(getStringResourceByName(context, mission.nameKey), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (progress.toFloat() / goal.toFloat()).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(6.6.dp).clip(CircleShape),
                color = if (isComplete) Color.Green else Color.Cyan,
                trackColor = Color.DarkGray
            )
            GameText("$progress / $goal", fontSize = 12.sp, color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        if (isClaimed) {
            GameText(stringResource(R.string.mission_claimed), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        } else {
            GameButton(
                text = if (isComplete) stringResource(R.string.claim) else stringResource(R.string.mission_go),
                onClick = {
                    if (isComplete) {
                        UserProfile.applyReward(mission.reward)
                        state.isClaimed = true
                        UserProfile.save()
                        SoundManager.playSoundByName(context, "victory")
                    }
                },
                containerColor = if (isComplete) Color.Green else Color.DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}
