package com.example.cardsandshades.ui.settings

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.sound.SoundManager
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameBackground
import com.example.cardsandshades.utils.changeLocale

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var musicVol by remember { mutableFloatStateOf(SoundManager.musicVolume) }
    var soundVol by remember { mutableFloatStateOf(SoundManager.soundVolume) }
    var expanded by remember { mutableStateOf(false) }

    val locales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!locales.isEmpty) locales[0]?.language ?: "en" else "en"

    BackHandler {
        onBack()
    }

    GameBackground(screenId = "settings") {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameButton(
                    text = stringResource(id = R.string.back),
                    onClick = onBack
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GameText(text = stringResource(id = R.string.settings), fontSize = 28.sp, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            GameText(
                text = stringResource(R.string.music, (musicVol * 100).toInt()),
                color = Color.White,
                fontSize = 18.sp
            )
            Slider(
                value = musicVol,
                onValueChange = {
                    musicVol = it
                    SoundManager.updateMusicVolume(context, it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            GameText(
                text = stringResource(R.string.sounds, (soundVol * 100).toInt()),
                color = Color.White,
                fontSize = 18.sp
            )
            Slider(
                value = soundVol,
                onValueChange = {
                    soundVol = it
                    SoundManager.updateSoundVolume(context, it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box {
                val currentLangName = when(currentLocale) {
                    "es" -> stringResource(R.string.lang_es)
                    "ru" -> stringResource(R.string.lang_ru)
                    else -> stringResource(R.string.lang_en)
                }

                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    GameText(text = stringResource(R.string.lang_selection_format, stringResource(R.string.language), currentLangName))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    DropdownMenuItem(
                        text = { GameText(stringResource(R.string.lang_en), color = Color.White) },
                        onClick = {
                            expanded = false
                            changeLocale(context = context, langCode = "en")
                        }
                    )
                    DropdownMenuItem(
                        text = { GameText(stringResource(R.string.lang_es), color = Color.White) },
                        onClick = {
                            expanded = false
                            changeLocale(context = context, langCode = "es")
                        }
                    )
                    DropdownMenuItem(
                        text = { GameText(stringResource(R.string.lang_ru), color = Color.White) },
                        onClick = {
                            expanded = false
                            changeLocale(context = context, langCode = "ru")
                        }
                    )
                }
            }
        }
    }
}
