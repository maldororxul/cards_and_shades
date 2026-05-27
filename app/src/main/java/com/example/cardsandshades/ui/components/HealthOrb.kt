package com.example.cardsandshades.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun HealthOrb(
    currentHp: Int,
    maxHp: Int,
    size: Dp = 80.dp,
    liquidColor: Color = Color(0xFFD32F2F)
) {
    val hpPercentage = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
    val animatedHp by animateFloatAsState(targetValue = hpPercentage, animationSpec = tween(1000), label = "hp")
    
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(2.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.toPx()
            val height = size.toPx()
            
            val circlePath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, width, height))
            }
            
            clipPath(circlePath) {
                drawRect(Color(0xFF121212))
                
                val fillHeight = height * (1f - animatedHp)
                val waveAmplitude = size.toPx() * 0.04f
                val waveLength = width
                
                val wavePath = Path().apply {
                    moveTo(0f, fillHeight)
                    for (x in 0..width.toInt()) {
                        val y = fillHeight + waveAmplitude * sin((x / waveLength) * 2 * Math.PI.toFloat() + waveOffset)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                
                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(liquidColor.copy(alpha = 0.6f), liquidColor),
                        startY = fillHeight,
                        endY = height
                    )
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(liquidColor.copy(alpha = 0.3f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * (1f - animatedHp/2f)),
                        radius = width * 0.4f
                    )
                )

                drawOval(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.15f),
                    size = androidx.compose.ui.geometry.Size(width * 0.4f, height * 0.25f)
                )

                drawPath(
                    path = circlePath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.08f)),
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * 1.1f),
                        radius = width * 0.6f
                    )
                )

                drawPath(
                    path = circlePath,
                    color = Color.White.copy(alpha = 0.1f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameText(
                text = currentHp.toString(),
                fontSize = (size.value * 0.28).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            GameText(
                text = "/ $maxHp",
                fontSize = (size.value * 0.14).sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
