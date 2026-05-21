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
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.ui.components.GameButton

@Composable
fun CampaignScreen(
    onLevelSelect: (LevelModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedLevel by UserProfile.maxUnlockedLevel.collectAsState()
    var selectedChapter by remember { mutableStateOf<ChapterModel?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedChapter != null) {
                GameButton(text = "⬅️", onClick = { selectedChapter = null }, containerColor = Color.DarkGray, modifier = Modifier.size(50.dp, 40.dp))
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
                    
                    LevelItem(
                        level = level,
                        isUnlocked = isUnlocked,
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
            .background(if (isUnlocked) Color(0xFF1E1E1E) else Color(0xFF141414))
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
private fun LevelItem(level: LevelModel, isUnlocked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isUnlocked) Color(0xFF388E3C) else Color.DarkGray, RoundedCornerShape(12.dp))
            .background(if (isUnlocked) Color(0xFF1E1E1E) else Color(0xFF141414))
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
            }
        }
        
        if (!isUnlocked) {
            GameText("🔒", fontSize = 18.sp)
        }
    }
}
