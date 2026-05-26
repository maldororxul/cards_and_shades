package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RenderAttackArrows(
    isPlayerDrawing: Boolean,
    playerStart: Offset,
    playerTargetOffset: Offset,
    enemyHeroOffset: Offset,
    isEnemyBoardEmpty: Boolean,
    aiAttackerId: String?,
    aiTargetId: String?,
    isAiTargetingHero: Boolean,
    enemyCardsOffsets: Map<String, Offset>,
    playerCardsOffsets: Map<String, Offset>,
    playerHeroOffset: Offset
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // AI ARROWS
        if (aiAttackerId != null) {
            val start = playerCardsOffsets[aiAttackerId] ?: enemyCardsOffsets[aiAttackerId]
            val end = if (isAiTargetingHero) {
                if (playerCardsOffsets.containsKey(aiAttackerId)) enemyHeroOffset else playerHeroOffset
            } else {
                aiTargetId?.let { tid ->
                    enemyCardsOffsets[tid] ?: playerCardsOffsets[tid]
                }
            }

            if (start != null && end != null) {
                drawAttackArrow(start, end, Color.Red)
            }
        }

        // PLAYER ARROW
        if (isPlayerDrawing) {
            // Target validation: Arrow only points to ENEMY units (board or hero)
            // We check if target is near any enemy board slot or the enemy hero
            val isTargetingEnemyBoard = enemyCardsOffsets.values.any { (it - playerTargetOffset).getDistance() < 100f }
            val isTargetingEnemyHero = (enemyHeroOffset - playerTargetOffset).getDistance() < 150f
            
            if (isTargetingEnemyBoard || isTargetingEnemyHero) {
                drawAttackArrow(playerStart, playerTargetOffset, Color.Yellow)
            } else {
                // Dimmed or different color if pointing to invalid target
                drawAttackArrow(playerStart, playerTargetOffset, Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAttackArrow(start: Offset, end: Offset, color: Color) {
    val strokeWidth = 6.dp.toPx()
    val headSize = 15.dp.toPx()

    // Line
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Arrow Head
    val angle = atan2(end.y - start.y, end.x - start.x)
    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            end.x - headSize * cos(angle - 0.5f),
            end.y - headSize * sin(angle - 0.5f)
        )
        moveTo(end.x, end.y)
        lineTo(
            end.x - headSize * cos(angle + 0.5f),
            end.y - headSize * sin(angle + 0.5f)
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
