import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.model.PlayerModel
import com.example.cardsandshades.ui.components.GameText

@Composable
fun OpponentHeaderZone(
    opponent: PlayerModel,
    isHeroTakingDamage: Boolean,
    damageValue: Int,
    onEnemyHeroPositioned: (Offset) -> Unit,
    onEnemyHeroClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1414), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(opponent.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            val heroScale by animateFloatAsState(targetValue = if (isHeroTakingDamage) 1.4f else 1f, animationSpec = tween(200))

            Box(contentAlignment = Alignment.Center) {
                GameText(
                    text = "HP: ${opponent.currentHp}/${opponent.maxHp} ❤️",
                    color = if (isHeroTakingDamage) Color.White else Color(0xFFFF5252),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .scale(heroScale)
                        .background(if (isHeroTakingDamage) Color.Red else Color.Transparent, RoundedCornerShape(4.dp))
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            onEnemyHeroPositioned(Offset(pos.x + coords.size.width / 2, pos.y + coords.size.height / 2))
                        }
                        .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clickable { onEnemyHeroClick() }
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
        }
        GameText("Карт в руке: ${opponent.hand.size} | Мана врага: ${opponent.currentMana}/${opponent.maxMana}", color = Color.Gray, fontSize = 12.sp)
    }
}
