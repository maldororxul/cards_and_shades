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
import com.example.cardsandshades.ui.components.HealthOrb
import com.example.cardsandshades.ui.game.GameViewModel

@Composable
fun PlayerControlsZone(
    player: PlayerModel,
    isPlayerTurn: Boolean,
    isHeroTakingDamage: Boolean,
    damageValue: Int,
    onPlayerHeroPositioned: (Offset) -> Unit,
    onCardLongClick: (CardModel) -> Unit,
    viewModel: GameViewModel
) {
    val playerHeroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.2f else 1f)
    
    val endTurnButtonColor by animateColorAsState(
        targetValue = if (isPlayerTurn) Color(0xFFFDD835) else Color.DarkGray,
        animationSpec = tween(500)
    )

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // ЛЕВАЯ КОЛОНКА КНОПОК
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // КНОПКА СКОРОСТИ
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.6f))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
                        .clickable { viewModel.cycleAnimationSpeed() },
                    contentAlignment = Alignment.Center
                ) {
                    GameText("x${viewModel.animationSpeed}", fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                // КНОПКА АВТОБОЯ
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(if (viewModel.isAutoBattleActive) Color(0xFF673AB7) else Color.Black.copy(alpha = 0.4f))
                        .border(2.dp, if (viewModel.isAutoBattleActive) Color.Cyan else Color.Gray, CircleShape)
                        .clickable { viewModel.toggleAutoBattle() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GameText("AUTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isAutoBattleActive) Color.White else Color.Gray)
                        GameText(if (viewModel.isAutoBattleActive) "ON" else "OFF", fontSize = 12.sp, color = if (viewModel.isAutoBattleActive) Color.Green else Color.Gray)
                    }
                }
            }

            // РУКА ИГРОКА
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy((-15).dp),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(140.dp)
            ) {
                items(player.hand, key = { it.id }) { card ->
                    DragTarget(
                        card = card,
                        onLongClick = { onCardLongClick(card) }
                    ) {
                        CardComponent(card = card, isPreview = true, modifier = Modifier.size(90.dp, 130.dp))
                    }
                }
            }

            // ПРАВАЯ ЧАСТЬ: HP ORB НАД КНОПКОЙ
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .scale(playerHeroScale)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onPlayerHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            }
                    ) {
                        HealthOrb(
                            currentHp = player.currentHp,
                            maxHp = player.maxHp,
                            size = 65.dp,
                            liquidColor = Color(0xFFD32F2F)
                        )
                    }

                    if (isHeroTakingDamage) {
                        val damageYOffset by animateDpAsState(targetValue = (-50).dp, animationSpec = tween(400))
                        GameText(
                            text = "-$damageValue",
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.offset(y = damageYOffset)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // КНОПКА ЗАВЕРШЕНИЯ ХОДА
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .shadow(if (isPlayerTurn) 15.dp else 0.dp, CircleShape, spotColor = endTurnButtonColor)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(4.dp, endTurnButtonColor, CircleShape)
                        .clickable(enabled = isPlayerTurn) { viewModel.endTurn() },
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
        }

        Spacer(modifier = Modifier.height(6.dp))

        // МАНА-БАР
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(stringResource(R.string.mana_label, player.currentMana, player.maxMana), color = Color(0xFF03A9F4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(10) { i ->
                    val isActive = i < player.currentMana
                    val isTotal = i < player.maxMana
                    Box(
                        modifier = Modifier
                            .size(10.dp)
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
}
