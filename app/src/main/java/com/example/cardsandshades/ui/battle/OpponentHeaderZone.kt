package com.example.cardsandshades.ui.battle

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.HealthOrb
import com.example.cardsandshades.utils.getStringResourceByName

@Composable
fun OpponentHeaderZone(
    opponent: PlayerModel,
    isHeroTakingDamage: Boolean,
    damageValue: Int,
    onEnemyHeroPositioned: (Offset) -> Unit,
    onEnemyHeroClick: () -> Unit
) {
    val heroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.2f else 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // СТАТИСТИКА ВРАГА
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            GameText(getStringResourceByName(context, opponent.name), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            GameText(
                text = stringResource(R.string.in_deck_label, opponent.deck.size),
                color = Color.Gray,
                fontSize = 10.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // МАНА-БАР ВРАГА
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(10) { i ->
                    val isActive = i < opponent.currentMana
                    val isTotal = i < opponent.maxMana
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFF03A9F4) 
                                else if (isTotal) Color.DarkGray.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                    )
                }
            }
        }

        // ГЕРОЙ ВРАГА (HEALTH ORB)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 8.dp)) {
            Box(
                modifier = Modifier
                    .scale(heroScale)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInWindow()
                        onEnemyHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                    }
                    .clickable { onEnemyHeroClick() }
            ) {
                HealthOrb(
                    currentHp = opponent.currentHp,
                    maxHp = opponent.maxHp,
                    size = 60.dp,
                    liquidColor = Color(0xFFB71C1C)
                )
            }

            if (isHeroTakingDamage) {
                val damageYOffset by animateDpAsState(targetValue = 40.dp, animationSpec = tween(400))
                GameText(
                    text = "-$damageValue",
                    color = Color.Red,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = damageYOffset)
                )
            }
        }
        
        Spacer(modifier = Modifier.size(60.dp))
    }
}
