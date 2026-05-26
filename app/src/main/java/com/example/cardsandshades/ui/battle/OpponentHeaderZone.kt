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
    val heroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.2f else 1f, label = "scale")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HP CAPSULE (LEFT)
            Box(contentAlignment = Alignment.Center) {
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
                        size = 55.dp, 
                        liquidColor = Color(0xFFB71C1C)
                    )
                }

                if (isHeroTakingDamage) {
                    val damageYOffset by animateDpAsState(targetValue = 40.dp, animationSpec = tween(400), label = "damage")
                    GameText(
                        text = "-$damageValue",
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(y = damageYOffset)
                    )
                }
            }

            // CENTER: HAND & DECK INFO
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val context = androidx.compose.ui.platform.LocalContext.current
                GameText(getStringResourceByName(context, opponent.name), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameText("🎴 ${opponent.hand.size}", fontSize = 10.sp, color = Color.Gray)
                    GameText("📦 ${opponent.deck.size}", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // RIGHT: EMPTY (For symmetry)
            Spacer(modifier = Modifier.size(55.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // OPPONENT MANA BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(stringResource(R.string.mana_label, opponent.currentMana, opponent.maxMana), color = Color(0xFFE91E63), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(10) { i ->
                    val isActive = i < opponent.currentMana
                    val isTotal = i < opponent.maxMana
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFFE91E63) 
                                else if (isTotal) Color.DarkGray.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                            .border(if (isTotal) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}
