package com.example.cardsandshades.ui.forge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameText(stringResource(R.string.forge_title), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        GameText(stringResource(R.string.forge_desc), fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // ЗОНА РЕЗУЛЬТАТА
        Box(
            modifier = Modifier
                .size(200.dp, 280.dp)
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
        GameText(text = message, color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.weight(1f))

        // ПАНЕЛЬ КОВКИ
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ForgeRow(Rarity.COMMON, Color.Gray, dustC, 40, true) { isMerge ->
                if (isMerge) {
                    if (UserProfile.mergeDust(Rarity.COMMON)) message = "Слияние: 100 $commonLabel -> 10 $rareLabel"
                    else message = mergeFail
                } else {
                    if (UserProfile.craftCard(Rarity.COMMON)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(commonLabel)
                    } else message = craftFail
                }
            }
            ForgeRow(Rarity.RARE, Color(0xFF1E88E5), dustR, 100, true) { isMerge ->
                if (isMerge) {
                    if (UserProfile.mergeDust(Rarity.RARE)) message = "Слияние: 100 $rareLabel -> 10 $epicLabel"
                    else message = mergeFail
                } else {
                    if (UserProfile.craftCard(Rarity.RARE)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(rareLabel)
                    } else message = craftFail
                }
            }
            ForgeRow(Rarity.EPIC, Color(0xFF8E24AA), dustE, 400, true) { isMerge ->
                if (isMerge) {
                    if (UserProfile.mergeDust(Rarity.EPIC)) message = "Слияние: 100 $epicLabel -> 10 $legendaryLabel"
                    else message = mergeFail
                } else {
                    if (UserProfile.craftCard(Rarity.EPIC)) {
                        forgedCard = UserProfile.collection.lastOrNull()
                        message = craftSuccess.format(epicLabel)
                    } else message = craftFail
                }
            }
            ForgeRow(Rarity.LEGENDARY, Color(0xFFFDD835), dustL, 1600, false) {
                if (UserProfile.craftCard(Rarity.LEGENDARY)) {
                    forgedCard = UserProfile.collection.lastOrNull()
                    message = forgeLegendaryReady
                } else message = craftFail
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp)) // Отступ для таббара
    }
}

@Composable
private fun ForgeRow(
    rarity: Rarity,
    color: Color,
    currentDust: Int,
    cost: Int,
    canMerge: Boolean,
    onAction: (isMerge: Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val rarityLabel = when(rarity) {
                Rarity.COMMON -> stringResource(R.string.rarity_common)
                Rarity.RARE -> stringResource(R.string.rarity_rare)
                Rarity.EPIC -> stringResource(R.string.rarity_epic)
                Rarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
            }
            GameText(text = rarityLabel, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            GameText(text = stringResource(R.string.dust_label, currentDust), fontSize = 12.sp, color = if (currentDust >= cost) Color.Green else Color.Gray)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (canMerge) {
                GameButton(
                    text = stringResource(R.string.forge_merge_btn),
                    onClick = { onAction(true) },
                    containerColor = Color(0xFF4E342E),
                    enabled = currentDust >= 100,
                    fontSize = 10.sp
                )
            }
            
            GameButton(
                text = stringResource(R.string.forge_craft_btn, cost),
                onClick = { onAction(false) },
                containerColor = if (currentDust >= cost) color else Color.DarkGray,
                enabled = currentDust >= cost,
                fontSize = 10.sp
            )
        }
    }
}
