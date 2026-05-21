package com.example.cardsandshades.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.cardsandshades.R

val GameFontFamily = FontFamily(
    Font(R.font.common, FontWeight.Normal),
    Font(R.font.common, FontWeight.Bold),
    Font(R.font.common, FontWeight.Medium),
    Font(R.font.common, FontWeight.SemiBold),
    Font(R.font.common, FontWeight.ExtraBold),
    Font(R.font.common, FontWeight.Black)
)

val GameTypography = Typography(
    displayLarge = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Black, fontSize = 48.sp),
    displayMedium = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = GameFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp)
)
