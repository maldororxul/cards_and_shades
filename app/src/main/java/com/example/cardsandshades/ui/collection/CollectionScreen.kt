package com.example.cardsandshades.ui.collection

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.cardsandshades.R
import com.example.cardsandshades.catalog.CardCatalog
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.Rarity
import com.example.cardsandshades.model.GroupTag
import com.example.cardsandshades.model.EffectTag
import com.example.cardsandshades.model.UserProfile
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.utils.getStringResourceByName

data class FilterState(
    val showOwnedOnly: Boolean = true,
    val selectedRarity: Rarity? = null,
    val selectedGroup: GroupTag? = null,
    val selectedEffect: EffectTag? = null,
    val selectedMana: Int? = null
)

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CollectionScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userCards = UserProfile.collection
    val userDeck = UserProfile.selectedDeck

    val currentDeck = remember { mutableStateListOf<CardModel>() }
    var isInitialized by remember { mutableStateOf(false) }
    
    if (!isInitialized && userDeck.isNotEmpty()) {
        currentDeck.clear()
        currentDeck.addAll(userDeck)
        isInitialized = true
    }

    val deckCountLabel = stringResource(R.string.deck_count, currentDeck.size)
    var statusMessage by remember { mutableStateOf(deckCountLabel) }
    
    LaunchedEffect(currentDeck.size) {
        if (!statusMessage.contains("✅") && !statusMessage.contains("❌")) {
            statusMessage = context.getString(R.string.deck_count, currentDeck.size)
        }
    }

    val dustC by UserProfile.dustCommon.collectAsState()
    val dustU by UserProfile.dustUncommon.collectAsState()
    val dustR by UserProfile.dustRare.collectAsState()
    val dustE by UserProfile.dustEpic.collectAsState()
    val dustL by UserProfile.dustLegendary.collectAsState()
    val dustM by UserProfile.dustMythic.collectAsState()

    var filterState by remember { mutableStateOf(FilterState()) }
    var filtersExpanded by remember { mutableStateOf(false) }

    // ГРУППИРОВКА ВСЕХ ШАБЛОНОВ С УЧЕТОМ ФИЛЬТРОВ
    val allTemplates = CardCatalog.templates
    val filteredTemplates = remember(filterState, userCards.size) {
        allTemplates.filter { t ->
            val owned = userCards.any { it.name == t.name }
            val passesOwned = !filterState.showOwnedOnly || owned
            val passesRarity = filterState.selectedRarity == null || t.rarity == filterState.selectedRarity
            val passesGroup = filterState.selectedGroup == null || t.groupTags.contains(filterState.selectedGroup)
            val passesEffect = filterState.selectedEffect == null || t.effectTags.contains(filterState.selectedEffect)
            val passesMana = filterState.selectedMana == null || t.manaCost == filterState.selectedMana
            
            passesOwned && passesRarity && passesGroup && passesEffect && passesMana
        }
    }

    val templatesByRarity = remember(filteredTemplates) {
        Rarity.entries.associateWith { rarity -> 
            filteredTemplates.filter { it.rarity == rarity }
        }
    }

    var inspectedCardsList by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var initialInspectedIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ВЕРХНЯЯ ПАНЕЛЬ: КНОПКИ ДЕЙСТВИЙ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameButton(
                text = stringResource(R.string.save),
                onClick = {
                    if (currentDeck.isNotEmpty()) {
                        userDeck.clear()
                        userDeck.addAll(currentDeck)
                        UserProfile.save()
                        statusMessage = context.getString(R.string.deck_saved)
                    } else {
                        statusMessage = context.getString(R.string.deck_error)
                    }
                },
                containerColor = if (currentDeck.isNotEmpty()) Color(0xFF388E3C) else Color.Gray,
                modifier = Modifier.weight(1f)
            )

            GameButton(
                text = stringResource(R.string.auto_deck),
                onClick = {
                    val autoDeck = generateAutoDeck(userCards)
                    if (autoDeck.isNotEmpty()) {
                        currentDeck.clear()
                        currentDeck.addAll(autoDeck)
                        // No status message as requested
                    } else {
                        statusMessage = context.getString(R.string.auto_deck_fail)
                    }
                },
                containerColor = Color(0xFF673AB7),
                modifier = Modifier.weight(1f)
            )

            GameButton(
                text = stringResource(R.string.clear),
                onClick = {
                    currentDeck.clear()
                    statusMessage = context.getString(R.string.deck_count, 0)
                },
                containerColor = Color(0xFFD32F2F),
                modifier = Modifier.weight(0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        GameText(
            text = statusMessage,
            color = if (statusMessage.contains("❌")) Color.Red else Color.Yellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // ФИЛЬТРЫ (Аккордеон)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameText(stringResource(R.string.filters), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                GameText(if (filtersExpanded) "▲" else "▼", color = Color.Gray)
            }

            if (filtersExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = filterState.showOwnedOnly,
                        onCheckedChange = { filterState = filterState.copy(showOwnedOnly = it) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                    )
                    GameText(stringResource(R.string.owned_only), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                FilterDropdownRow(filterState, onFilterChange = { filterState = it })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { filterState = FilterState() }) {
                        GameText(stringResource(R.string.reset_filters), color = Color.Cyan, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // ПАНЕЛЬ ПЫЛИ
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DustInfo(Color.White, dustC)
                DustInfo(Color.Green, dustU)
                DustInfo(Color(0xFF2196F3), dustR)
                DustInfo(Color(0xFF9C27B0), dustE)
                DustInfo(Color.Yellow, dustL)
                DustInfo(Color.Red, dustM)
            }
            
            GameButton(
                text = stringResource(R.string.dust_extras, userCards.groupBy { it.name }.values.sumOf { if (it.size > 2) it.size - 2 else 0 }),
                onClick = { UserProfile.dustExtras() },
                containerColor = Color(0xFF5D4037),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // СПИСОК КАРТ С АККОРДЕОНАМИ ПО РЕДКОСТИ
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            templatesByRarity.forEach { (rarity, templates) ->
                if (templates.isNotEmpty()) {
                    item {
                        RaritySection(
                            rarity = rarity,
                            templates = templates,
                            userCards = userCards,
                            currentDeck = currentDeck,
                            onCardClick = { card ->
                                inspectedCardsList = filteredTemplates.map { t ->
                                    userCards.find { it.name == t.name } ?: CardCatalog.createCardInstance(t.name)!!
                                }
                                initialInspectedIndex = filteredTemplates.indexOfFirst { it.name == card.name }
                            },
                            onDeckAdd = { card -> 
                                val template = CardCatalog.templates.find { it.name == card.name }
                                val limit = template?.deckLimit ?: 3
                                if (currentDeck.size < 20 && currentDeck.count { it.name == card.name } < limit) {
                                    currentDeck.add(card.copy(id = java.util.UUID.randomUUID().toString()))
                                }
                            },
                            onDeckRemove = { cardName ->
                                val toRemove = currentDeck.find { it.name == cardName }
                                if (toRemove != null) currentDeck.remove(toRemove)
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(70.dp))
    }

    if (inspectedCardsList.isNotEmpty()) {
        CardInspectionDialog(
            cards = inspectedCardsList,
            initialIndex = initialInspectedIndex,
            onDismiss = { inspectedCardsList = emptyList() }
        )
    }
}

@Composable
private fun FilterDropdownRow(state: FilterState, onFilterChange: (FilterState) -> Unit) {
    val allText = stringResource(R.string.all)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Rarity
            FilterDropdown(
                label = stringResource(R.string.filter_rarity),
                selected = state.selectedRarity?.name ?: allText,
                options = listOf(allText) + Rarity.entries.map { it.name },
                onSelect = { 
                    onFilterChange(state.copy(selectedRarity = if (it == allText) null else Rarity.valueOf(it)))
                },
                modifier = Modifier.weight(1f)
            )
            // Group
            FilterDropdown(
                label = stringResource(R.string.filter_tag),
                selected = state.selectedGroup?.name ?: allText,
                options = listOf(allText) + GroupTag.entries.map { it.name },
                onSelect = {
                    onFilterChange(state.copy(selectedGroup = if (it == allText) null else GroupTag.valueOf(it)))
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             // Effect
             FilterDropdown(
                 label = stringResource(R.string.filter_effect),
                 selected = state.selectedEffect?.name ?: allText,
                 options = listOf(allText) + EffectTag.entries.map { it.name },
                 onSelect = {
                     onFilterChange(state.copy(selectedEffect = if (it == allText) null else EffectTag.valueOf(it)))
                 },
                 modifier = Modifier.weight(1f)
             )
             // Mana
             FilterDropdown(
                 label = stringResource(R.string.filter_mana),
                 selected = state.selectedMana?.toString() ?: allText,
                 options = listOf(allText) + (0..10).map { it.toString() },
                 onSelect = {
                     onFilterChange(state.copy(selectedMana = if (it == allText) null else it.toInt()))
                 },
                 modifier = Modifier.weight(1f)
             )
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Column {
            GameText(label, fontSize = 10.sp, color = Color.Gray)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .clickable { expanded = true }
                    .padding(8.dp)
            ) {
                GameText(selected, fontSize = 12.sp, maxLines = 1)
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1A1A1A)).border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { GameText(option, fontSize = 12.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun generateAutoDeck(userCards: List<CardModel>): List<CardModel> {
    if (userCards.isEmpty()) return emptyList()

    val deck = mutableListOf<CardModel>()
    
    // Группируем карты по именам для контроля лимита
    val availableGroups = userCards.groupBy { it.name }.mapValues { it.value.toMutableList() }
    
    // Сортируем все уникальные карты по "ценности"
    val sortedTemplates = CardCatalog.templates
        .filter { t -> userCards.any { it.name == t.name } }
        .sortedByDescending { t ->
            val rarityVal = t.rarity.ordinal * 20
            val statVal = t.baseAttack + t.baseHealth
            rarityVal + statVal
        }

    for (template in sortedTemplates) {
        if (deck.size >= 20) break
        
        val limit = template.deckLimit
        val ownedInstances = availableGroups[template.name] ?: continue
        
        repeat(limit) {
            if (deck.size < 20 && ownedInstances.isNotEmpty()) {
                val card = ownedInstances.removeAt(0)
                val newCard = card.copy(id = java.util.UUID.randomUUID().toString())
                deck.add(newCard)
            }
        }
    }
    
    return deck
}

@Composable
private fun RaritySection(
    rarity: Rarity,
    templates: List<com.example.cardsandshades.catalog.CardTemplate>,
    userCards: List<CardModel>,
    currentDeck: List<CardModel>,
    onCardClick: (CardModel) -> Unit,
    onDeckAdd: (CardModel) -> Unit,
    onDeckRemove: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(rarity == Rarity.COMMON) } 

    val rarityColor = when (rarity) {
        Rarity.COMMON -> Color.White
        Rarity.UNCOMMON -> Color.Green
        Rarity.RARE -> Color(0xFF2196F3)
        Rarity.EPIC -> Color(0xFF9C27B0)
        Rarity.LEGENDARY -> Color.Yellow
        Rarity.MYTHIC -> Color.Red
    }

    val ownedCountInRarity = templates.count { t -> userCards.any { it.name == t.name } }
    val totalInRarity = templates.size

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(rarityColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(rarity.name, color = rarityColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            GameText("$ownedCountInRarity / $totalInRarity", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            GameText(if (expanded) "▲" else "▼", color = rarityColor)
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            val rows = templates.chunked(3) 
            rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { template ->
                        val ownedCards = userCards.filter { it.name == template.name }
                        val ownedCount = ownedCards.size
                        val inDeckCount = currentDeck.count { it.name == template.name }
                        val isOwned = ownedCount > 0
                        
                        val limit = template.deckLimit
                        
                        val displayCard = ownedCards.firstOrNull() ?: CardCatalog.createCardInstance(template.name)!!

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(contentAlignment = Alignment.BottomCenter) {
                                    Box(
                                        modifier = Modifier
                                            .graphicsLayer { alpha = if (inDeckCount >= limit) 0.5f else 1.0f }
                                            .then(if (!isOwned) Modifier.blur(8.dp) else Modifier)
                                    ) {
                                        CardComponent(
                                            card = displayCard,
                                            isPreview = true,
                                            modifier = Modifier.size(105.dp, 145.dp),
                                            onClick = { onCardClick(displayCard) }
                                        )
                                        if (!isOwned) {
                                            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)))
                                        }
                                    }
                                    
                                    // СТАТИСТИКА ВЛАДЕНИЯ И КОЛОДЫ
                                    Column(
                                        modifier = Modifier.padding(bottom = 2.dp).background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        GameText("Owned: $ownedCount", fontSize = 8.sp, color = Color.Gray)
                                        GameText("Deck: ${inDeckCount}/$limit", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                if (isOwned) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        if (inDeckCount > 0) {
                                            IconButton(onClick = { onDeckRemove(template.name) }, modifier = Modifier.size(24.dp)) {
                                                GameText("-", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (inDeckCount < limit && ownedCount > inDeckCount) {
                                            IconButton(onClick = { onDeckAdd(displayCard) }, modifier = Modifier.size(24.dp)) {
                                                GameText("+", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DustInfo(color: Color, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(3.dp))
        GameText(text = amount.toString(), fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
