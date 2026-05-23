package com.example.cardsandshades.ui.battle

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.example.cardsandshades.ui.components.AttackArrow
import kotlin.collections.get

@Composable
fun RenderAttackArrows(
    isPlayerDrawing: Boolean,
    playerStart: Offset,
    playerTargetOffset: Offset?,
    enemyHeroOffset: Offset,
    isEnemyBoardEmpty: Boolean,
    aiAttackerId: String?,
    aiTargetId: String?,
    isAiTargetingHero: Boolean,
    enemyCardsOffsets: Map<String, Offset>,
    playerCardsOffsets: Map<String, Offset>,
    playerHeroOffset: Offset
) {
    // 1. Рисуем стрелку прицеливания игрока
    if (isPlayerDrawing) {
        val finalArrowEnd = playerTargetOffset ?: enemyHeroOffset.takeIf { isEnemyBoardEmpty } ?: playerStart
        AttackArrow(start = playerStart, end = finalArrowEnd)
    }

    // 2. Рисуем стрелку прицеливания ИИ в его ход
    if (aiAttackerId != null) {
        val aiStart = enemyCardsOffsets[aiAttackerId]
        val aiEnd = if (isAiTargetingHero) playerHeroOffset else playerCardsOffsets[aiTargetId]

        if (aiStart != null && aiEnd != null) {
            AttackArrow(start = aiStart, end = aiEnd)
        }
    }
}