package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.GameText

@Composable
fun EnemyBoardZone(
    boardCards: List<CardModel>,
    onCardPositioned: (String, Offset) -> Unit,
    onCardClick: (CardModel) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Increased height to match player board logic
            .border(2.dp, Color(0xFF3A2323).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (boardCards.isEmpty()) {
            GameText(stringResource(R.string.enemy_board_empty), color = Color.DarkGray, fontSize = 12.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                items(boardCards, key = { "opp_${it.id}" }) { enemyCard ->
                    CardComponent(
                        card = enemyCard,
                        modifier = Modifier
                            .size(105.dp, 150.dp) // FIXED: Standard size, no stretching
                            .padding(4.dp)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onCardPositioned(enemyCard.id, Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            },
                        onClick = { onCardClick(enemyCard) }
                    )
                }
            }
        }
    }
}
