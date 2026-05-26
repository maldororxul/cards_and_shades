package com.example.cardsandshades.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.utils.getStringResourceByName

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CardInspectionDialog(
    cards: List<CardModel>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { cards.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                CardInspectionContent(card = cards[page], onDismiss = onDismiss)
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
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    var showInspectDialog by remember { mutableStateOf(false) }

    val attackOffset by animateDpAsState(targetValue = if (card.isAttacking) (-40).dp else 0.dp, label = "attack")
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

    val deathAlpha by animateFloatAsState(targetValue = if (card.isDying) 0f else 1f, animationSpec = tween(350), label = "death_alpha")
    val deathScale by animateFloatAsState(targetValue = if (card.isDying) 0.5f else 1f, animationSpec = tween(350), label = "death_scale")

    val borderColor = when (card.rarity) {
        Rarity.COMMON -> Color.White
        Rarity.UNCOMMON -> Color.Green
        Rarity.RARE -> Color(0xFF2196F3)
        Rarity.EPIC -> Color(0xFF9C27B0)
        Rarity.LEGENDARY -> Color.Yellow
        Rarity.MYTHIC -> Color.Red
    }
    
    // THINNER BORDERS FOR COMBAT (using isPreview as proxy for 'not inspecting')
    val borderThickness = if (isPreview) {
        when(card.rarity) {
            Rarity.COMMON -> 1.dp
            Rarity.UNCOMMON -> 1.5.dp
            Rarity.RARE -> 2.dp
            Rarity.EPIC -> 2.5.dp
            Rarity.LEGENDARY -> 3.dp
            Rarity.MYTHIC -> 3.5.dp
        }
    } else {
        when(card.rarity) {
            Rarity.COMMON -> 1.dp
            Rarity.UNCOMMON -> 2.dp
            Rarity.RARE -> 3.dp
            Rarity.EPIC -> 4.dp
            Rarity.LEGENDARY -> 5.dp
            Rarity.MYTHIC -> 6.dp
        }
    }
    
    val finalBorderThickness = if (card.hasTaunt && isPreview) borderThickness + 1.dp else borderThickness
    val finalBorderColor = if (card.hasTaunt && isPreview) Color(0xFFFF9800) else borderColor

    val cardAlpha = if ((card.isSleeping || card.hasAttackedThisTurn) && isPreview) 0.6f else 1f

    val cardBaseModifier = if (modifier == Modifier) Modifier.size(105.dp, 150.dp) else modifier

    Box(
        modifier = cardBaseModifier
            .offset(x = shakeOffset.dp, y = attackOffset)
            .alpha(deathAlpha * cardAlpha)
            .scale(deathScale)
            .padding(2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(finalBorderThickness, finalBorderColor, RoundedCornerShape(10.dp))
                .padding(finalBorderThickness)
                .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .combinedClickable(
                    enabled = isPreview || onClick != null,
                    onClick = {
                        if (onClick != null) {
                            onClick()
                        }
                    },
                    onLongClick = {
                        if (onLongClick != null) {
                            onLongClick()
                        } else {
                            showInspectDialog = true
                        }
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))) {
                    CardVisual(card = card)
                }
                
                if (card.isFrozen) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03A9F4).copy(alpha = 0.4f)))
                }
                
                if (card.isStunned) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFEB3B).copy(alpha = 0.3f)))
                }

                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

                CardContent(card = card, borderColor = finalBorderColor)

                if (card.isTakingDamage && card.lastDamageTaken > 0) {
                    var damageYOffset by remember { mutableStateOf(0.dp) }
                    LaunchedEffect(Unit) { damageYOffset = (-40).dp }
                    val animatedDamageY by animateDpAsState(targetValue = damageYOffset, animationSpec = tween(400), label = "damage_y")

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
        CardInspectionDialog(cards = listOf(card), onDismiss = { showInspectDialog = false })
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
            Rarity.UNCOMMON -> stringResource(R.string.rarity_uncommon)
            Rarity.RARE -> stringResource(R.string.rarity_rare)
            Rarity.EPIC -> stringResource(R.string.rarity_epic)
            Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
            Rarity.MYTHIC -> stringResource(R.string.rarity_mythic)
        }
        GameText(text = rarityLabel, color = borderColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)

        if (card.activeTags.isNotEmpty()) {
            GameText(
                text = "[${getStringResourceByName(context, card.activeEffects.first().name)}]",
                color = Color.Yellow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (card.groups.isNotEmpty()) {
            GameText(
                text = card.groups.first().name,
                color = Color.Cyan,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    val attackBgColor = if (card.currentAttack > card.baseAttack) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Box(modifier = Modifier.size(22.dp).background(attackBgColor, RoundedCornerShape(4.dp)).align(Alignment.BottomStart), contentAlignment = Alignment.Center) {
        GameText(text = card.currentAttack.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    val healthBgColor = when {
        card.currentHealth < card.baseHealth -> Color(0xFF7B1FA2)
        card.currentHealth > card.baseHealth -> Color(0xFF388E3C)
        else -> Color(0xFF388E3C)
    }
    val actualHealthBg = if (card.currentHealth > card.baseHealth) Color(0xFF4CAF50) else healthBgColor

    Box(modifier = Modifier.size(22.dp).background(actualHealthBg, RoundedCornerShape(4.dp)).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
        GameText(text = card.currentHealth.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
