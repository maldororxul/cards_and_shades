import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.ui.components.GameText

@Composable
fun BattleLogZone(battleLog: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF222222), RoundedCornerShape(4.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        val cleanLog = if (battleLog.startsWith("card_") || battleLog == "draw") {
            com.example.cardsandshades.utils.getStringResourceByName(context, battleLog)
        } else {
            battleLog
        }
        
        GameText(
            text = cleanLog,
            color = if (battleLog.contains("❌")) Color.Red else Color.Yellow,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
