package com.example.cardsandshades.ui.battle

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
        // СТАТИСТИКА ВРАГА (Парящая)
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            GameText(getStringResourceByName(context, opponent.name), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            GameText(
                text = stringResource(R.string.opponent_stats, opponent.hand.size, opponent.deck.size, opponent.currentMana, opponent.maxMana),
                color = Color(0xFFFF8A65),
                fontSize = 12.sp
            )
        }

        // ГЕРОЙ ВРАГА (КРУГЛЫЙ)
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(3.dp, if (isHeroTakingDamage) Color.White else Color(0xFFFF5252), CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onEnemyHeroClick() },
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = stringResource(R.string.hp_label, opponent.currentHp, opponent.maxHp),
                    color = if (isHeroTakingDamage) Color.White else Color(0xFFFF5252),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .scale(heroScale)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            onEnemyHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                        }
                )
            }

            if (isHeroTakingDamage) {
                val damageYOffset by animateDpAsState(targetValue = 40.dp, animationSpec = tween(400))
                GameText(
                    text = "-$damageValue",
                    color = Color.Red,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = damageYOffset)
                )
            }
        }
        
        // ПУСТОЕ МЕСТО ДЛЯ СИММЕТРИИ
        Spacer(modifier = Modifier.size(60.dp))
    }
}
