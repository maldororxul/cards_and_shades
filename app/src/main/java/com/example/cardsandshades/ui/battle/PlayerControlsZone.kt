import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.ui.components.CardComponent
import com.example.cardsandshades.ui.components.DragTarget

@Composable
fun PlayerControlsZone(
    player: PlayerModel,
    isPlayerTurn: Boolean,
    onPlayerHeroPositioned: (Offset) -> Unit,
    onEndTurnClick: () -> Unit,
    viewModel: ViewModel,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF141A1E), RoundedCornerShape(8.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(player.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ваша мана: ${player.currentMana}/${player.maxMana} 💧", color = Color(0xFF29B6F6), fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val playerHeroScale by animateFloatAsState(targetValue = if (viewModel.playerHeroTakingDamage) 1.4f else 1f)

                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "HP: ${player.currentHp}/${player.maxHp} ❤️",
                        color = if (viewModel.playerHeroTakingDamage) Color.White else Color(0xFF66BB6A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .scale(playerHeroScale)
                            .background(if (viewModel.playerHeroTakingDamage) Color.Red else Color.Transparent, RoundedCornerShape(4.dp))
                            .padding(end = 12.dp)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                onPlayerHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                            }
                    )

                    if (viewModel.playerHeroTakingDamage) {
                        Text(
                            text = "-${viewModel.playerHeroDamageValue}",
                            color = Color.Red,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.offset(y = (-40).dp)
                        )
                    }
                }
                Button(
                    onClick = onEndTurnClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = isPlayerTurn
                ) {
                    Text("Конец Хода")
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // РУКА ИГРОКА
        LazyRow(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.Start) {
            items(player.hand, key = { "hand_${it.id}" }) { card ->
                DragTarget(card = card, modifier = Modifier.padding(4.dp)) {
                    CardComponent(card = card)
                }
            }
        }
    }
}