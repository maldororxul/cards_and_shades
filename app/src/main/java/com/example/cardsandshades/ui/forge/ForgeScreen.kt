package com.example.cardsandshades.ui.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.forge), 
        stringResource(R.string.merge_dust),
        stringResource(R.string.disenchant) 
    )

    val dustC by UserProfile.dustCommon.collectAsState()
    val dustU by UserProfile.dustUncommon.collectAsState()
    val dustR by UserProfile.dustRare.collectAsState()
    val dustE by UserProfile.dustEpic.collectAsState()
    val dustL by UserProfile.dustLegendary.collectAsState()
    val dustM by UserProfile.dustMythic.collectAsState()

    val hammerC by UserProfile.hammerCommon.collectAsState()
    val hammerU by UserProfile.hammerUncommon.collectAsState()
    val hammerR by UserProfile.hammerRare.collectAsState()
    val hammerE by UserProfile.hammerEpic.collectAsState()
    val hammerL by UserProfile.hammerLegendary.collectAsState()
    val hammerM by UserProfile.hammerMythic.collectAsState()

    var forgedCard by remember { mutableStateOf<CardModel?>(null) }
    val welcomeMsg = stringResource(R.string.forge_welcome)
    var message by remember { mutableStateOf(welcomeMsg) }
    
    val craftSuccess = stringResource(R.string.forge_craft_success)
    val mergeSuccess = stringResource(R.string.forge_merge_success)
    val craftFail = stringResource(R.string.forge_craft_fail)
    val mergeFail = stringResource(R.string.forge_merge_fail)
    
    val rarityCommon = stringResource(R.string.rarity_common)
    val rarityUncommon = stringResource(R.string.rarity_uncommon)
    val rarityRare = stringResource(R.string.rarity_rare)
    val rarityEpic = stringResource(R.string.rarity_epic)
    val rarityLegendary = stringResource(R.string.rarity_legendary)
    val rarityMythic = stringResource(R.string.rarity_mythic)
    val forgeLegendaryReady = stringResource(R.string.forge_legendary_ready)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameText(stringResource(R.string.forge_title), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        
        Spacer(modifier = Modifier.height(12.dp))

        // RESOURCES (Dust & Hammers)
        Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DustChip("C", Color.White, dustC)
                DustChip("U", Color.Green, dustU)
                DustChip("R", Color(0xFF2196F3), dustR)
                DustChip("E", Color(0xFF9C27B0), dustE)
                DustChip("L", Color.Yellow, dustL)
                DustChip("M", Color.Red, dustM)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HammerChip(Color.White, hammerC)
                HammerChip(Color.Green, hammerU)
                HammerChip(Color(0xFF2196F3), hammerR)
                HammerChip(Color(0xFF9C27B0), hammerE)
                HammerChip(Color.Yellow, hammerL)
                HammerChip(Color.Red, hammerM)
            }
        }

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

        when (selectedTab) {
            0 -> { // FORGE
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    ForgeRow(Rarity.COMMON, Color.White, dustC, hammerC, 40) {
                        if (UserProfile.craftCard(Rarity.COMMON)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = craftSuccess.format(rarityCommon)
                        } else message = craftFail
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ForgeRow(Rarity.UNCOMMON, Color.Green, dustU, hammerU, 80) {
                        if (UserProfile.craftCard(Rarity.UNCOMMON)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = craftSuccess.format(rarityUncommon)
                        } else message = craftFail
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ForgeRow(Rarity.RARE, Color(0xFF2196F3), dustR, hammerR, 100) {
                        if (UserProfile.craftCard(Rarity.RARE)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = craftSuccess.format(rarityRare)
                        } else message = craftFail
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ForgeRow(Rarity.EPIC, Color(0xFF9C27B0), dustE, hammerE, 400) {
                        if (UserProfile.craftCard(Rarity.EPIC)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = craftSuccess.format(rarityEpic)
                        } else message = craftFail
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ForgeRow(Rarity.LEGENDARY, Color.Yellow, dustL, hammerL, 1600) {
                        if (UserProfile.craftCard(Rarity.LEGENDARY)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = forgeLegendaryReady
                        } else message = craftFail
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ForgeRow(Rarity.MYTHIC, Color.Red, dustM, hammerM, 5000) {
                        if (UserProfile.craftCard(Rarity.MYTHIC)) {
                            forgedCard = UserProfile.collection.lastOrNull()
                            message = craftSuccess.format(rarityMythic)
                        } else message = craftFail
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            1 -> { // MERGE
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameText(stringResource(R.string.merge_dust_desc), fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))

                    MergeRow(rarityCommon, rarityUncommon, Color.White, Color.Green, dustC) {
                        if (UserProfile.mergeDust(Rarity.COMMON)) message = mergeSuccess.format(rarityCommon, rarityUncommon)
                        else message = mergeFail
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MergeRow(rarityUncommon, rarityRare, Color.Green, Color(0xFF2196F3), dustU) {
                        if (UserProfile.mergeDust(Rarity.UNCOMMON)) message = mergeSuccess.format(rarityUncommon, rarityRare)
                        else message = mergeFail
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MergeRow(rarityRare, rarityEpic, Color(0xFF2196F3), Color(0xFF9C27B0), dustR) {
                        if (UserProfile.mergeDust(Rarity.RARE)) message = mergeSuccess.format(rarityRare, rarityEpic)
                        else message = mergeFail
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MergeRow(rarityEpic, rarityLegendary, Color(0xFF9C27B0), Color.Yellow, dustE) {
                        if (UserProfile.mergeDust(Rarity.EPIC)) message = mergeSuccess.format(rarityEpic, rarityLegendary)
                        else message = mergeFail
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MergeRow(rarityLegendary, rarityMythic, Color.Yellow, Color.Red, dustL) {
                        if (UserProfile.mergeDust(Rarity.LEGENDARY)) message = mergeSuccess.format(rarityLegendary, rarityMythic)
                        else message = mergeFail
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    GameText(text = message, color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            2 -> { // DISENCHANT
                val extraCards = UserProfile.collection.groupBy { it.name }.filter { it.value.size > 2 }
                val totalExtras = extraCards.values.sumOf { it.size - 2 }
                
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameText(stringResource(R.string.disenchant_desc), fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (totalExtras > 0) {
                        GameText(stringResource(R.string.dust_extras_found, totalExtras), color = Color.Cyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        GameButton(
                            text = "⚒️ " + stringResource(R.string.disenchant_all),
                            onClick = { 
                                UserProfile.dustExtras()
                                message = "Disenchanted extras for dust!"
                            },
                            containerColor = Color(0xFF5D4037)
                        )
                    } else {
                        GameText(stringResource(R.string.no_extras), color = Color.DarkGray)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    GameText(text = message, color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun DustChip(label: String, color: Color, amount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GameText(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        GameText(amount.toString(), color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun HammerChip(color: Color, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GameText("⚒️", fontSize = 10.sp)
        Spacer(modifier = Modifier.width(2.dp))
        GameText(amount.toString(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ForgeRow(rarity: Rarity, color: Color, dust: Int, hammers: Int, cost: Int, onClick: () -> Unit) {
    val canForge = dust >= cost && hammers >= 1
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val rarityLabel = when(rarity) {
                Rarity.COMMON -> stringResource(R.string.rarity_common)
                Rarity.UNCOMMON -> stringResource(R.string.rarity_uncommon)
                Rarity.RARE -> stringResource(R.string.rarity_rare)
                Rarity.EPIC -> stringResource(R.string.rarity_epic)
                Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
                Rarity.MYTHIC -> stringResource(R.string.rarity_mythic)
            }
            GameText(rarityLabel, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row {
                GameText(stringResource(R.string.dust_label, dust), fontSize = 11.sp, color = if (dust >= cost) Color.Green else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                GameText("⚒️ $hammers", fontSize = 11.sp, color = if (hammers >= 1) Color.Yellow else Color.Gray)
            }
        }
        GameButton(
            text = "⚒️ $cost", 
            onClick = onClick, 
            enabled = canForge, 
            containerColor = Color.DarkGray,
            fontSize = 12.sp
        )
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
        GameButton(
            text = "⚒️ MERGE", 
            onClick = onClick, 
            enabled = dust >= 100, 
            containerColor = Color(0xFF4E342E), 
            fontSize = 10.sp
        )
    }
}
