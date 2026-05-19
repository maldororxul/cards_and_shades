package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cardsandshades.ui.campaign.CampaignScreen
import com.example.cardsandshades.ui.components.GameScreen
import com.example.cardsandshades.ui.components.GameViewModel

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Простейший стейт-менеджер экранов
                    var currentScreen by remember { mutableStateOf("campaign") }

                    when (currentScreen) {
                        "campaign" -> {
                            CampaignScreen(
                                onLevelSelect = { selectedLevel ->
                                    gameViewModel.startNewGame(selectedLevel)
                                    currentScreen = "game"
                                }
                            )
                        }
                        "game" -> {
                            GameScreen(
                                viewModel = gameViewModel
                                // В будущем добавим кнопку "Назад в меню" на игровом экране
                            )
                        }
                    }
                }
            }
        }
    }
}