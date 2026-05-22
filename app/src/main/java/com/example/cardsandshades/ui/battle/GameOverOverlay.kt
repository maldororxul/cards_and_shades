package com.example.cardsandshades.ui.battle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.model.RewardSetModel
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.utils.getStringResourceByName

@Composable
fun GameOverOverlay(
    isGameOver: Boolean,
    winnerName: String?,
    playerName: String?,
    rewards: RewardSetModel?,
    onExitClick: (Boolean) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AnimatedVisibility(visible = isGameOver, enter = fadeIn(), exit = fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                GameText(stringResource(R.string.battle_finished), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                val isDraw = winnerName == "draw"
                val isPlayerWin = winnerName == playerName
                
                val statusText = when {
                    isDraw -> stringResource(R.string.draw)
                    isPlayerWin -> stringResource(R.string.victory_msg)
                    else -> stringResource(R.string.defeat_msg)
                }
                
                val statusColor = when {
                    isDraw -> Color.Yellow
                    isPlayerWin -> Color.Green
                    else -> Color.Red
                }

                GameText(
                    text = statusText,
                    color = statusColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // ВИЗУАЛИЗАЦИЯ НАГРАДЫ ИГРОКА
                if (isPlayerWin && (rewards != null) && !rewards.isEmpty) {
                    Spacer(modifier = Modifier.height(24.dp))
                    GameText(stringResource(R.string.rewards_label), color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (rewards.gold > 0) RewardItem("🪙 +${rewards.gold} " + stringResource(R.string.reward_gold), Color.Yellow)
                        if (rewards.crystals > 0) RewardItem("💎 +${rewards.crystals} " + stringResource(R.string.reward_crystals), Color(0xFF03A9F4))
                        if (rewards.dustCommon > 0) RewardItem("⚪ +${rewards.dustCommon} " + stringResource(R.string.reward_dust), Color.Gray)
                        if (rewards.dustRare > 0) RewardItem("🔵 +${rewards.dustRare} " + stringResource(R.string.reward_dust), Color(0xFF1E88E5))
                        if (rewards.dustEpic > 0) RewardItem("🟣 +${rewards.dustEpic} " + stringResource(R.string.reward_dust), Color(0xFF8E24AA))
                        if (rewards.dustLegendary > 0) RewardItem("🟡 +${rewards.dustLegendary} " + stringResource(R.string.reward_dust), Color(0xFFFDD835))
                        
                        rewards.cardName?.let { cardName ->
                            Spacer(modifier = Modifier.height(8.dp))
                            GameText(stringResource(R.string.card_added, getStringResourceByName(context, cardName)), color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                GameButton(
                    text = stringResource(R.string.collect_loot),
                    onClick = { onExitClick(isPlayerWin) },
                    containerColor = Color(0xFF388E3C),
                    modifier = Modifier.width(240.dp)
                )
            }
        }
    }
}

@Composable
private fun RewardItem(text: String, color: Color) {
    GameText(text = text, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
}
