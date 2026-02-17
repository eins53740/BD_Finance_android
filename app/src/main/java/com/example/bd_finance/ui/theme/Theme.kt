package com.example.bd_finance.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkNavy,
    primaryContainer = DeepGold,
    onPrimaryContainer = Color(0xFFFFE082),
    secondary = Amber,
    onSecondary = DarkNavy,
    secondaryContainer = DarkAmber,
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = Green,
    onTertiary = DarkNavy,
    tertiaryContainer = DarkGreen,
    onTertiaryContainer = Color(0xFFA5D6A7),
    error = Red,
    onError = DarkNavy,
    errorContainer = DarkRed,
    onErrorContainer = Color(0xFFEF9A9A),
    background = Charcoal,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = Slate,
    onSurfaceVariant = MutedGray,
    outline = DarkOutline,
    outlineVariant = Color(0xFF2C3240),
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = LightGold,
    onPrimaryContainer = Color(0xFF1A237E),
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = LightAmber,
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = GreenDark,
    onTertiary = Color.White,
    tertiaryContainer = LightGreen,
    onTertiaryContainer = Color(0xFF1B5E20),
    error = RedDark,
    onError = Color.White,
    errorContainer = LightRed,
    onErrorContainer = Color(0xFFB71C1C),
    background = LightBackground,
    onBackground = DarkNavy,
    surface = Color.White,
    onSurface = DarkNavy,
    surfaceVariant = LightGray,
    onSurfaceVariant = MutedDark,
    outline = LightOutline,
    outlineVariant = Color(0xFFDADCE0),
)

@Composable
fun BD_FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
