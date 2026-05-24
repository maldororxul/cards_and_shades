package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.GameText

@Composable
fun EnemyBoardZone(
    boardSlots: Array<CardModel?>,
    onCardPositioned: (String, Offset) -> Unit,
    onCardClick: (CardModel) -> Unit,
    onCardLongClick: (CardModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        boardSlots.forEachIndexed { index, card ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (card == null) {
                    // ПУСТОЙ СЛОТ ОППОНЕНТА
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = Color.Black.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(
                            text = (index + 1).toString(),
                            color = Color.DarkGray.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // КАРТА ОППОНЕНТА
                    CardComponent(
                        card = card,
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onCardPositioned(card.id, Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            },
                        onClick = { onCardClick(card) },
                        onLongClick = { onCardLongClick(card) }
                    )
                }
            }
        }
    }
}
