package com.example.cardsandshades.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameDialog
import com.example.cardsandshades.ui.game.GameViewModel
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.BackHandler
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.catalog.PromoCodeCatalog
import com.example.cardsandshades.utils.changeLocale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    var musicVol by remember { mutableFloatStateOf(SoundManager.musicVolume) }
    var soundVol by remember { mutableFloatStateOf(SoundManager.soundVolume) }
    
    var showPromoDialog by remember { mutableStateOf(false) }
    var promoResult by remember { mutableStateOf<String?>(null) }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameText(stringResource(R.string.settings), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        
        Spacer(modifier = Modifier.height(24.dp))

        // ЗВУК И МУЗЫКА
        SettingsSection(title = stringResource(R.string.settings_audio)) {
            GameText(stringResource(R.string.music, (musicVol * 100).toInt()), fontSize = 14.sp)
            Slider(value = musicVol, onValueChange = { musicVol = it; SoundManager.updateMusicVolume(context, it) }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GameText(stringResource(R.string.sounds, (soundVol * 100).toInt()), fontSize = 14.sp)
            Slider(value = soundVol, onValueChange = { soundVol = it; SoundManager.updateSoundVolume(context, it) }, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // БОЙ
        SettingsSection(title = stringResource(R.string.settings_battle)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GameText(stringResource(R.string.settings_speed, viewModel.animationSpeed), fontSize = 14.sp)
                GameButton(text = stringResource(R.string.settings_cycle), onClick = { viewModel.cycleAnimationSpeed() }, modifier = Modifier.width(100.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GameText(stringResource(R.string.settings_auto_battle), fontSize = 14.sp)
                Switch(checked = viewModel.isAutoBattleActive, onCheckedChange = { viewModel.toggleAutoBattle() })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ЯЗЫК
        SettingsSection(title = stringResource(R.string.language)) {
            LanguageSelector()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ПРОМОКОДЫ
        SettingsSection(title = stringResource(R.string.settings_rewards)) {
            GameButton(text = stringResource(R.string.settings_promo_btn), onClick = { showPromoDialog = true }, modifier = Modifier.fillMaxWidth())
            if (promoResult != null) {
                GameText(promoResult!!, color = Color.Yellow, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        GameButton(text = stringResource(R.string.back), onClick = onBack, containerColor = Color.DarkGray, modifier = Modifier.fillMaxWidth())
        
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showPromoDialog) {
        var codeInput by remember { mutableStateOf("") }
        GameDialog(
            onDismiss = { showPromoDialog = false },
            title = stringResource(R.string.settings_promo_title),
            content = {
                Column {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text(stringResource(R.string.settings_promo_hint), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF673AB7),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            },
            confirmButton = { onAction ->
                GameButton(text = stringResource(R.string.settings_apply), onClick = {
                    val result = PromoCodeCatalog.applyCode(context, codeInput)
                    promoResult = result
                    onAction()
                })
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        GameText(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun LanguageSelector() {
    val context = LocalContext.current
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangCode = if (currentLocales.isEmpty) "en" else currentLocales[0]?.language ?: "en"
    
    var expanded by remember { mutableStateOf(false) }
    
    val languages = listOf(
        "en" to stringResource(R.string.lang_en),
        "ru" to stringResource(R.string.lang_ru),
        "es" to stringResource(R.string.lang_es)
    )
    
    val currentLangName = languages.find { it.first == currentLangCode }?.second ?: languages[0].second

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameText(currentLangName, fontSize = 14.sp)
                GameText("▼", color = Color.Gray, fontSize = 12.sp)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        GameText(
                            text = name,
                            fontSize = 14.sp,
                            color = if (code == currentLangCode) Color.Yellow else Color.White
                        )
                    },
                    onClick = {
                        expanded = false
                        changeLocale(context, code)
                    }
                )
            }
        }
    }
}
