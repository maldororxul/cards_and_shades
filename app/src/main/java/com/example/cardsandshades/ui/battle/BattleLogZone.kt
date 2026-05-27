package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.model.LogEntry
import com.example.cardsandshades.model.LogType
import com.example.cardsandshades.ui.components.GameDialog
import com.example.cardsandshades.ui.components.GameText
import com.example.cardsandshades.utils.getStringResourceByName
import java.util.Locale

@Composable
fun BattleLogZone(
    battleLog: String,
    history: List<LogEntry> = emptyList(),
    onCardClick: (String) -> Unit = {}
) {
    var showDetailedLog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Transparent)
                )
            )
            .clickable { showDetailedLog = true }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val cleanLog = formatLogMessage(context, battleLog)
        
        GameText(
            text = cleanLog.text,
            color = if (battleLog.contains("❌")) Color.Red else Color(0xFFFFEB3B),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }

    if (showDetailedLog) {
        DetailedBattleLogDialog(
            history = history,
            onCardClick = onCardClick,
            onDismiss = { showDetailedLog = false }
        )
    }
}

@Composable
fun DetailedBattleLogDialog(
    history: List<LogEntry>,
    onCardClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.scrollToItem(history.size - 1)
        }
    }
    
    GameDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.battle_history),
        content = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(history) { entry ->
                        val color = when (entry.type) {
                            LogType.PLAYER -> Color(0xFF4CAF50)
                            LogType.OPPONENT -> Color(0xFFF44336)
                            LogType.SYSTEM -> Color(0xFF2196F3)
                        }
                        
                        val fontWeight = if (entry.type == LogType.SYSTEM) FontWeight.Black else FontWeight.Normal
                        val textAlign = if (entry.type == LogType.SYSTEM) TextAlign.Center else TextAlign.Start
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            val annotated = formatLogMessage(context, entry.message)
                            ClickableText(
                                text = annotated,
                                style = androidx.compose.ui.text.TextStyle(
                                    color = color,
                                    fontSize = 14.sp,
                                    fontWeight = fontWeight,
                                    textAlign = textAlign
                                ),
                                onClick = { offset ->
                                    annotated.getStringAnnotations(tag = "CARD", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onCardClick(annotation.item)
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun formatLogMessage(context: android.content.Context, message: String): AnnotatedString {
    if (message.contains("|")) {
        val parts = message.split("|")
        val templateName = parts[0]
        val args = parts.drop(1)
        
        val template = getStringResourceByName(context, templateName)
        
        // Split template by format specifiers %1$s, %2$s, etc.
        val regex = Regex("%[0-9]\\\$[sd]")
        val textParts = template.split(regex)
        val specifiers = regex.findAll(template).map { it.value }.toList()

        return buildAnnotatedString {
            textParts.forEachIndexed { index, text ->
                append(text)
                if (index < specifiers.size) {
                    val argIndex = specifiers[index][1].digitToInt() - 1
                    val argValue = args.getOrNull(argIndex) ?: ""
                    
                    if (argValue.startsWith("card_")) {
                        val cardName = getStringResourceByName(context, argValue)
                        withStyle(style = SpanStyle(color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)) {
                            pushStringAnnotation(tag = "CARD", annotation = argValue)
                            append(cardName)
                            pop()
                        }
                    } else if (argValue == "player") {
                        append(stringResource(R.string.player))
                    } else if (argValue == "opponent") {
                        append(stringResource(R.string.opponent))
                    } else {
                        append(argValue)
                    }
                }
            }
        }
    }

    return buildAnnotatedString {
        append(message)
    }
}
