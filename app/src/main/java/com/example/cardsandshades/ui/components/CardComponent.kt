package com.example.cardsandshades.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun CardComponent(
    card: CardModel,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    onClick: () -> Unit = {}
) {
    var showInspectDialog by remember { mutableStateOf(false) }

    val borderColor = when (card.rarity) {
        Rarity.COMMON -> Color.Gray
        Rarity.RARE -> Color(0xFF1E88E5)
        Rarity.EPIC -> Color(0xFF8E24AA)
        Rarity.LEGENDARY -> Color(0xFFFDD835)
    }

    // 1. АНИМАЦИЯ АТАКУЮЩЕГО (Рывок вверх/вниз на 40dp)
    val attackOffset by animateDpAsState(
        targetValue = if (card.isAttacking) (-40).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    // 2. АНИМАЦИЯ ПОЛУЧЕНИЯ УРОНА (Тряска по оси X через бесконечную анимацию)
    val shakeTransition = rememberInfiniteTransition()
    val shakeOffset by shakeTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val currentXOffset = if (card.isTakingDamage) shakeOffset.dp else 0.dp

    // 3. АНИМАЦИЯ ПАДЕНИЯ/СМЕРТИ (Уменьшение масштаба и растворение)
    val deathAlpha by animateFloatAsState(targetValue = if (card.isDying) 0f else 1f, animationSpec = tween(350))
    val deathScale by animateFloatAsState(targetValue = if (card.isDying) 0.5f else 1f, animationSpec = tween(350))

    Box(
        modifier = modifier
            .offset(x = currentXOffset, y = attackOffset)
            .alpha(deathAlpha)
            .scale(deathScale)
    ) {
        Card(
            modifier = Modifier
                .width(105.dp)
                .height(150.dp)
                .border(2.dp, if (card.isTakingDamage) Color.Red else borderColor, RoundedCornerShape(8.dp))
                .clickable(enabled = !isPreview) {
                    if (onClick == {}) showInspectDialog = true else onClick()
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CardContent(card = card, borderColor = borderColor)

                // 4. ВЫЛЕТАЮЩИЕ ЦИФРЫ УРОНА (Появляются поверх карты при уроне)
                if (card.isTakingDamage && card.lastDamageTaken > 0) {
                    val damageYOffset by animateDpAsState(
                        targetValue = (-30).dp,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                    Text(
                        text = "-${card.lastDamageTaken}",
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = damageYOffset)
                    )
                }
            }
        }
    }

    if (showInspectDialog) {
        Dialog(onDismissRequest = { showInspectDialog = false }) {
            Card(
                modifier = Modifier
                    .width(240.dp)
                    .height(360.dp)
                    .border(4.dp, borderColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0288D1), RoundedCornerShape(18.dp))
                            .align(Alignment.TopCenter),
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
                        Text(
                            text = "Существо базовой ценности статов: ${card.manaCost * 2}. Призовите его на поле боя для сражения.",
                            color = Color.Gray,
                            fontSize = 12.sp,
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
private fun CardContent(card: CardModel, borderColor: Color) {
    Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
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
        }

        Box(modifier = Modifier.size(20.dp).background(Color(0xFFD32F2F), RoundedCornerShape(4.dp)).align(Alignment.BottomStart), contentAlignment = Alignment.Center) {
            Text(text = card.currentAttack.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        val healthBgColor = if (card.currentHealth < card.baseHealth) Color(0xFF7B1FA2) else Color(0xFF388E3C)
        Box(modifier = Modifier.size(20.dp).background(healthBgColor, RoundedCornerShape(4.dp)).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
            Text(text = card.currentHealth.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
