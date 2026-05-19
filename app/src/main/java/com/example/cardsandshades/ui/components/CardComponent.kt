package com.example.cardsandshades.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity

@Composable
fun CardComponent(
    card: CardModel,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    onClick: () -> Unit = {}
) {
    var showInspectDialog by remember { mutableStateOf(false) }

    val attackOffset by animateDpAsState(targetValue = if (card.isAttacking) (-40).dp else 0.dp)
    var shakeOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(card.isTakingDamage) {
        if (card.isTakingDamage) {
            repeat(2) {
                shakeOffset = -10f
                kotlinx.coroutines.delay(50)
                shakeOffset = 10f
                kotlinx.coroutines.delay(50)
            }
            shakeOffset = 0f
        }
    }

    val deathAlpha by animateFloatAsState(targetValue = if (card.isDying) 0f else 1f, animationSpec = tween(350))
    val deathScale by animateFloatAsState(targetValue = if (card.isDying) 0.5f else 1f, animationSpec = tween(350))

    // ВЫБОР ЦВЕТА РАМКИ: Если у карты есть Провокация (Taunt) — делаем рамку толстой и золотисто-оранжевой
    val borderColor = if (card.hasTaunt && !isPreview) {
        Color(0xFFFF9800) // Оранжевый щит
    } else {
        when (card.rarity) {
            Rarity.COMMON -> Color.Gray
            Rarity.RARE -> Color(0xFF1E88E5)
            Rarity.EPIC -> Color(0xFF8E24AA)
            Rarity.LEGENDARY -> Color(0xFFFDD835)
        }
    }
    val borderThickness = if (card.hasTaunt && !isPreview) 4.dp else 2.dp

    // ОПРЕДЕЛЯЕМ ПРОЗРАЧНОСТЬ (Эффект сна или потраченного хода)
    // Спящие или уже походившие карты слегка затеняются (alpha = 0.6f)
    val cardAlpha = if ((card.isSleeping || card.hasAttackedThisTurn) && !isPreview) 0.6f else 1f

    Box(
        modifier = modifier
            .offset(x = shakeOffset.dp, y = attackOffset)
            .alpha(deathAlpha * cardAlpha)
            .scale(deathScale)
    ) {
        Card(
            modifier = Modifier
                .width(105.dp)
                .height(150.dp)
                .border(borderThickness, if (card.isTakingDamage) Color.Red else borderColor, RoundedCornerShape(8.dp))
                .clickable(enabled = !isPreview) {
                    if (onClick == {}) showInspectDialog = true else onClick()
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CardContent(card = card, borderColor = borderColor)

                if (card.isTakingDamage && card.lastDamageTaken > 0) {
                    var damageYOffset by remember { mutableStateOf(0.dp) }
                    LaunchedEffect(Unit) { damageYOffset = (-40).dp }
                    val animatedDamageY by animateDpAsState(targetValue = damageYOffset, animationSpec = tween(400))

                    Text(
                        text = "-${card.lastDamageTaken}",
                        color = Color.Red,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center).offset(y = animatedDamageY)
                    )
                }
            }
        }
    }

    if (showInspectDialog) {
        Dialog(onDismissRequest = { showInspectDialog = false }) {
            Card(
                modifier = Modifier.width(240.dp).height(360.dp).border(4.dp, borderColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(Color(0xFF0288D1), RoundedCornerShape(18.dp)).align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(card.manaCost.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(card.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(card.rarity.name, color = borderColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(16.dp))

                        val effectsDescription = if (card.activeTags.isNotEmpty()) {
                            card.activeEffects.joinToString("\n") { "✨ ${it.name}: ${it.description}" }
                        } else {
                            "Обычное существо. Не имеет скрытых магических сил."
                        }

                        if (card.activeTags.isNotEmpty()) {
                            Text(
                                text = "[${card.activeEffects.first().name}]",
                                color = Color.Yellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = effectsDescription,
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(36.dp).background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Text(card.currentAttack.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(36.dp).background(Color(0xFF388E3C), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Text(card.currentHealth.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CardContent(card: CardModel, borderColor: Color) {
    Box(
        modifier = Modifier.size(20.dp).background(Color(0xFF0288D1), RoundedCornerShape(10.dp)).align(Alignment.TopStart),
        contentAlignment = Alignment.Center
    ) {
        Text(text = card.manaCost.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }

    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = card.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = card.rarity.name, color = borderColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)

        // Маленький текстовый индикатор абилки прямо на карте для удобства в бою
        if (card.activeTags.isNotEmpty()) {
            Text(
                text = "[${card.activeEffects.first().name}]",
                color = Color.Yellow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    Box(modifier = Modifier.size(20.dp).background(Color(0xFFD32F2F), RoundedCornerShape(4.dp)).align(Alignment.BottomStart), contentAlignment = Alignment.Center) {
        Text(text = card.currentAttack.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }

    val healthBgColor = if (card.currentHealth < card.baseHealth) Color(0xFF7B1FA2) else Color(0xFF388E3C)
    Box(modifier = Modifier.size(20.dp).background(healthBgColor, RoundedCornerShape(4.dp)).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
        Text(text = card.currentHealth.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}