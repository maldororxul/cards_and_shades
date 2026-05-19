package com.example.cardsandshades.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity

@Composable
fun CardComponent(
    card: CardModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Определяем цвет рамки в зависимости от редкости карты
    val borderColor = when (card.rarity) {
        Rarity.COMMON -> Color.Gray
        Rarity.RARE -> Color(0xFF1E88E5) // Синий
        Rarity.EPIC -> Color(0xFF8E24AA) // Фиолетовый
        Rarity.LEGENDARY -> Color(0xFFFDD835) // Золотой
    }

    // Подсвечиваем базовый цвет карты темным или слегка фэнтезийным оттенком
    val cardBgColor = Color(0xFF212121)

    Card(
        modifier = modifier
            .width(110.dp)
            .height(160.dp)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // 1. СТОИМОСТЬ МАНЫ (Верхний левый угол)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF0288D1), RoundedCornerShape(12.dp))
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.manaCost.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. ИМЯ КАРТЫ И РЕДКОСТЬ (Центр)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.rarity.name,
                    color = borderColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. АТАКА (Нижний левый угол)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFD32F2F), RoundedCornerShape(6.dp))
                    .align(Alignment.BottomStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.currentAttack.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. ЗДОРОВЬЕ (Нижний правый угол)
            // Подсвечиваем красным, если здоровье карты уменьшилось в бою
            val healthBgColor = if (card.currentHealth < card.baseHealth) Color(0xFF7B1FA2) else Color(0xFF388E3C)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(healthBgColor, RoundedCornerShape(6.dp))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.currentHealth.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}