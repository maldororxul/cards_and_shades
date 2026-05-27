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
import androidx.compose.ui.draw.clip
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

data class SortState(
    val sortByRarity: Boolean = true,
    val ascending: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
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
    if (!isInitialized) {
        currentDeck.clear()
        currentDeck.addAll(userDeck)
        isInitialized = true
    }

    LaunchedEffect(currentDeck.size, currentDeck.toList()) {
        userDeck.clear()
        userDeck.addAll(currentDeck)
        UserProfile.save()
    }

    var filterState by remember { mutableStateOf(FilterState()) }
    var filtersExpanded by remember { mutableStateOf(false) }
    
    var sortState by remember { mutableStateOf(SortState()) }
    var sortExpanded by remember { mutableStateOf(false) }
    
    var myDeckExpanded by remember { mutableStateOf(true) } // OPEN BY DEFAULT

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // MY DECK ACCORDION
        Accordion(
            title = "My Deck (${currentDeck.size}/20)",
            isExpanded = myDeckExpanded,
            onToggle = { myDeckExpanded = !myDeckExpanded }
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameButton(
                        text = stringResource(R.string.auto_deck),
                        onClick = {
                            val autoDeck = generateAutoDeck(userCards)
                            if (autoDeck.isNotEmpty()) {
                                currentDeck.clear()
                                currentDeck.addAll(autoDeck)
                            }
                        },
                        containerColor = Color(0xFF673AB7),
                        modifier = Modifier.weight(1f)
                    )

                    GameButton(
                        text = stringResource(R.string.clear),
                        onClick = { currentDeck.clear() },
                        containerColor = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // FULL VISUALS IN DECK
                val deckRows = currentDeck.chunked(3)
                deckRows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { card ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CardComponent(
                                        card = card,
                                        isPreview = true,
                                        modifier = Modifier.size(90.dp, 125.dp),
                                        onClick = { currentDeck.remove(card) }
                                    )
                                    GameText("- Remove", fontSize = 8.sp, color = Color.Red, modifier = Modifier.clickable { currentDeck.remove(card) })
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FILTERS ACCORDION
        Accordion(
            title = stringResource(R.string.filters),
            isExpanded = filtersExpanded,
            onToggle = { filtersExpanded = !filtersExpanded }
        ) {
            Column {
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

        // SORTING ACCORDION
        Accordion(
            title = "Sorting",
            isExpanded = sortExpanded,
            onToggle = { sortExpanded = !sortExpanded }
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortState.ascending,
                        onClick = { sortState = sortState.copy(ascending = true) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan)
                    )
                    GameText("Ascending", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = !sortState.ascending,
                        onClick = { sortState = sortState.copy(ascending = false) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan)
                    )
                    GameText("Descending", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LIST OF CARDS
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
        
        val sortedTemplates = remember(filteredTemplates, sortState) {
            if (sortState.ascending) filteredTemplates.sortedBy { it.rarity.ordinal }
            else filteredTemplates.sortedByDescending { it.rarity.ordinal }
        }

        val templatesByRarity = remember(sortedTemplates) {
            sortedTemplates.groupBy { it.rarity }
        }

        var inspectedCardsList by remember { mutableStateOf<List<CardModel>>(emptyList()) }
        var initialInspectedIndex by remember { mutableIntStateOf(0) }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            templatesByRarity.forEach { (rarity, templates) ->
                item {
                    RaritySection(
                        rarity = rarity,
                        templates = templates,
                        userCards = userCards,
                        currentDeck = currentDeck,
                        onCardClick = { card ->
                            // FIX: Use the ACTUAL sorted/filtered list of instances for paging
                            inspectedCardsList = sortedTemplates.map { t ->
                                userCards.find { it.name == t.name } ?: CardCatalog.createCardInstance(t.name)!!
                            }
                            initialInspectedIndex = sortedTemplates.indexOfFirst { it.name == card.name }.coerceAtLeast(0)
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
        
        if (inspectedCardsList.isNotEmpty()) {
            CardInspectionDialog(
                cards = inspectedCardsList,
                initialIndex = initialInspectedIndex,
                onDismiss = { inspectedCardsList = emptyList() }
            )
        }
    }
}

@Composable
private fun Accordion(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            GameText(if (isExpanded) "▲" else "▼", color = Color.Gray)
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FilterDropdownRow(state: FilterState, onFilterChange: (FilterState) -> Unit) {
    val allText = stringResource(R.string.all)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdown(
                label = stringResource(R.string.filter_rarity),
                selected = state.selectedRarity?.name ?: allText,
                options = listOf(allText) + Rarity.entries.map { it.name },
                onSelect = { 
                    onFilterChange(state.copy(selectedRarity = if (it == allText) null else Rarity.valueOf(it)))
                },
                modifier = Modifier.weight(1f)
            )
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
             FilterDropdown(
                 label = stringResource(R.string.filter_effect),
                 selected = state.selectedEffect?.name ?: allText,
                 options = listOf(allText) + EffectTag.entries.map { it.name },
                 onSelect = {
                     onFilterChange(state.copy(selectedEffect = if (it == allText) null else EffectTag.valueOf(it)))
                 },
                 modifier = Modifier.weight(1f)
             )
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
    val availableGroups = userCards.groupBy { it.name }.mapValues { it.value.toMutableList() }
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
    var expanded by remember { mutableStateOf(false) } // DEFAULT CLOSED
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
