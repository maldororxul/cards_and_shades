package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cardsandshades.catalog.*
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.model.MissionManager
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.ui.booster.BoosterScreen
import com.example.cardsandshades.ui.battle_menu.ToBattleScreen
import com.example.cardsandshades.ui.collection.CollectionScreen
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameBackground
import com.example.cardsandshades.ui.forge.ForgeScreen
import com.example.cardsandshades.ui.game.GameScreen
import com.example.cardsandshades.ui.game.GameViewModel
import com.example.cardsandshades.ui.rewards.RewardsScreen
import com.example.cardsandshades.ui.settings.SettingsScreen
import com.example.cardsandshades.ui.theme.CardsAndShadesTheme
import com.example.cardsandshades.utils.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        hideSystemUI()
        
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this))

        CardCatalog.init(this)
        FusionCatalog.init(this)
        BoosterCatalog.init(this)
        CampaignCatalog.init(this)
        AchievementCatalog.init(this)
        MissionCatalog.init(this)
        BackgroundCatalog.init(this)
        RewardsCatalog.init(this)
        UserProfile.initDatabase(this)
        SoundManager.init(this)

        setContent {
            CardsAndShadesTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // GLOBAL PLAYTIME TIMER
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000)
                        MissionManager.tickPlaytime(1)
                    }
                }

                LaunchedEffect(currentRoute) {
                    if (currentRoute != "battle" && currentRoute != null) {
                        SoundManager.startMusic(this@MainActivity)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main_menu",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("main_menu") {
                            MainMenuPager(navController, gameViewModel)
                        }
                        composable("battle") { 
                            GameScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = { navController.popBackStack() }
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        SoundManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
    }
}

@Composable
fun MainMenuPager(navController: androidx.navigation.NavController, gameViewModel: GameViewModel) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentIndex = pagerState.currentPage,
                onNavigate = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> GameBackground(screenId = "campaign") {
                    ToBattleScreen(
                        onLevelSelect = { level ->
                            gameViewModel.startNewGame(level)
                            navController.navigate("battle")
                        },
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    )
                }
                1 -> GameBackground(screenId = "collection") {
                    CollectionScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
                2 -> GameBackground(screenId = "shop") {
                    BoosterScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
                3 -> GameBackground(screenId = "forge") {
                    ForgeScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
                4 -> GameBackground(screenId = "rewards") {
                    RewardsScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
                5 -> GameBackground(screenId = "settings") {
                    SettingsScreen(
                        onBack = { /* Handled by system back */ },
                        viewModel = gameViewModel,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(currentIndex: Int, onNavigate: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp),
        color = Color.Black.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon(0, "⚔️", "Battle", currentIndex == 0, onNavigate)
            NavIcon(1, "🃏", "Cards", currentIndex == 1, onNavigate)
            NavIcon(2, "🛍️", "Shop", currentIndex == 2, onNavigate)
            NavIcon(3, "⚒️", "Forge", currentIndex == 3, onNavigate)
            NavIcon(4, "🎁", "Daily", currentIndex == 4, onNavigate)
            NavIcon(5, "⚙️", "Settings", currentIndex == 5, onNavigate)
        }
    }
}

@Composable
fun NavIcon(index: Int, icon: String, label: String, isSelected: Boolean, onNavigate: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onNavigate(index) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 40.dp else 34.dp)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = icon, fontSize = if (isSelected) 22.sp else 18.sp)
        }
        GameText(text = label, fontSize = 9.sp, color = if (isSelected) Color.White else Color.Gray)
    }
}
