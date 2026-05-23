package com.example.cardsandshades.ui.battle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.DragTarget
import com.example.cardsandshades.ui.components.GameText

@Composable
fun PlayerControlsZone(
    player: PlayerModel,
    isPlayerTurn: Boolean,
    isHeroTakingDamage: Boolean,
    damageValue: Int,
    onPlayerHeroPositioned: (Offset) -> Unit,
    onEndTurnClick: () -> Unit
) {
    var inspectedCard by remember { mutableStateOf<CardModel?>(null) }
    val playerHeroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.2f else 1f)
    
    val endTurnButtonColor by animateColorAsState(
        targetValue = if (isPlayerTurn) Color(0xFFFDD835) else Color.DarkGray,
        animationSpec = tween(500)
    )

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // ГЕРОЙ ИГРОКА (Без бокса, парящий)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(3.dp, if (isHeroTakingDamage) Color.Red else Color(0xFF388E3C), CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameText(
                        text = stringResource(R.string.hp_label, player.currentHp, player.maxHp),
                        color = if (isHeroTakingDamage) Color.Red else Color(0xFF66BB6A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .scale(playerHeroScale)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onPlayerHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            }
                    )

                    if (isHeroTakingDamage) {
                        val damageYOffset by animateDpAsState(targetValue = (-40).dp, animationSpec = tween(400))
                        GameText(
                            text = "-$damageValue",
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.offset(y = damageYOffset)
                        )
                    }
                }
            }

            // РУКА ИГРОКА
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy((-15).dp), // Слегка внахлест
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(140.dp)
            ) {
                items(player.hand, key = { it.id }) { card ->
                    DragTarget(
                        card = card,
                        onLongClick = { inspectedCard = card }
                    ) {
                        CardComponent(card = card, isPreview = true, modifier = Modifier.size(90.dp, 130.dp))
                    }
                }
            }

            // КНОПКА ЗАВЕРШЕНИЯ ХОДА (КРУГЛАЯ)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .shadow(if (isPlayerTurn) 15.dp else 0.dp, CircleShape, spotColor = endTurnButtonColor)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(3.dp, endTurnButtonColor, CircleShape)
                    .clickable(enabled = isPlayerTurn) { onEndTurnClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameText(
                        text = stringResource(R.string.end_turn),
                        color = if (isPlayerTurn) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (isPlayerTurn) {
                        GameText("➔", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // МАНА-БАР (Стильный полупрозрачный)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(stringResource(R.string.mana_label, player.currentMana, player.maxMana), color = Color(0xFF03A9F4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(10) { i ->
                    val isActive = i < player.currentMana
                    val isTotal = i < player.maxMana
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFF03A9F4) 
                                else if (isTotal) Color.DarkGray.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                            .border(if (isTotal) 1.dp else 0.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }

    if (inspectedCard != null) {
        CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
    }
}
