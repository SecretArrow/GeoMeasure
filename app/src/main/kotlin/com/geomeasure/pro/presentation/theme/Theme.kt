package com.geomeasure.pro.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = Color(0xFFE65100),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD4BA),
    onTertiaryContainer = Color(0xFF310900),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FBF8),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8EDE8),
    onSurfaceVariant = Color(0xFF44483E),
    outline = Color(0xFF74796D),
    outlineVariant = Color(0xFFC4C8BB)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = Color(0xFFBBDEFB),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4E1E00),
    tertiaryContainer = Color(0xFF7E3F00),
    onTertiaryContainer = Color(0xFFFFD4BA),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE2E3DD),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF42483E),
    onSurfaceVariant = Color(0xFFC4C8BB),
    outline = Color(0xFF8D9286),
    outlineVariant = Color(0xFF42483E)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun GeoMeasureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
