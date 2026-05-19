package com.example.cardsandshades.ui.components

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
fun AttackArrow(
    start: Offset,
    end: Offset,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 6.dp.toPx()
        val color = Color(0xFFFF5252) // Агрессивный красный цвет атаки

        // 1. Рисуем линию атаки
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // 2. Рассчитываем угол наклона наконечника стрелки
        val angle = atan2(end.y - start.y, end.x - start.x)
        val arrowLength = 20.dp.toPx()
        val arrowAngle = Math.toRadians(30.0)

        val path = Path().apply {
            moveTo(end.x, end.y)
            lineTo(
                (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
                (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
            )
            moveTo(end.x, end.y)
            lineTo(
                (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
                (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
            )
        }

        // 3. Рисуем наконечник стрелки
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
