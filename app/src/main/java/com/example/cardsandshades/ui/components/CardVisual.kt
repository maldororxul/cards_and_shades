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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.UserProfile

@Composable
fun CardVisual(
    card: CardModel,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    forceBlur: Boolean = false,
    isBackground: Boolean = false // New flag
) {
    val context = LocalContext.current
    val resName = card.name
    
    // Check ownership ONLY if it's not a background and starts with card_
    val isRealCard = resName.startsWith("card_") && !isBackground
    val isOwned = if (isRealCard) UserProfile.collection.any { it.name == card.name } else true
    val shouldBlur = forceBlur || (isRealCard && !isOwned)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val visualModifier = if (shouldBlur) Modifier.fillMaxSize().blur(12.dp) else Modifier.fillMaxSize()

        if (resName.isNotEmpty()) {
            val cleanResName = resName.lowercase().trim()
            
            val drawableResId = context.resources.getIdentifier(cleanResName, "drawable", context.packageName)
            val rawResId = context.resources.getIdentifier(cleanResName, "raw", context.packageName)
            
            when {
                drawableResId != 0 -> {
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = card.name,
                        modifier = visualModifier,
                        contentScale = contentScale
                    )
                }
                rawResId != 0 -> {
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
                        modifier = visualModifier
                    )
                }
                else -> PlaceholderVisual(card.name)
            }
        } else {
            PlaceholderVisual(card.name)
        }
        
        // Dark overlay for unowned cards
        if (shouldBlur) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        }
    }
}

@Composable
private fun PlaceholderVisual(name: String) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "NO IMG\n${name.takeLast(10)}",
            color = Color.Yellow,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
