import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cardsandshades.model.PlayerModel

@Composable
fun OpponentHeaderZone(
    opponent: PlayerModel,
    onEnemyHeroPositioned: (Offset) -> Unit,
    onEnemyHeroClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1414), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(opponent.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            val heroScale by animateFloatAsState(targetValue = if (viewModel.opponentHeroTakingDamage) 1.4f else 1f)

            Box(contentAlignment = Alignment.Center) {
                Text(
                    "HP: ${opponent.currentHp}/${opponent.maxHp} ❤️",
                    color = if (viewModel.opponentHeroTakingDamage) Color.White else Color(0xFFFF5252),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .scale(heroScale)
                        .background(if (viewModel.opponentHeroTakingDamage) Color.Red else Color.Transparent, RoundedCornerShape(4.dp))
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            onEnemyHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                        }
                        .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clickable { onEnemyHeroClick() }
                )

                if (viewModel.opponentHeroTakingDamage) {
                    Text(
                        text = "-${viewModel.opponentHeroDamageValue}",
                        color = Color.Red,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(y = (-40).dp)
                    )
                }
            }
        }
        Text("Карт в руке: ${opponent.hand.size} | Мана врага: ${opponent.currentMana}/${opponent.maxMana}", color = Color.Gray, fontSize = 12.sp)
    }
}