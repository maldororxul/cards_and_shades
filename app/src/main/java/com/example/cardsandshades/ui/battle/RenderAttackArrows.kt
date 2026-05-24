package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.cardsandshades.ui.components.LocalDragTargetInfo

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
    val dragInfo = LocalDragTargetInfo.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. Рисуем стрелку прицеливания игрока
        if (isPlayerDrawing) {
            // Если мы тянем стрелку, то концом является текущая позиция пальца (dragInfo.dragPosition)
            // Но если мы кликнули и выбрали карту для атаки (старая механика), то используем playerTargetOffset
            val fingerPos = dragInfo.dragPosition
            val finalArrowEnd = if (fingerPos != Offset.Zero) fingerPos 
                               else playerTargetOffset ?: enemyHeroOffset.takeIf { isEnemyBoardEmpty } ?: playerStart
            
            drawAttackArrow(start = playerStart, end = finalArrowEnd, color = Color.Red)
        }

        // 2. Рисуем стрелку прицеливания ИИ в его ход
        if (aiAttackerId != null) {
            val aiStart = enemyCardsOffsets[aiAttackerId]
            val aiEnd = if (isAiTargetingHero) playerHeroOffset else playerCardsOffsets[aiTargetId]

            if (aiStart != null && aiEnd != null) {
                drawAttackArrow(start = aiStart, end = aiEnd, color = Color.Yellow)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAttackArrow(start: Offset, end: Offset, color: Color) {
    if (start == end) return
    
    val strokeWidth = 6.dp.toPx()
    
    // Линия
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Наконечник стрелки
    val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val arrowSize = 20.dp.toPx()
    
    val p1 = Offset(
        (end.x - arrowSize * Math.cos(angle - Math.PI / 6)).toFloat(),
        (end.y - arrowSize * Math.sin(angle - Math.PI / 6)).toFloat()
    )
    val p2 = Offset(
        (end.x - arrowSize * Math.cos(angle + Math.PI / 6)).toFloat(),
        (end.y - arrowSize * Math.sin(angle + Math.PI / 6)).toFloat()
    )
    
    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    
    drawPath(path = path, color = color)
}
