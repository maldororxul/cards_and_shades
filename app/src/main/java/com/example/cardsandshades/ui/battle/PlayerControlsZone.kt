import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R
import com.example.cardsandshades.model.CardModel
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.CardInspectionDialog
import com.example.cardsandshades.ui.components.DragTarget
import com.example.cardsandshades.ui.components.GameButton
import com.example.cardsandshades.ui.components.GameText

@Composable
fun PlayerControlsZone(
    player: PlayerModel,
    isPlayerTurn: Boolean,
    isHeroTakingDamage: Boolean,
    damageValue: Int,
    onPlayerHeroPositioned: (Offset) -> Unit,
    onEndTurnClick: () -> Unit
) {
    var inspectedCard by remember { mutableStateOf<CardModel?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF141A1E), RoundedCornerShape(8.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                GameText(player.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                GameText(
                    text = "${stringResource(R.string.mana)}: ${player.currentMana}/${player.maxMana} 💧 | ${stringResource(R.string.in_deck)}: ${player.deck.size}",
                    color = Color(0xFF29B6F6),
                    fontSize = 14.sp
                )
            }

            val playerHeroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.4f else 1f, animationSpec = tween(200))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 12.dp)) {
                    GameText(
                        text = "HP: ${player.currentHp}/${player.maxHp} ❤️",
                        color = if (isHeroTakingDamage) Color.White else Color(0xFF66BB6A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .scale(playerHeroScale)
                            .background(if (isHeroTakingDamage) Color.Red else Color.Transparent, RoundedCornerShape(4.dp))
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onPlayerHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            }
                    )

                    if (isHeroTakingDamage) {
                        val damageYOffset by animateDpAsState(targetValue = (-40).dp, animationSpec = tween(400))
                        GameText(
                            text = "-$damageValue",
                            color = Color.Red,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.offset(y = damageYOffset)
                        )
                    }
                }

                GameButton(
                    text = stringResource(R.string.end_turn),
                    onClick = onEndTurnClick,
                    containerColor = Color(0xFFD84315),
                    enabled = isPlayerTurn
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.Start) {
            items(player.hand, key = { "hand_${it.id}" }) { card ->
                DragTarget(
                    card = card,
                    modifier = Modifier.padding(4.dp),
                    onTap = { /* Ничего не делаем или выбираем */ },
                    onLongClick = { inspectedCard = card }
                ) {
                    CardComponent(card = card, isPreview = true)
                }
            }
        }
    }

    if (inspectedCard != null) {
        CardInspectionDialog(card = inspectedCard!!, onDismiss = { inspectedCard = null })
    }
}
