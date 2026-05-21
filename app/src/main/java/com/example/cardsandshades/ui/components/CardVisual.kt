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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resName = card.imageResName

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        if (!resName.isNullOrEmpty()) {
            val folder = if (card.isVideo) "raw" else "drawable"
            // Принудительно в нижний регистр, так как ресурсы Android только в нем
            val cleanResName = resName.lowercase().trim()
            val resId = context.resources.getIdentifier(cleanResName, folder, context.packageName)
            
            if (resId != 0) {
                if (card.isVideo) {
                    // ПЛЕЕР ДЛЯ MP4 ЖИВЫХ ФОНОВ
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                // Используем имя пакета из контекста для надежности
                                val uri = Uri.parse("android.resource://${ctx.packageName}/$resId")
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
                } else {
                    // СТАТИЧЕСКАЯ КАРТИНКА ИЗ DRAWABLE
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Если id = 0, значит ресурс не найден по имени
                PlaceholderVisual(card.name)
            }
        } else {
            PlaceholderVisual(card.name)
        }
    }
}

@Composable
private fun PlaceholderVisual(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = name.take(1),
            color = Color.DarkGray,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )
    }
}
