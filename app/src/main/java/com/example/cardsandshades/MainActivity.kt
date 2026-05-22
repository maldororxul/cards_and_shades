package com.example.cardsandshades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import com.example.cardsandshades.ui.components.GameBackground
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.ui.settings.SettingsScreen
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем каталоги и БД
        com.example.cardsandshades.catalog.CardCatalog.init(this)
        com.example.cardsandshades.catalog.CampaignCatalog.init(this)
        com.example.cardsandshades.catalog.RewardsCatalog.init(this)
        com.example.cardsandshades.catalog.BoosterCatalog.init(this)
        com.example.cardsandshades.catalog.BackgroundCatalog.init(this)
        com.example.cardsandshades.catalog.FusionCatalog.init(this)
        com.example.cardsandshades.catalog.AchievementCatalog.init(this)
        UserProfile.initDatabase(this)
        SoundManager.init(this)
        SoundManager.startMusic(this)

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> SoundManager.pauseMusic()
                Lifecycle.Event.ON_RESUME -> SoundManager.resumeMusic()
                else -> {}
            }
        })

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
                    val screens = listOf("campaign", "collection", "shop", "forge", "rewards", "settings")
                    val pagerState = rememberPagerState(initialPage = 0, pageCount = { screens.size })
                    val scope = rememberCoroutineScope()
                    
                    val currentScreen = screens[pagerState.currentPage]

                    Box(modifier = Modifier.fillMaxSize()) {
                        // ЭФФЕКТ ЗАТЕМНЕНИЯ ПРИ СМЕНЕ ЭКРАНА (для кнопок навигации)
                        // При свайпах анимация и так есть в Pager
                        var isTransitioning by remember { mutableStateOf(false) }

                        if (gameViewModel.gameState.collectAsState().value != null) {
                            // ЭКРАН ИГРЫ (Оверлей)
                            GameScreen(viewModel = gameViewModel, onBackToMenu = { 
                                gameViewModel.claimRewardsAndExit(false) 
                            })
                        } else {
                    // КОНТЕНТ ТЕКУЩЕГО ЭКРАНА С ПЕЙДЖЕРОМ
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = gameViewModel.gameState.collectAsState().value == null
                    ) { page ->
                                val screen = screens[page]
                                GameBackground(screenId = screen) {
                                    when (screen) {
                                        "campaign" -> {
                                            CampaignScreen(
                                                onLevelSelect = { selectedLevel ->
                                                    gameViewModel.startNewGame(selectedLevel)
                                                }
                                            )
                                        }
                                        "collection" -> {
                                            com.example.cardsandshades.ui.collection.CollectionScreen()
                                        }
                                        "shop" -> {
                                            com.example.cardsandshades.ui.booster.BoosterScreen()
                                        }
                                        "forge" -> {
                                            com.example.cardsandshades.ui.forge.ForgeScreen()
                                        }
                                        "rewards" -> {
                                            com.example.cardsandshades.ui.rewards.RewardsScreen()
                                        }
                                        "settings" -> {
                                            SettingsScreen(onBack = { 
                                                scope.launch { pagerState.animateScrollToPage(0) }
                                            })
                                        }
                                    }
                                }
                            }

                            // ВИЗУАЛЬНЫЙ СЛОЙ ЗАТЕМНЕНИЯ (опционально)
                            AnimatedVisibility(
                                visible = isTransitioning,
                                enter = fadeIn(tween(200)),
                                exit = fadeOut(tween(200))
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                            }

                            // НИЖНЯЯ ПАНЕЛЬ НАВИГАЦИИ
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onScreenChange = { 
                                    scope.launch { pagerState.animateScrollToPage(screens.indexOf(it)) }
                                },
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
        NavIcon("🗺️", "campaign", stringResource(R.string.campaign), currentScreen == "campaign", onScreenChange)
        NavIcon("🃏", "collection", stringResource(R.string.collection), currentScreen == "collection", onScreenChange)
        NavIcon("🏪", "shop", stringResource(R.string.shop), currentScreen == "shop", onScreenChange)
        NavIcon("🔨", "forge", stringResource(R.string.forge), currentScreen == "forge", onScreenChange)
        NavIcon("🎁", "rewards", stringResource(R.string.rewards), currentScreen == "rewards", onScreenChange)
        NavIcon("⚙️", "settings", stringResource(R.string.settings), currentScreen == "settings", onScreenChange)
    }
}

@Composable
private fun NavIcon(icon: String, id: String, label: String, isSelected: Boolean, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(if (isSelected) Color(0xFF673AB7) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick(id) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameText(text = icon, fontSize = 20.sp)
            GameText(text = label, fontSize = 8.sp, color = if (isSelected) Color.White else Color.Gray)
        }
    }
}
