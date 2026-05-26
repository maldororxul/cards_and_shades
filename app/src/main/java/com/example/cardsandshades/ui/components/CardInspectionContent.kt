package com.example.cardsandshades.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.utils.getStringResourceByName
import kotlin.math.abs

@Composable
fun CardInspectionContent(
    card: CardModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isFocusMode by remember { mutableStateOf(false) }
    var selectedEffectDesc by remember { mutableStateOf<String?>(null) }

    // Hide system bars when dialog is shown
    val view = androidx.compose.ui.platform.LocalView.current
    val window = (context as? android.app.Activity)?.window
    if (window != null) {
        val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
        LaunchedEffect(Unit) {
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    val rarityColor = when (card.rarity) {
        Rarity.COMMON -> Color.White
        Rarity.UNCOMMON -> Color.Green
        Rarity.RARE -> Color(0xFF2196F3)
        Rarity.EPIC -> Color(0xFF9C27B0)
        Rarity.LEGENDARY -> Color.Yellow
        Rarity.MYTHIC -> Color.Red
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // If vertical swipe distance is large enough, dismiss
                        if (abs(dragAmount.y) > 50f) {
                            onDismiss()
                        }
                    }
                )
            }
    ) {
        CardVisual(
            card = card, 
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale),
            contentScale = ContentScale.Crop
        )
        
        // Solid opaque border frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(12.dp, rarityColor, RoundedCornerShape(0.dp))
        )
        
        if (!isFocusMode) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)))
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    GameText(getStringResourceByName(context, card.name), fontSize = 26.sp, fontWeight = FontWeight.Black)
                    val rarityLabel = when(card.rarity) {
                        Rarity.COMMON -> stringResource(R.string.rarity_common)
                        Rarity.UNCOMMON -> stringResource(R.string.rarity_uncommon)
                        Rarity.RARE -> stringResource(R.string.rarity_rare)
                        Rarity.EPIC -> stringResource(R.string.rarity_epic)
                        Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
                        Rarity.MYTHIC -> stringResource(R.string.rarity_mythic)
                    }
                    GameText(rarityLabel, color = rarityColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        card.groups.forEach { group ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                GameText(group.name, fontSize = 11.sp, color = Color.Cyan)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF0288D1), CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(card.manaCost.toString(), fontWeight = FontWeight.Black, fontSize = 30.sp)
                }
            }

            // FOOTER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))))
                    .navigationBarsPadding()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        card.activeEffects.forEach { effect ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .clickable { 
                                        selectedEffectDesc = getStringResourceByName(context, effect.description)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                GameText(getStringResourceByName(context, effect.name), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                            }
                        }
                    }

                    if (selectedEffectDesc != null) {
                        GameText(selectedEffectDesc!!, color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                        TextButton(onClick = { selectedEffectDesc = null }) {
                            GameText(stringResource(R.string.close).lowercase(), color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        val normalDesc = stringResource(R.string.effect_normal_desc)
                        GameText(normalDesc, color = Color.LightGray, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(70.dp).background(Color(0xFFD32F2F), RoundedCornerShape(12.dp)).border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                GameText(card.currentAttack.toString(), fontWeight = FontWeight.Black, fontSize = 34.sp)
                            }
                            GameText(stringResource(R.string.stat_attack), fontSize = 12.sp, color = Color.Gray)
                        }

                        GameButton(text = stringResource(R.string.back), onClick = onDismiss, containerColor = Color.DarkGray)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(70.dp).background(Color(0xFF388E3C), RoundedCornerShape(12.dp)).border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                GameText(card.currentHealth.toString(), fontWeight = FontWeight.Black, fontSize = 34.sp)
                            }
                            GameText(stringResource(R.string.stat_health), fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { isFocusMode = !isFocusMode },
            modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            GameText(if (isFocusMode) "👁️" else "👁️‍🗨️", fontSize = 28.sp)
        }
    }
}
