package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.ui.components.GameText

import androidx.compose.ui.graphics.Brush

@Composable
fun BattleLogZone(battleLog: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Transparent)
                )
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val cleanLog = if (battleLog.startsWith("card_") || battleLog == "draw") {
            com.example.cardsandshades.utils.getStringResourceByName(context, battleLog)
        } else {
            battleLog
        }
        
        GameText(
            text = cleanLog,
            color = if (battleLog.contains("❌")) Color.Red else Color(0xFFFFEB3B),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
