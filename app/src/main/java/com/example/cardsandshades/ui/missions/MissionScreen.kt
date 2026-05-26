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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.cardsandshades.utils.getStringResourceByName
import com.example.cardsandshades.sound.SoundManager

@Composable
fun MissionScreen(
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.daily_tab), stringResource(R.string.weekly_tab))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameText(stringResource(R.string.missions_title), fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        // Playtime Progress Bar
        PlaytimeProgressBar()

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.Cyan
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { GameText(title, fontSize = 14.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val missions = if (selectedTab == 0) MissionCatalog.dailyMissions else MissionCatalog.weeklyMissions
        val states = if (selectedTab == 0) MissionManager.dailyStates else MissionManager.weeklyStates

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(missions) { mission ->
                val state = states.getOrPut(mission.id) { MissionState(mission.id) }
                MissionItem(mission, state)
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun PlaytimeProgressBar() {
    val totalSeconds = MissionManager.dailyPlaytimeSeconds
    val totalMinutes = (totalSeconds / 60).toInt()
    val rewards = MissionCatalog.playtimeRewards

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GameText(stringResource(R.string.daily_playtime), fontSize = 12.sp, color = Color.Gray)
            GameText(stringResource(R.string.playtime_format, totalMinutes, 60), fontSize = 12.sp, color = Color.Cyan)
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.CenterStart) {
            LinearProgressIndicator(
                progress = { (totalMinutes.toFloat() / 60f).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                color = Color.Cyan,
                trackColor = Color.DarkGray
            )
            
            // Chest markers
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
                            .offset(x = 16.dp) // Half of chest size to center it on the point
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isClaimed) Color.Gray else if (isAvailable) Color.Yellow else Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, if (isAvailable) Color.White else Color.Transparent, CircleShape)
                            .clickable(enabled = isAvailable) {
                                UserProfile.applyReward(reward.reward)
                                MissionManager.claimedPlaytimeRewards.add(reward.minutes)
                                UserProfile.save()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(if (isClaimed) "✔" else "🎁", fontSize = 14.sp)
                    }
                }
            }
        }
    }
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
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
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
