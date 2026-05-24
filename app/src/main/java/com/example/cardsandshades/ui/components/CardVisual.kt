package com.example.cardsandshades.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cardsandshades.model.CardModel

@Composable
fun CardVisual(
    card: CardModel,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resName = card.name

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (resName.isNotEmpty()) {
            val cleanResName = resName.lowercase().trim()
            
            // 1. Сначала ищем DRAWABLE (основной вариант)
            val drawableResId = context.resources.getIdentifier(cleanResName, "drawable", context.packageName)
            
            // 2. Затем RAW (видео-фон, если есть)
            val rawResId = context.resources.getIdentifier(cleanResName, "raw", context.packageName)
            
            when {
                drawableResId != 0 -> {
                    // СТАТИЧЕСКАЯ КАРТИНКА ИЗ DRAWABLE
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                }
                rawResId != 0 -> {
                    // ПЛЕЕР ДЛЯ MP4 ЖИВЫХ ФОНОВ
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val uri = Uri.parse("android.resource://${ctx.packageName}/$rawResId")
                                setVideoURI(uri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f)
                                }
                                start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PlaceholderVisual(card.name)
                }
            }
        } else {
            PlaceholderVisual(card.name)
        }
    }
}

@Composable
private fun PlaceholderVisual(name: String) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF2C2C2C)), // Более светлый серый
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "NO IMG\n${name.takeLast(10)}", // Показываем конец имени для отладки
            color = Color.Yellow, // Яркий цвет
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
