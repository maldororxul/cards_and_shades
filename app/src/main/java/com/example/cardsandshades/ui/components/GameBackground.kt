package com.example.cardsandshades.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cardsandshades.catalog.BackgroundCatalog
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity

@Composable
fun GameBackground(
    screenId: String,
    overrideRes: String? = null,
    content: @Composable () -> Unit
) {
    val bgResName = overrideRes ?: BackgroundCatalog.getBackgroundForScreen(screenId)
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        if (bgResName != null) {
            CardVisual(
                card = CardModel(
                    id = "bg",
                    name = bgResName,
                    manaCost = 0,
                    baseAttack = 0,
                    baseHealth = 0,
                    rarity = Rarity.COMMON,
                ),
                modifier = Modifier.fillMaxSize(),
                isBackground = true // Explicitly marked as background
            )
        }
        content()
    }
}
