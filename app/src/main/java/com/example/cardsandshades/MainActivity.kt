package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cardsandshades.ui.booster.BoosterScreen
import com.example.cardsandshades.ui.campaign.CampaignScreen
import com.example.cardsandshades.ui.game.GameScreen
import com.example.cardsandshades.ui.game.GameViewModel

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by remember { mutableStateOf("campaign") }

                    when (currentScreen) {
                        "campaign" -> {
                            Column {
                                // Кнопка перехода в магазин на экране кампании
                                Button(
                                    onClick = { currentScreen = "shop" },
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) { Text("🏪 Магазин Бустеров") }

                                CampaignScreen(
                                    onLevelSelect = { selectedLevel ->
                                        gameViewModel.startNewGame(selectedLevel)
                                        currentScreen = "game"
                                    }
                                )
                            }
                        }
                        "game" -> {
                            GameScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = { currentScreen = "campaign" }
                            )
                        }
                        "shop" -> {
                            BoosterScreen(onBack = { currentScreen = "campaign" })
                        }
                    }
                }
            }
        }
    }
}