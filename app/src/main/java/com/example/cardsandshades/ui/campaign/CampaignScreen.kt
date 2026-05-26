package com.example.cardsandshades.ui.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.cardsandshades.catalog.CampaignCatalog
import com.example.cardsandshades.model.ChapterModel
import com.example.cardsandshades.model.LevelModel
import com.example.cardsandshades.model.RewardSetModel
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.utils.getStringResourceByName

@Composable
fun CampaignScreen(
    onLevelSelect: (LevelModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = CampaignCatalog.chapters
    val maxUnlocked = UserProfile.maxUnlockedLevel.collectAsState().value
    var selectedChapter by remember { mutableStateOf<ChapterModel?>(null) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedChapter != null) {
                IconButton(onClick = { selectedChapter = null }) {
                    GameText(stringResource(R.string.back_arrow), fontSize = 24.sp)
                }
            }
            GameText(
                text = if (selectedChapter != null) getStringResourceByName(context, selectedChapter!!.name) else stringResource(R.string.campaign),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // PADDING FOR BOTTOM NAV
        ) {
            if (selectedChapter == null) {
                items(chapters) { chapter ->
                    val isUnlocked = chapter.levels.first().id <= maxUnlocked
                    ChapterItem(chapter, isUnlocked) {
                        if (isUnlocked) selectedChapter = chapter
                    }
                }
            } else {
                items(selectedChapter!!.levels) { level ->
                    val isUnlocked = level.id <= maxUnlocked
                    val isCompleted = level.id < maxUnlocked
                    LevelItem(level, isUnlocked, isCompleted) {
                        if (isUnlocked) onLevelSelect(level)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(chapter: ChapterModel, isUnlocked: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUnlocked) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.7f))
            .border(2.dp, if (isUnlocked) Color.Cyan.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                GameText(getStringResourceByName(context, chapter.name), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray)
                GameText(text = stringResource(R.string.missions_count, chapter.levels.size), fontSize = 12.sp, color = Color.Gray)
            }
            if (!isUnlocked) {
                GameText(stringResource(R.string.locked_icon), fontSize = 20.sp)
            } else {
                GameText(stringResource(R.string.unlocked_icon), fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun LevelItem(level: LevelModel, isUnlocked: Boolean, isCompleted: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val bgColor = when {
        isCompleted -> Color(0xFF1B5E20).copy(alpha = 0.4f)
        isUnlocked -> Color(0xFF1E1E1E).copy(alpha = 0.8f)
        else -> Color.Black.copy(alpha = 0.6f)
    }
    
    val borderColor = if (isUnlocked && !isCompleted) Color.Yellow.copy(alpha = 0.6f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                GameText(getStringResourceByName(context, level.name), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray)
                GameText(getStringResourceByName(context, level.difficultyDescription), fontSize = 12.sp, color = Color.LightGray, maxLines = 2)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GameText(text = stringResource(R.string.reward_label), fontSize = 11.sp, color = Color.Gray)
                        RewardIcons(level.firstTimeReward)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (!isUnlocked) {
                    GameText(stringResource(R.string.locked_icon), fontSize = 18.sp)
                } else if (isCompleted) {
                    GameText(stringResource(R.string.completed_icon), fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun RewardIcons(rewards: RewardSetModel) {
    Row {
        if (rewards.gold > 0) GameText("🪙", fontSize = 14.sp)
        if (rewards.crystals > 0) GameText("💎", fontSize = 14.sp)
        if (rewards.dustCommon > 0) GameText("⚪", fontSize = 14.sp)
        if (rewards.cardName != null) GameText("🃏", fontSize = 14.sp)
    }
}
