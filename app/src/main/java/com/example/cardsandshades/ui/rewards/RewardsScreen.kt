package com.example.cardsandshades.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.catalog.RewardsCatalog
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.model.AchievementManager
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.utils.getStringResourceByName

@Composable
fun RewardsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.daily_tab), stringResource(R.string.achievements_tab))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF673AB7)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { GameText(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            DailyTab()
        } else {
            AchievementsTab()
        }
    }
}

@Composable
private fun DailyTab() {
    val chainDay by UserProfile.loginChainDays.collectAsState()
    val claimedSet by UserProfile.rewardsClaimed.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(stringResource(R.string.daily_rewards), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameText(
            text = stringResource(R.string.rewards_desc, chainDay),
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(RewardsCatalog.rewards) { reward ->
                val isClaimed = claimedSet.contains(reward.day)
                val isCurrent = reward.day == chainDay
                val canClaim = isCurrent && !isClaimed
                
                val bgColor = when {
                    isClaimed -> Color(0xFF1B5E20).copy(alpha = 0.5f)
                    isCurrent -> Color(0xFF673AB7).copy(alpha = 0.8f)
                    else -> Color(0xFF1E1E1E).copy(alpha = 0.8f)
                }
                
                val borderColor = if (isCurrent) Color.Yellow else Color.DarkGray
                
                Column(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = canClaim) {
                            claimReward(reward.day)
                        }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GameText(text = stringResource(R.string.day_x, reward.day), fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    GameText(
                        text = "${reward.amount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClaimed) Color.Gray else Color.White
                    )
                    GameText(
                        text = getRewardIcon(reward.type, context),
                        fontSize = 12.sp,
                        color = Color.Yellow
                    )
                    
                    if (isClaimed) {
                        GameText(stringResource(R.string.completed_icon), fontSize = 10.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (claimedSet.contains(chainDay)) {
             GameText(stringResource(R.string.reward_claimed_msg), color = Color.Yellow, textAlign = TextAlign.Center)
        } else {
             GameButton(
                 text = stringResource(R.string.claim_reward), 
                 onClick = { claimReward(chainDay) },
                 modifier = Modifier.fillMaxWidth()
             )
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun AchievementsTab() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Принудительное обновление прогресса перед показом
    LaunchedEffect(Unit) {
        AchievementManager.updateProgress(com.example.cardsandshades.catalog.AchievementType.COLLECTION_SIZE, UserProfile.collection.size, true)
        AchievementManager.updateProgress(com.example.cardsandshades.catalog.AchievementType.EPIC_COLLECTION, UserProfile.collection.count { it.rarity == com.example.cardsandshades.model.Rarity.EPIC }, true)
        AchievementManager.updateProgress(com.example.cardsandshades.catalog.AchievementType.CAMPAIGN_LEVEL, UserProfile.maxUnlockedLevel.value - 1, true)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        com.example.cardsandshades.catalog.AchievementCatalog.groups.forEach { group ->
            AchievementGroupItem(group, context)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun AchievementGroupItem(group: com.example.cardsandshades.catalog.AchievementGroup, context: android.content.Context) {
    val state = AchievementManager.getState(group.id)
    val currentTier = group.tiers.getOrNull(state.currentTierIndex)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        GameText(getStringResourceByName(context, group.nameKey), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        
        if (currentTier != null) {
            val progress = state.progressValue
            val goal = currentTier.goal
            val isReady = progress >= goal
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (progress.toFloat() / goal).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (isReady) Color.Green else Color(0xFF673AB7),
                trackColor = Color.DarkGray,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                GameText(stringResource(R.string.ach_progress, progress, goal), fontSize = 11.sp, color = Color.Gray)
                GameText(getRewardText(currentTier.reward, context), fontSize = 11.sp, color = Color.Yellow)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            GameButton(
                text = if (isReady) stringResource(R.string.ach_claim) else stringResource(R.string.locked_icon),
                onClick = {
                    // Начисляем награду
                    val r = currentTier.reward
                    UserProfile.gold.value += r.gold
                    UserProfile.crystals.value += r.crystals
                    UserProfile.dustCommon.value += r.dustCommon
                    UserProfile.dustRare.value += r.dustRare
                    UserProfile.dustEpic.value += r.dustEpic
                    UserProfile.dustLegendary.value += r.dustLegendary
                    
                    state.claimedTiers.add(state.currentTierIndex)
                    state.currentTierIndex++
                    UserProfile.save()
                    SoundManager.playSoundByName(context, "victory")
                },
                enabled = isReady,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp
            )
        } else {
            GameText(stringResource(R.string.ach_completed), color = Color.Green, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

private fun getRewardText(reward: com.example.cardsandshades.model.RewardSetModel, context: android.content.Context): String {
    val parts = mutableListOf<String>()
    if (reward.gold > 0) parts.add("🪙${reward.gold}")
    if (reward.crystals > 0) parts.add("💎${reward.crystals}")
    if (reward.dustCommon > 0) parts.add("⚪${reward.dustCommon}")
    if (reward.dustRare > 0) parts.add("🔵${reward.dustRare}")
    if (reward.dustEpic > 0) parts.add("🟣${reward.dustEpic}")
    if (reward.dustLegendary > 0) parts.add("🟡${reward.dustLegendary}")
    return parts.joinToString(" ")
}

private fun getRewardIcon(type: String, context: android.content.Context): String {
    return when (type) {
        "gold" -> "🪙 " + context.getString(R.string.reward_gold)
        "crystals" -> "💎 " + context.getString(R.string.reward_crystals)
        "dust_common" -> "⚪ " + context.getString(R.string.reward_dust)
        "dust_rare" -> "🔵 " + context.getString(R.string.reward_dust)
        "dust_epic" -> "🟣 " + context.getString(R.string.reward_dust)
        "dust_legendary" -> "🟡 " + context.getString(R.string.reward_dust)
        else -> "🎁 " + context.getString(R.string.reward_item)
    }
}

private fun claimReward(day: Int) {
    val reward = RewardsCatalog.rewards.find { it.day == day } ?: return
    if (UserProfile.rewardsClaimed.value.contains(day)) return
    
    when (reward.type) {
        "gold" -> UserProfile.gold.value += reward.amount
        "crystals" -> UserProfile.crystals.value += reward.amount
        "dust_common" -> UserProfile.dustCommon.value += reward.amount
        "dust_rare" -> UserProfile.dustRare.value += reward.amount
        "dust_epic" -> UserProfile.dustEpic.value += reward.amount
        "dust_legendary" -> UserProfile.dustLegendary.value += reward.amount
    }
    
    val newClaimed = UserProfile.rewardsClaimed.value + day
    UserProfile.rewardsClaimed.value = newClaimed
    UserProfile.save()
}
