package com.example.cardsandshades.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CardInspectionDialog(
    card: CardModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isFocusMode by remember { mutableStateOf(false) }

    val borderColor = when (card.rarity) {
        Rarity.COMMON -> Color.Gray
        Rarity.RARE -> Color(0xFF1E88E5)
        Rarity.EPIC -> Color(0xFF8E24AA)
        Rarity.LEGENDARY -> Color(0xFFFDD835)
        Rarity.MYTHIC -> Color(0xFFFF3D00)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ФОН: КАРТИНКА КАРТЫ НА ВЕСЬ ЭКРАН
            CardVisual(card = card, modifier = Modifier.fillMaxSize())
            
            // ЗАТЕМНЕНИЕ (Если не в режиме фокуса)
            if (!isFocusMode) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
                
                // КОНТЕНТ (ПОВЕРХ КАРТИНКИ)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ХЕДЕР
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).background(Color(0xFF0288D1), RoundedCornerShape(25.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            GameText(card.manaCost.toString(), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            GameText(getStringResourceByName(context, card.name), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            val rarityLabel = when(card.rarity) {
                                Rarity.COMMON -> stringResource(R.string.rarity_common)
                                Rarity.RARE -> stringResource(R.string.rarity_rare)
                                Rarity.EPIC -> stringResource(R.string.rarity_epic)
                                Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
                                Rarity.MYTHIC -> stringResource(R.string.rarity_mythic)
                            }
                            GameText(rarityLabel, color = borderColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ОПИСАНИЕ И ЭФФЕКТЫ (В ЦЕНТРЕ)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val effectsDescription = if (card.activeTags.isNotEmpty()) {
                                card.activeEffects.joinToString("\n\n") { 
                                    "✨ " + getStringResourceByName(context, it.name) + "\n" + getStringResourceByName(context, it.description)
                                }
                            } else {
                                stringResource(R.string.effect_normal_desc)
                            }
                            
                            GameText(effectsDescription, color = Color.LightGray, fontSize = 16.sp, textAlign = TextAlign.Center)
                            
                            if (card.buffs.isNotEmpty()) {
                                val buffsText = card.buffs.joinToString("\n") {
                                    context.getString(R.string.buff_format, getStringResourceByName(context, it.name), it.attackBonus, it.healthBonus, it.duration)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                GameText(stringResource(R.string.effect_active_buffs, buffsText), color = Color(0xFF4CAF50), fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    // ФУТЕР: АТАКА И ЗДОРОВЬЕ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(60.dp).background(Color(0xFFD32F2F), RoundedCornerShape(12.dp)).border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                GameText(card.currentAttack.toString(), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            GameText(stringResource(R.string.stat_attack), color = Color.Gray, fontSize = 14.sp)
                        }

                        GameButton(text = stringResource(R.string.close), onClick = onDismiss, containerColor = Color.DarkGray)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GameText(stringResource(R.string.stat_health), color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.size(60.dp).background(Color(0xFF388E3C), RoundedCornerShape(12.dp)).border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                GameText(card.currentHealth.toString(), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            }
                        }
                    }
                }
            }

            // КНОПКА ФОКУСА (Глаз)
            IconButton(
                onClick = { isFocusMode = !isFocusMode },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                GameText(if (isFocusMode) "👁️" else "👁️‍🗨️", fontSize = 24.sp)
            }
        }
    }
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

    // ВЫБОР ЦВЕТА РАМКИ: Сделаем ее более "вычурной" и толстой
    val borderColor = if (card.hasTaunt && !isPreview) {
        Color(0xFFFF9800) // Оранжевый щит
    } else {
        when (card.rarity) {
            Rarity.COMMON -> Color.Gray
            Rarity.RARE -> Color(0xFF1E88E5)
            Rarity.EPIC -> Color(0xFF8E24AA)
            Rarity.LEGENDARY -> Color(0xFFFDD835)
            Rarity.MYTHIC -> Color(0xFFFF3D00)
        }
    }
    
    val borderThickness = when(card.rarity) {
        Rarity.COMMON -> 2.dp
        Rarity.RARE -> 3.dp
        Rarity.EPIC -> 4.dp
        Rarity.LEGENDARY -> 5.dp
        Rarity.MYTHIC -> 6.dp
    }
    val finalBorderThickness = if (card.hasTaunt && !isPreview) borderThickness + 2.dp else borderThickness

    // ОПРЕДЕЛЯЕМ ПРОЗРАЧНОСТЬ (Эффект сна или потраченного хода)
    val cardAlpha = if ((card.isSleeping || card.hasAttackedThisTurn) && !isPreview) 0.6f else 1f

    Box(
        modifier = modifier
            .offset(x = shakeOffset.dp, y = attackOffset)
            .alpha(deathAlpha * cardAlpha)
            .scale(deathScale)
            .padding(2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(finalBorderThickness, borderColor, RoundedCornerShape(10.dp))
                // ВТОРАЯ ТОНКАЯ РАМКА ДЛЯ "ВЫЧУРНОСТИ"
                .padding(finalBorderThickness)
                .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
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
    val context = LocalContext.current
    Box(
        modifier = Modifier.size(22.dp).background(Color(0xFF0288D1), RoundedCornerShape(11.dp)).align(Alignment.TopStart),
        contentAlignment = Alignment.Center
    ) {
        GameText(text = card.manaCost.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        GameText(text = getStringResourceByName(context, card.name), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2)
        Spacer(modifier = Modifier.height(2.dp))
        
        val rarityLabel = when(card.rarity) {
            Rarity.COMMON -> stringResource(R.string.rarity_common)
            Rarity.RARE -> stringResource(R.string.rarity_rare)
            Rarity.EPIC -> stringResource(R.string.rarity_epic)
            Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
            Rarity.MYTHIC -> stringResource(R.string.rarity_mythic)
        }
        GameText(text = rarityLabel, color = borderColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)

        // Маленький текстовый индикатор абилки прямо на карте для удобства в бою
        if (card.activeTags.isNotEmpty()) {
            GameText(
                text = "[${getStringResourceByName(context, card.activeEffects.first().name)}]",
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
