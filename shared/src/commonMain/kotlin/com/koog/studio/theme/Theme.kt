package com.koog.studio.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeSeed(val label: String, val lightPrimary: Color, val lightOnPrimary: Color, val lightPrimaryContainer: Color, val lightOnPrimaryContainer: Color, val darkPrimary: Color, val darkOnPrimary: Color, val darkPrimaryContainer: Color, val darkOnPrimaryContainer: Color) {
    Blue(
        label = "Blue",
        lightPrimary = Color(0xFF1A73E8), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFD3E3FD), lightOnPrimaryContainer = Color(0xFF001C3B),
        darkPrimary = Color(0xFF8AB4F8), darkOnPrimary = Color(0xFF003062),
        darkPrimaryContainer = Color(0xFF004A93), darkOnPrimaryContainer = Color(0xFFD3E3FD),
    ),
    Purple(
        label = "Purple",
        lightPrimary = Color(0xFF6750A4), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFEADDFF), lightOnPrimaryContainer = Color(0xFF21005D),
        darkPrimary = Color(0xFFD0BCFF), darkOnPrimary = Color(0xFF381E72),
        darkPrimaryContainer = Color(0xFF4F378B), darkOnPrimaryContainer = Color(0xFFEADDFF),
    ),
    Teal(
        label = "Teal",
        lightPrimary = Color(0xFF006B5E), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFF7AF8E4), lightOnPrimaryContainer = Color(0xFF00201C),
        darkPrimary = Color(0xFF5EDBC6), darkOnPrimary = Color(0xFF003731),
        darkPrimaryContainer = Color(0xFF005047), darkOnPrimaryContainer = Color(0xFF7AF8E4),
    ),
    Red(
        label = "Red",
        lightPrimary = Color(0xFFBA1A1A), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFFFDAD6), lightOnPrimaryContainer = Color(0xFF410002),
        darkPrimary = Color(0xFFFFB4AB), darkOnPrimary = Color(0xFF690005),
        darkPrimaryContainer = Color(0xFF93000A), darkOnPrimaryContainer = Color(0xFFFFDAD6),
    ),
    Green(
        label = "Green",
        lightPrimary = Color(0xFF386A20), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFB8F397), lightOnPrimaryContainer = Color(0xFF052100),
        darkPrimary = Color(0xFF9DD67E), darkOnPrimary = Color(0xFF0B3900),
        darkPrimaryContainer = Color(0xFF1E5108), darkOnPrimaryContainer = Color(0xFFB8F397),
    ),
    Orange(
        label = "Orange",
        lightPrimary = Color(0xFF8B5000), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFFFB870), lightOnPrimaryContainer = Color(0xFF2D1600),
        darkPrimary = Color(0xFFFFB870), darkOnPrimary = Color(0xFF4A2800),
        darkPrimaryContainer = Color(0xFF6A3B00), darkOnPrimaryContainer = Color(0xFFFFB870),
    ),
    Pink(
        label = "Pink",
        lightPrimary = Color(0xFF984061), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFFFD9E2), lightOnPrimaryContainer = Color(0xFF3E001D),
        darkPrimary = Color(0xFFFFB1C8), darkOnPrimary = Color(0xFF5E1133),
        darkPrimaryContainer = Color(0xFF7B2949), darkOnPrimaryContainer = Color(0xFFFFD9E2),
    ),
    Cyan(
        label = "Cyan",
        lightPrimary = Color(0xFF006684), lightOnPrimary = Color.White,
        lightPrimaryContainer = Color(0xFFBDE8FF), lightOnPrimaryContainer = Color(0xFF001E2D),
        darkPrimary = Color(0xFF8ECFFF), darkOnPrimary = Color(0xFF003449),
        darkPrimaryContainer = Color(0xFF004C66), darkOnPrimaryContainer = Color(0xFFBDE8FF),
    ),
}

@Composable
fun KoogStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seed: ThemeSeed = ThemeSeed.Blue,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = seed.darkPrimary,
            onPrimary = seed.darkOnPrimary,
            primaryContainer = seed.darkPrimaryContainer,
            onPrimaryContainer = seed.darkOnPrimaryContainer,
            secondary = md_theme_dark_secondary,
            onSecondary = md_theme_dark_onSecondary,
            secondaryContainer = md_theme_dark_secondaryContainer,
            onSecondaryContainer = md_theme_dark_onSecondaryContainer,
            tertiary = seed.darkPrimary,
            onTertiary = seed.darkOnPrimary,
            tertiaryContainer = seed.darkPrimaryContainer,
            onTertiaryContainer = seed.darkOnPrimaryContainer,
            error = md_theme_dark_error,
            onError = md_theme_dark_onError,
            errorContainer = md_theme_dark_errorContainer,
            onErrorContainer = md_theme_dark_onErrorContainer,
            background = md_theme_dark_background,
            onBackground = md_theme_dark_onBackground,
            surface = md_theme_dark_surface,
            onSurface = md_theme_dark_onSurface,
            surfaceVariant = md_theme_dark_surfaceVariant,
            onSurfaceVariant = md_theme_dark_onSurfaceVariant,
            outline = md_theme_dark_outline,
            outlineVariant = md_theme_dark_outlineVariant,
            inverseSurface = md_theme_dark_inverseSurface,
            inverseOnSurface = md_theme_dark_inverseOnSurface,
            inversePrimary = md_theme_dark_inversePrimary,
            surfaceTint = seed.darkPrimary,
            scrim = md_theme_dark_scrim,
        )
    } else {
        lightColorScheme(
            primary = seed.lightPrimary,
            onPrimary = seed.lightOnPrimary,
            primaryContainer = seed.lightPrimaryContainer,
            onPrimaryContainer = seed.lightOnPrimaryContainer,
            secondary = md_theme_light_secondary,
            onSecondary = md_theme_light_onSecondary,
            secondaryContainer = md_theme_light_secondaryContainer,
            onSecondaryContainer = md_theme_light_onSecondaryContainer,
            tertiary = seed.lightPrimary,
            onTertiary = seed.lightOnPrimary,
            tertiaryContainer = seed.lightPrimaryContainer,
            onTertiaryContainer = seed.lightOnPrimaryContainer,
            error = md_theme_light_error,
            onError = md_theme_light_onError,
            errorContainer = md_theme_light_errorContainer,
            onErrorContainer = md_theme_light_onErrorContainer,
            background = md_theme_light_background,
            onBackground = md_theme_light_onBackground,
            surface = md_theme_light_surface,
            onSurface = md_theme_light_onSurface,
            surfaceVariant = md_theme_light_surfaceVariant,
            onSurfaceVariant = md_theme_light_onSurfaceVariant,
            outline = md_theme_light_outline,
            outlineVariant = md_theme_light_outlineVariant,
            inverseSurface = md_theme_light_inverseSurface,
            inverseOnSurface = md_theme_light_inverseOnSurface,
            inversePrimary = md_theme_light_inversePrimary,
            surfaceTint = seed.lightPrimary,
            scrim = md_theme_light_scrim,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
