package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Cute Pixel Art Shapes (Chunky retro edges)
val PixelShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

private val PixelLightColorScheme = lightColorScheme(
    primary = PixelGrassGreen,
    onPrimary = PixelOnGrassGreen,
    primaryContainer = PixelGrassGreenContainer,
    onPrimaryContainer = PixelOnGrassGreenContainer,
    secondary = PixelBlossomPink,
    onSecondary = PixelOnBlossomPink,
    secondaryContainer = PixelBlossomPinkContainer,
    onSecondaryContainer = PixelOnBlossomPinkContainer,
    tertiary = PixelSkyBlueLight,
    onTertiary = PixelOnSkyBlue,
    tertiaryContainer = PixelSkyBlueContainer,
    background = PixelParchmentBackground,
    onBackground = PixelTextDark,
    surface = PixelCardSurface,
    onSurface = PixelTextDark,
    surfaceVariant = PixelSurfaceVariant,
    onSurfaceVariant = PixelTextMuted,
    outline = PixelInkOutline
)

private val PixelDarkColorScheme = darkColorScheme(
    primary = PixelGrassGreen,
    onPrimary = PixelOnGrassGreen,
    primaryContainer = PixelDarkGrassGreen,
    onPrimaryContainer = PixelGrassGreenContainer,
    secondary = PixelBlossomPink,
    onSecondary = PixelOnBlossomPink,
    background = Color(0xFF1E222A),
    onBackground = Color(0xFFF0F3F6),
    surface = Color(0xFF262C36),
    onSurface = Color(0xFFF0F3F6),
    surfaceVariant = Color(0xFF333B48),
    onSurfaceVariant = Color(0xFFB0BAC5),
    outline = Color(0xFF8B949E)
)

@Composable
fun FocusGroveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    usePixelFont: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PixelDarkColorScheme else PixelLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(usePixelFont),
        shapes = PixelShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    FocusGroveTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
