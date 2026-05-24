package com.example.cardsandshades.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.cardsandshades.ui.components.DropTarget
import com.example.cardsandshades.ui.components.GameText

@Composable
fun PlayerBoardZone(
    boardSlots: Array<CardModel?>,
    selectedCard: CardModel?,
    onCardPositioned: (String, Offset) -> Unit,
    onCardClick: (CardModel, Offset) -> Unit,
    onCardDroppedInSlot: (CardModel, Int) -> Unit
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
                    // ПУСТОЙ СЛОТ - ЯВЛЯЕТСЯ ЦЕЛЬЮ ДЛЯ DROP
                    DropTarget(
                        modifier = Modifier.fillMaxSize(),
                        onCardDropped = { droppedCard -> onCardDroppedInSlot(droppedCard, index) }
                    ) { isHovered ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = if (isHovered) Color.Green else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    color = if (isHovered) Color.Green.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            GameText(
                                text = (index + 1).toString(),
                                color = Color.DarkGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // КАРТА В СЛОТЕ
                    val isSelected = selectedCard?.id == card.id
                    var cardOffset by remember { mutableStateOf(Offset.Zero) }

                    CardComponent(
                        card = card,
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                cardOffset = Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2)
                                onCardPositioned(card.id, cardOffset)
                            }
                            .then(
                                if (isSelected) Modifier.border(3.dp, Color.Green, RoundedCornerShape(10.dp))
                                else Modifier
                            ),
                        onClick = { onCardClick(card, cardOffset) }
                    )
                }
            }
        }
    }
}
