package com.example.cardsandshades.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cardsandshades.catalog.BackgroundCatalog
import com.example.cardsandshades.model.CardModel

@Composable
fun GameBackground(
    screenId: String,
    content: @Composable () -> Unit
) {
    val bgResName = BackgroundCatalog.getBackgroundForScreen(screenId)
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (bgResName != null) {
            // Используем CardVisual как универсальный загрузчик картинка/видео
            CardVisual(
                card = CardModel(
                    id = "bg",
                    name = "background",
                    manaCost = 0,
                    baseAttack = 0,
                    baseHealth = 0,
                    rarity = com.example.cardsandshades.model.Rarity.COMMON,
                    imageResName = bgResName
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
        content()
    }
}
