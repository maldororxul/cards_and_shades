package com.example.cardsandshades.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.catalog.CampaignCatalog
import com.example.cardsandshades.model.ChapterModel
import com.example.cardsandshades.model.LevelModel
import com.example.cardsandshades.model.RewardSetModel
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameButton
import androidx.activity.compose.BackHandler

@Composable
fun CampaignScreen(
    onLevelSelect: (LevelModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedLevel by UserProfile.maxUnlockedLevel.collectAsState()
    var selectedChapter by remember { mutableStateOf<ChapterModel?>(null) }

    // ОБРАБОТКА СИСТЕМНОЙ КНОПКИ НАЗАД (Только если выбрана глава)
    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedChapter != null) {
                // КНОПКА НАЗАД ВНУТРИ ЭКРАНА
                GameButton(
                    text = "⬅️", 
                    onClick = { selectedChapter = null }, 
                    containerColor = Color.DarkGray, 
                    modifier = Modifier.size(50.dp, 40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            GameText(
                text = selectedChapter?.name ?: "Карта Кампании",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedChapter == null) {
            // СПИСОК ГЛАВ
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(CampaignCatalog.chapters) { chapter ->
                    val isChapterUnlocked = chapter.levels.any { it.id <= unlockedLevel }
                    
                    ChapterItem(
                        chapter = chapter,
                        isUnlocked = isChapterUnlocked,
                        onClick = { if (isChapterUnlocked) selectedChapter = chapter }
                    )
                }
            }
        } else {
            // СПИСОК МИССИЙ В ГЛАВЕ
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(selectedChapter!!.levels) { level ->
                    val isUnlocked = level.id <= unlockedLevel
                    val isCompleted = level.id < unlockedLevel
                    
                    LevelItem(
                        level = level,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        onClick = { if (isUnlocked) onLevelSelect(level) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun ChapterItem(chapter: ChapterModel, isUnlocked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isUnlocked) Color(0xFF673AB7) else Color.DarkGray, RoundedCornerShape(12.dp))
            .background(if (isUnlocked) Color(0xFF1E1E1E).copy(alpha = 0.8f) else Color(0xFF141414).copy(alpha = 0.8f))
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            GameText(text = chapter.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray)
            GameText(text = "${chapter.levels.size} Миссий", fontSize = 12.sp, color = Color.Gray)
        }
        
        if (!isUnlocked) {
            GameText("🔒", fontSize = 20.sp)
        } else {
            GameText("➡️", fontSize = 20.sp)
        }
    }
}

@Composable
private fun LevelItem(level: LevelModel, isUnlocked: Boolean, isCompleted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isUnlocked) Color(0xFF388E3C) else Color.DarkGray, RoundedCornerShape(12.dp))
            .background(if (isUnlocked) Color(0xFF1E1E1E).copy(alpha = 0.8f) else Color(0xFF141414).copy(alpha = 0.8f))
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            GameText(
                text = "${level.id}. ${level.name}",
                color = if (isUnlocked) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                GameText(text = level.difficultyDescription, color = Color.Gray, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // ОТОБРАЖЕНИЕ НАГРАД
                val currentRewards = if (isCompleted) level.repeatReward else level.firstTimeReward
                if (!currentRewards.isEmpty) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GameText(text = "Награда: ", fontSize = 11.sp, color = Color.LightGray)
                        RewardIcons(currentRewards)
                    }
                }
            }
        }
        
        if (!isUnlocked) {
            GameText("🔒", fontSize = 18.sp)
        } else if (isCompleted) {
            GameText("✅", fontSize = 18.sp)
        }
    }
}

@Composable
private fun RewardIcons(rewards: RewardSetModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (rewards.gold > 0) GameText("🪙${rewards.gold}", fontSize = 11.sp, color = Color.Yellow)
        if (rewards.crystals > 0) GameText("💎${rewards.crystals}", fontSize = 11.sp, color = Color(0xFF03A9F4))
        if (rewards.dustCommon > 0) GameText("⚪${rewards.dustCommon}", fontSize = 11.sp, color = Color.Gray)
        if (rewards.dustRare > 0) GameText("🔵${rewards.dustRare}", fontSize = 11.sp, color = Color(0xFF1E88E5))
        if (rewards.dustEpic > 0) GameText("🟣${rewards.dustEpic}", fontSize = 11.sp, color = Color(0xFF8E24AA))
        if (rewards.dustLegendary > 0) GameText("🟡${rewards.dustLegendary}", fontSize = 11.sp, color = Color(0xFFFDD835))
        if (rewards.cardName != null) GameText("🃏${rewards.cardName}", fontSize = 11.sp, color = Color(0xFF00E676))
    }
}
