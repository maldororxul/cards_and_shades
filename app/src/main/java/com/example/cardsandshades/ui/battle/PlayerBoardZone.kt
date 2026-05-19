import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.ui.components.CardComponent

@Composable
fun PlayerBoardZone(
    boardCards: List<CardModel>,
    selectedCard: CardModel?,
    isHovered: Boolean,
    onCardPositioned: (String, Offset) -> Unit,
    onCardClick: (CardModel, Offset) -> Unit
) {
    val boardBorderColor = if (isHovered) Color.Green else Color(0xFF233A23)
    val boardBgColor = if (isHovered) Color(0xFF142414) else Color(0xFF141F14)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, boardBorderColor, RoundedCornerShape(8.dp))
            .background(boardBgColor),
        contentAlignment = Alignment.Center
    ) {
        if (boardCards.isEmpty()) {
            Text("Перетащите карту сюда из руки", color = Color.Gray, fontSize = 12.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                items(boardCards, key = { "pl_${it.id}" }) { playerCard ->
                    val isSelected = selectedCard?.id == playerCard.id
                    var cardOffset by remember { mutableStateOf(Offset.Zero) }

                    CardComponent(
                        card = playerCard,
                        modifier = Modifier
                            .padding(4.dp)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                cardOffset = Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2)
                                onCardPositioned(playerCard.id, cardOffset)
                            }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.Green else Color(0xFF4CAF50),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        onClick = { onCardClick(playerCard, cardOffset) }
                    )
                }
            }
        }
    }
}