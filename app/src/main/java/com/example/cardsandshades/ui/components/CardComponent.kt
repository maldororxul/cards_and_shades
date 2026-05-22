package com.example.cardsandshades.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity

@Composable
fun CardInspectionDialog(
    card: CardModel,
    onDismiss: () -> Unit
) {
    val borderColor = when (card.rarity) {
        Rarity.COMMON -> Color.Gray
        Rarity.RARE -> Color(0xFF1E88E5)
        Rarity.EPIC -> Color(0xFF8E24AA)
        Rarity.LEGENDARY -> Color(0xFFFDD835)
    }

    GameDialog(
        onDismiss = onDismiss,
        title = card.name,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF0288D1), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(card.manaCost.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    
                    GameText(
                        text = card.rarity.name,
                        color = borderColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ИЗОБРАЖЕНИЕ ИЛИ ВИДЕО КАРТЫ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFF212121), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CardVisual(card = card)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val effectsDescription = if (card.activeTags.isNotEmpty()) {
                    card.activeEffects.joinToString("\n\n") { "✨ ${it.name}\n${it.description}" }
                } else {
                    "Обычное существо.\nНе имеет скрытых магических сил."
                }

                if (card.buffs.isNotEmpty()) {
                    GameText(
                        text = "Активные баффы:\n" + card.buffs.joinToString("\n") { "💪 ${it.name}: +${it.attackBonus}/+${it.healthBonus} (${it.duration} ходов)" },
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                GameText(
                    text = effectsDescription,
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            GameText(card.currentAttack.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GameText("Атака", color = Color.Gray, fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GameText("Здоровье", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFF388E3C), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            GameText(card.currentHealth.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            GameButton(text = "Закрыть", onClick = onDismiss, containerColor = Color(0xFF673AB7))
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardComponent(
    card: CardModel,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    onClick: (() -> Unit)? = null
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
                .combinedClickable(
                    enabled = !isPreview || onClick != null,
                    onClick = {
                        if (onClick != null) {
                            onClick()
                        }
                    },
                    onLongClick = {
                        showInspectDialog = true
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // ФОН КАРТЫ (АРТ)
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))) {
                    CardVisual(card = card)
                }
                
                // ЗАТЕМНЕНИЕ ДЛЯ ЧИТАЕМОСТИ ТЕКСТА
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

                CardContent(card = card, borderColor = borderColor)

                if (card.isTakingDamage && card.lastDamageTaken > 0) {
                    var damageYOffset by remember { mutableStateOf(0.dp) }
                    LaunchedEffect(Unit) { damageYOffset = (-40).dp }
                    val animatedDamageY by animateDpAsState(targetValue = damageYOffset, animationSpec = tween(400))

                    GameText(
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
        CardInspectionDialog(card = card, onDismiss = { showInspectDialog = false })
    }
}

@Composable
private fun BoxScope.CardContent(card: CardModel, borderColor: Color) {
    Box(
        modifier = Modifier.size(22.dp).background(Color(0xFF0288D1), RoundedCornerShape(11.dp)).align(Alignment.TopStart),
        contentAlignment = Alignment.Center
    ) {
        GameText(text = card.manaCost.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        GameText(text = card.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2)
        Spacer(modifier = Modifier.height(2.dp))
        GameText(text = card.rarity.name, color = borderColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)

        // Маленький текстовый индикатор абилки прямо на карте для удобства в бою
        if (card.activeTags.isNotEmpty()) {
            GameText(
                text = "[${card.activeEffects.first().name}]",
                color = Color.Yellow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    val attackBgColor = if (card.currentAttack > card.baseAttack) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Box(modifier = Modifier.size(22.dp).background(attackBgColor, RoundedCornerShape(4.dp)).align(Alignment.BottomStart), contentAlignment = Alignment.Center) {
        GameText(text = card.currentAttack.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    val healthBgColor = when {
        card.currentHealth < card.baseHealth -> Color(0xFF7B1FA2) // Ранен
        card.currentHealth > card.baseHealth -> Color(0xFF388E3C) // Баффнут
        else -> Color(0xFF388E3C) // Норма
    }
    val actualHealthBg = if (card.currentHealth > card.baseHealth) Color(0xFF4CAF50) else healthBgColor

    Box(modifier = Modifier.size(22.dp).background(actualHealthBg, RoundedCornerShape(4.dp)).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
        GameText(text = card.currentHealth.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
