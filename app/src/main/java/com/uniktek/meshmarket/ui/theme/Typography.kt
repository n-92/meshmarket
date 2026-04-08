package com.uniktek.meshmarket.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Base font size constant (default, unscaled)
internal const val BASE_FONT_SIZE = com.uniktek.meshmarket.util.AppConstants.UI.BASE_FONT_SIZE_SP

/**
 * Get the scaled base font size, accounting for user's display scale preference.
 */
val scaledFontSize: Int
    get() = (BASE_FONT_SIZE * DisplayScaleManager.factor).toInt()

/**
 * Scale a dp icon size by the user's display scale factor.
 */
fun Dp.scaled(): Dp = (this.value * DisplayScaleManager.factor).dp

/**
 * Build Typography with the current display scale applied.
 */
fun scaledTypography(scale: Float = DisplayScaleManager.factor): Typography {
    val base = (BASE_FONT_SIZE * scale).toInt()
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = (base + 1).sp,
            lineHeight = (base + 7).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = base.sp,
            lineHeight = (base + 3).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = (base - 3).sp,
            lineHeight = (base + 1).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = (base + 3).sp,
            lineHeight = (base + 9).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = (base + 1).sp,
            lineHeight = (base + 7).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = (base - 2).sp,
            lineHeight = (base + 3).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = (base - 4).sp,
            lineHeight = (base + 1).sp
        )
    )
}

// Default typography (for initial composition before scale is loaded)
val Typography = scaledTypography(1.0f)
