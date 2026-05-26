package com.example.cardsandshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.ui.booster.BoosterScreen
import com.example.cardsandshades.ui.campaign.CampaignScreen
import com.example.cardsandshades.ui.collection.CollectionScreen
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameBackground
import com.example.cardsandshades.ui.forge.ForgeScreen
import com.example.cardsandshades.ui.game.GameScreen
import com.example.cardsandshades.ui.game.GameViewModel
import com.example.cardsandshades.ui.missions.MissionScreen
import com.example.cardsandshades.ui.rewards.RewardsScreen
import com.example.cardsandshades.ui.settings.SettingsScreen
import com.example.cardsandshades.ui.theme.CardsAndShadesTheme
import com.example.cardsandshades.utils.LocaleHelper

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        hideSystemUI()
        
        // Load language first
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

                // Handle music transitions
                LaunchedEffect(currentRoute) {
                    if (currentRoute != "battle" && currentRoute != null) {
                        SoundManager.startMusic(this@MainActivity)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute != "battle") {
                            BottomNavBar(
                                currentRoute = currentRoute ?: "campaign",
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "campaign",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("campaign") { 
                            GameBackground(screenId = "campaign") {
                                CampaignScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onLevelSelect = { level ->
                                        gameViewModel.startNewGame(level)
                                        navController.navigate("battle")
                                    }
                                )
                            }
                        }
                        composable("collection") { 
                            GameBackground(screenId = "collection") {
                                CollectionScreen(modifier = Modifier.padding(innerPadding)) 
                            }
                        }
                        composable("shop") { 
                            GameBackground(screenId = "shop") {
                                BoosterScreen(modifier = Modifier.padding(innerPadding)) 
                            }
                        }
                        composable("rewards") { 
                            GameBackground(screenId = "rewards") {
                                RewardsScreen(modifier = Modifier.padding(innerPadding)) 
                            }
                        }
                        composable("missions") { 
                            GameBackground(screenId = "missions") {
                                MissionScreen(modifier = Modifier.padding(innerPadding)) 
                            }
                        }
                        composable("forge") { 
                            GameBackground(screenId = "forge") {
                                ForgeScreen(modifier = Modifier.padding(innerPadding)) 
                            }
                        }
                        composable("settings") { 
                            GameBackground(screenId = "settings") {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    viewModel = gameViewModel,
                                    modifier = Modifier.padding(innerPadding)
                                ) 
                            }
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

    private fun hideSystemUI() {
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
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
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
            NavIcon("campaign", "🗺️", "Campaign", currentRoute == "campaign", onNavigate)
            NavIcon("collection", "🃏", "Cards", currentRoute == "collection", onNavigate)
            NavIcon("missions", "🎯", "Missions", currentRoute == "missions", onNavigate)
            NavIcon("shop", "🛍️", "Shop", currentRoute == "shop", onNavigate)
            NavIcon("forge", "⚒️", "Forge", currentRoute == "forge", onNavigate)
            NavIcon("rewards", "🎁", "Daily", currentRoute == "rewards", onNavigate)
            NavIcon("settings", "⚙️", "Settings", currentRoute == "settings", onNavigate)
        }
    }
}

@Composable
fun NavIcon(route: String, icon: String, label: String, isSelected: Boolean, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onNavigate(route) }
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
