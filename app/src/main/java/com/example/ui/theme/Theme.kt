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

private val PinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5277),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3E8),
    onPrimaryContainer = Color(0xFF6B1124),
    secondary = Color(0xFFE91E63),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4EC),
    onSecondaryContainer = Color(0xFF880E4F),
    tertiary = Color(0xFF9C27B0),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E5F5),
    background = Color(0xFFFFF9FA),
    onBackground = PixelTextDark,
    surface = Color.White,
    onSurface = PixelTextDark,
    surfaceVariant = Color(0xFFFFECEF),
    onSurfaceVariant = PixelTextMuted,
    outline = Color(0xFFD81B60)
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5277),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFFD1DC),
    secondary = Color(0xFFFF80AB),
    onSecondary = Color.Black,
    background = Color(0xFF26181C),
    onBackground = Color(0xFFFDECEF),
    surface = Color(0xFF332025),
    onSurface = Color(0xFFFDECEF),
    surfaceVariant = Color(0xFF452B33),
    onSurfaceVariant = Color(0xFFE0B2BE),
    outline = Color(0xFFFF80AB)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF1E88E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF00ACC1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = Color(0xFF3949AB),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8EAF6),
    background = Color(0xFFF5F9FD),
    onBackground = PixelTextDark,
    surface = Color.White,
    onSurface = PixelTextDark,
    surfaceVariant = Color(0xFFE1EDF7),
    onSurfaceVariant = PixelTextMuted,
    outline = Color(0xFF1976D2)
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF42A5F5),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color.Black,
    background = Color(0xFF151D28),
    onBackground = Color(0xFFEDF4FA),
    surface = Color(0xFF1D2837),
    onSurface = Color(0xFFEDF4FA),
    surfaceVariant = Color(0xFF28374B),
    onSurfaceVariant = Color(0xFFB0C4DE),
    outline = Color(0xFF64B5F6)
)

private val YellowLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFDE68A),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFFBEB),
    background = Color(0xFFFFFDF5),
    onBackground = PixelTextDark,
    surface = Color.White,
    onSurface = PixelTextDark,
    surfaceVariant = Color(0xFFFEF9C3),
    onSurfaceVariant = PixelTextMuted,
    outline = Color(0xFFD97706)
)

private val YellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.Black,
    background = Color(0xFF261D0C),
    onBackground = Color(0xFFFEF9C3),
    surface = Color(0xFF332712),
    onSurface = Color(0xFFFEF9C3),
    surfaceVariant = Color(0xFF453418),
    onSurfaceVariant = Color(0xFFD4AF37),
    outline = Color(0xFFFBBF24)
)

@Composable
fun FocusGroveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    usePixelFont: Boolean = true,
    equippedTheme: String = "DEFAULT",
    equippedFont: String = "DEFAULT",
    content: @Composable () -> Unit,
) {
    val colorScheme = when (equippedTheme.uppercase()) {
        "BLUE" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        "PINK" -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        "YELLOW" -> if (darkTheme) YellowDarkColorScheme else YellowLightColorScheme
        "DARK" -> PixelDarkColorScheme
        "NATURE", "DEFAULT" -> if (darkTheme) PixelDarkColorScheme else PixelLightColorScheme
        else -> if (darkTheme) PixelDarkColorScheme else PixelLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(equippedFont, usePixelFont),
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
