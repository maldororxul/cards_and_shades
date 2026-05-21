package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.campaign.CampaignScreen
import com.example.cardsandshades.ui.game.GameScreen
import com.example.cardsandshades.ui.game.GameViewModel
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.theme.GameTypography

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем каталоги и БД
        com.example.cardsandshades.catalog.CardCatalog.init(this)
        com.example.cardsandshades.catalog.CampaignCatalog.init(this)
        com.example.cardsandshades.catalog.RewardsCatalog.init(this)
        UserProfile.initDatabase(this)

        // 1. Разрешаем приложению отрисовываться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Скрываем статус-бар и панель навигации (кнопки)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme(typography = GameTypography) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("campaign") }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // КОНТЕНТ ТЕКУЩЕГО ЭКРАНА
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    GameScreen(viewModel = gameViewModel, onBackToMenu = { currentScreen = "campaign" })
                                }
                                "shop" -> {
                                    com.example.cardsandshades.ui.booster.BoosterScreen()
                                }
                                "collection" -> {
                                    com.example.cardsandshades.ui.collection.CollectionScreen()
                                }
                                "rewards" -> {
                                    com.example.cardsandshades.ui.rewards.RewardsScreen()
                                }
                                "forge" -> {
                                    com.example.cardsandshades.ui.forge.ForgeScreen()
                                }
                            }
                        }

                        // НИЖНЯЯ ПАНЕЛЬ НАВИГАЦИИ (видна везде кроме боя)
                        if (currentScreen != "game") {
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onScreenChange = { currentScreen = it },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1A1A1A).copy(alpha = 0.95f), RoundedCornerShape(24.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIcon("🗺️", "campaign", currentScreen == "campaign", onScreenChange)
        NavIcon("🃏", "collection", currentScreen == "collection", onScreenChange)
        NavIcon("🏪", "shop", currentScreen == "shop", onScreenChange)
        NavIcon("🔨", "forge", currentScreen == "forge", onScreenChange)
        NavIcon("🎁", "rewards", currentScreen == "rewards", onScreenChange)
    }
}

@Composable
private fun NavIcon(icon: String, id: String, isSelected: Boolean, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (isSelected) Color(0xFF673AB7) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick(id) },
        contentAlignment = Alignment.Center
    ) {
        GameText(text = icon, fontSize = 24.sp)
    }
}
