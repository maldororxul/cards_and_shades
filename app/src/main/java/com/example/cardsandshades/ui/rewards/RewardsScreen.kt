package com.example.cardsandshades.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.catalog.RewardsCatalog
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText

@Composable
fun RewardsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chainDay by UserProfile.loginChainDays.collectAsState()
    val claimedSet by UserProfile.rewardsClaimed.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameButton(text = "Назад", onClick = onBack, containerColor = Color.Gray)
            GameText("Награды за вход", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameText(
            text = "Заходите каждый день, чтобы получать ценные призы! Ваш текущий день: $chainDay",
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
                    else -> Color(0xFF1E1E1E)
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
                    GameText(text = "День ${reward.day}", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    GameText(
                        text = "${reward.amount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClaimed) Color.Gray else Color.White
                    )
                    GameText(
                        text = getRewardIcon(reward.type),
                        fontSize = 12.sp,
                        color = Color.Yellow
                    )
                    
                    if (isClaimed) {
                        GameText("✅", fontSize = 10.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (claimedSet.contains(chainDay)) {
             GameText("Вы уже забрали сегодняшнюю награду! Ждем вас завтра 🎁", color = Color.Yellow)
        } else {
             GameButton(
                 text = "Забрать награду дня", 
                 onClick = { claimReward(chainDay) },
                 modifier = Modifier.fillMaxWidth()
             )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getRewardIcon(type: String): String {
    return when (type) {
        "gold" -> "🪙 Золото"
        "crystals" -> "💎 Кристаллы"
        "dust_common" -> "⚪ Пыль"
        "dust_rare" -> "🔵 Пыль"
        "dust_epic" -> "🟣 Пыль"
        "dust_legendary" -> "🟡 Пыль"
        else -> "🎁 Предмет"
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
