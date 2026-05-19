import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverOverlay(
    isGameOver: Boolean,
    winnerName: String?,
    playerName: String?,
    rewardGold: Int,
    rewardCardName: String?,
    onExitClick: (Boolean) -> Unit
) {
    AnimatedVisibility(visible = isGameOver, enter = fadeIn(), exit = fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Битва Завершена!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                val isPlayerWin = winnerName == playerName
                Text(
                    text = if (isPlayerWin) "ВЫ ПОБЕДИЛИ! 🎉" else "ВЫ ПРОИГРАЛИ 💀",
                    color = if (isPlayerWin) Color.Green else Color.Red,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // ВИЗУАЛИЗАЦИЯ НАГРАДЫ ИГРОКА
                if (isPlayerWin) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Ваша награда за победу:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🪙 +$rewardGold Золотых монет", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    rewardCardName?.let { cardName ->
                        Text("🃏 Карта: [$cardName] добавлена в коллекцию!", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onExitClick(isPlayerWin) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Забрать лут и выйти")
                }
            }
        }
    }
}