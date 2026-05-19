package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.campaign.CampaignScreen
import com.example.cardsandshades.ui.game.GameScreen
import com.example.cardsandshades.ui.game.GameViewModel

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем локальное хранилище Room при запуске игры
        UserProfile.initDatabase(this)

        // 1. Разрешаем приложению отрисовываться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Скрываем статус-бар и панель навигации (кнопки)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("campaign") }

                    when (currentScreen) {
                        "campaign" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // ПАНЕЛЬ НАВИГАЦИИ МЕНЮ ГЛАВНОЙ КАМПАНИИ
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { currentScreen = "shop" },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("🏪 Магазин") }

                                    Button(
                                        onClick = { currentScreen = "collection" },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF673AB7))
                                    ) { Text("🃏 Коллекция") }
                                }

                                CampaignScreen(
                                    onLevelSelect = { selectedLevel ->
                                        gameViewModel.startNewGame(selectedLevel)
                                        currentScreen = "game"
                                    }
                                )
                            }
                        }
                        "game" -> {
                            GameScreen(viewModel = gameViewModel, onBackToMenu = { currentScreen = "campaign" })
                        }
                        "shop" -> {
                            com.example.cardsandshades.ui.booster.BoosterScreen(onBack = { currentScreen = "campaign" })
                        }
                        "collection" -> {
                            com.example.cardsandshades.ui.collection.CollectionScreen(onBack = { currentScreen = "campaign" })
                        }
                    }
                }
            }
        }
    }
}