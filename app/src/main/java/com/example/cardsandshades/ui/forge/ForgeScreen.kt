package com.example.cardsandshades.ui.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText

@Composable
fun ForgeScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.forge), stringResource(R.string.merge_dust))

    val dustC by UserProfile.dustCommon.collectAsState()
    val dustR by UserProfile.dustRare.collectAsState()
    val dustE by UserProfile.dustEpic.collectAsState()
    val dustL by UserProfile.dustLegendary.collectAsState()

    var forgedCard by remember { mutableStateOf<CardModel?>(null) }
    
    val welcomeMsg = stringResource(R.string.forge_welcome)
    var message by remember { mutableStateOf(welcomeMsg) }
    
    val mergeFail = stringResource(R.string.forge_merge_fail)
    val craftFail = stringResource(R.string.forge_craft_fail)
    val commonLabel = stringResource(R.string.rarity_common)
    val rareLabel = stringResource(R.string.rarity_rare)
    val epicLabel = stringResource(R.string.rarity_epic)
    val legendaryLabel = stringResource(R.string.rarity_legendary)
    val craftSuccess = stringResource(R.string.forge_craft_success)
    val forgeLegendaryReady = stringResource(R.string.forge_legendary_ready)
    val mergeSuccess = stringResource(R.string.forge_merge_success)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameText(stringResource(R.string.forge_title), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF673AB7)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { GameText(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            // ТАБ 1: КОВКА КАРТ
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GameText(stringResource(R.string.forge_desc), fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(180.dp, 250.dp)
                        .border(2.dp, Color(0xFF424242).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (forgedCard != null) {
                        CardComponent(card = forgedCard!!)
                    } else {
                        GameText(stringResource(R.string.forge_awaiting), color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                GameText(text = message, color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(24.dp))

                ForgeRow(Rarity.COMMON, Color.Gray, dustC, 40) {
                    if (UserProfile.craftCard(Rarity.COMMON)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(commonLabel)
                    } else message = craftFail
                }
                Spacer(modifier = Modifier.height(8.dp))
                ForgeRow(Rarity.RARE, Color(0xFF1E88E5), dustR, 100) {
                    if (UserProfile.craftCard(Rarity.RARE)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(rareLabel)
                    } else message = craftFail
                }
                Spacer(modifier = Modifier.height(8.dp))
                ForgeRow(Rarity.EPIC, Color(0xFF8E24AA), dustE, 400) {
                    if (UserProfile.craftCard(Rarity.EPIC)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(epicLabel)
                    } else message = craftFail
                }
                Spacer(modifier = Modifier.height(8.dp))
                ForgeRow(Rarity.LEGENDARY, Color(0xFFFDD835), dustL, 1600) {
                    if (UserProfile.craftCard(Rarity.LEGENDARY)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = forgeLegendaryReady
                    } else message = craftFail
                }
            }
        } else {
            // ТАБ 2: СЛИЯНИЕ ПЫЛИ
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GameText(stringResource(R.string.merge_dust_desc), fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))

                MergeRow(commonLabel, rareLabel, Color.Gray, Color(0xFF1E88E5), dustC) {
                    if (UserProfile.mergeDust(Rarity.COMMON)) message = mergeSuccess.format(commonLabel, rareLabel)
                    else message = mergeFail
                }
                Spacer(modifier = Modifier.height(16.dp))
                MergeRow(rareLabel, epicLabel, Color(0xFF1E88E5), Color(0xFF8E24AA), dustR) {
                    if (UserProfile.mergeDust(Rarity.RARE)) message = mergeSuccess.format(rareLabel, epicLabel)
                    else message = mergeFail
                }
                Spacer(modifier = Modifier.height(16.dp))
                MergeRow(epicLabel, legendaryLabel, Color(0xFF8E24AA), Color(0xFFFDD835), dustE) {
                    if (UserProfile.mergeDust(Rarity.EPIC)) message = mergeSuccess.format(epicLabel, legendaryLabel)
                    else message = mergeFail
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                GameText(text = message, color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun ForgeRow(rarity: Rarity, color: Color, dust: Int, cost: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val rarityLabel = when(rarity) {
                Rarity.COMMON -> stringResource(R.string.rarity_common)
                Rarity.RARE -> stringResource(R.string.rarity_rare)
                Rarity.EPIC -> stringResource(R.string.rarity_epic)
                Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
            }
            GameText(rarityLabel, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            GameText(stringResource(R.string.dust_label, dust), fontSize = 12.sp, color = if (dust >= cost) Color.Green else Color.Gray)
        }
        GameButton(text = stringResource(R.string.forge_craft_btn, cost), onClick = onClick, enabled = dust >= cost, containerColor = if (dust >= cost) color else Color.DarkGray, fontSize = 10.sp)
    }
}

@Composable
private fun MergeRow(from: String, to: String, colorFrom: Color, colorTo: Color, dust: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameText("100 ", color = colorFrom, fontWeight = FontWeight.Bold)
            GameText(from, fontSize = 12.sp)
            GameText(" ➔ ", color = Color.Gray)
            GameText("10 ", color = colorTo, fontWeight = FontWeight.Bold)
            GameText(to, fontSize = 12.sp)
        }
        GameButton(text = stringResource(R.string.forge_merge_btn), onClick = onClick, enabled = dust >= 100, containerColor = Color(0xFF4E342E), fontSize = 10.sp)
    }
}
