package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

// Theme 1: Bento Slate (Original Teal & Slate)
private val BentoSlateDark = darkColorScheme(
    primary = Color(0xFF14B8A6),       // Vibrant neon teal 400
    secondary = Color(0xFF0D9488),     // Teal 600
    tertiary = Color(0xFFF97316),      // Warning orange 500
    background = Color(0xFF0F172A),    // Slate deep navy
    surface = Color(0xFF1E293B),       // Slate dark card gray
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val BentoSlateLight = lightColorScheme(
    primary = Color(0xFF0D9488),       // Precise Bento teal 600
    secondary = Color(0xFF0F766E),     // Bento teal 700
    tertiary = Color(0xFFEA580C),      // Accent orange 600
    background = Color(0xFFF7F9FF),    // Bento custom soft background bg-[#F7F9FF]
    surface = Color(0xFFFFFFFF),       // Clear white bento containers
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF001F29),  // Bento deep dark text #001F29
    onSurface = Color(0xFF1E293B)
)

// Theme 2: Royal Amethyst (Purple & Lavender)
private val RoyalAmethystDark = darkColorScheme(
    primary = Color(0xFFD8B4FE),       // Soft purple 300
    secondary = Color(0xFFC084FC),     // Purple 400
    tertiary = Color(0xFFF472B6),      // Pink 400
    background = Color(0xFF0F0E1E),    // Dark midnight indigo
    surface = Color(0xFF1E1B4B),       // Indigo card
    onPrimary = Color(0xFF0F0E1E),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFAF5FF),
    onSurface = Color(0xFFFAF5FF)
)

private val RoyalAmethystLight = lightColorScheme(
    primary = Color(0xFF8B5CF6),       // Purple 500
    secondary = Color(0xFF7C3AED),     // Purple 600
    tertiary = Color(0xFFEC4899),      // Pink 500
    background = Color(0xFFF5F3FF),    // Purple soft tint
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B4B),
    onSurface = Color(0xFF312E81)
)

// Theme 3: Emerald Forest (Green & Sage)
private val EmeraldForestDark = darkColorScheme(
    primary = Color(0xFF86EFAC),       // Green 300
    secondary = Color(0xFF4ADE80),     // Green 400
    tertiary = Color(0xFFFCD34D),      // Amber 300
    background = Color(0xFF031A0E),    // Ultra-dark forest
    surface = Color(0xFF14532D),       // Forest green card
    onPrimary = Color(0xFF031A0E),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF0FDF4),
    onSurface = Color(0xFFF0FDF4)
)

private val EmeraldForestLight = lightColorScheme(
    primary = Color(0xFF16A34A),       // Green 600
    secondary = Color(0xFF15803D),     // Green 700
    tertiary = Color(0xFFD97706),      // Amber 600
    background = Color(0xFFF0FDF4),    // Green tint back
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF022C22),
    onSurface = Color(0xFF14532D)
)

// Theme 4: Solar Amber (Charcoal & Amber)
private val SolarAmberDark = darkColorScheme(
    primary = Color(0xFFFBBF24),       // Amber 300
    secondary = Color(0xFFF59E0B),     // Amber 500
    tertiary = Color(0xFF38BDF8),      // Sky blue 400
    background = Color(0xFF121212),    // Clean jet black
    surface = Color(0xFF1E1E1E),       // Obsidian dark card
    onPrimary = Color(0xFF121212),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFBEB),
    onSurface = Color(0xFFFFFBEB)
)

private val SolarAmberLight = lightColorScheme(
    primary = Color(0xFFD97706),       // Amber 600
    secondary = Color(0xFFB45309),     // Amber 700
    tertiary = Color(0xFF0284C7),      // Blue 600
    background = Color(0xFFFFFBEB),    // Amber warm back
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1917),
    onSurface = Color(0xFF44403C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    languageCode: String = "fa",
    themeName: String = "BENTO_SLATE",
    content: @Composable () -> Unit
) {
    // Select color scheme dynamically
    val colorScheme = when (themeName) {
        "ROYAL_AMETHYST" -> if (darkTheme) RoyalAmethystDark else RoyalAmethystLight
        "EMERALD_FOREST" -> if (darkTheme) EmeraldForestDark else EmeraldForestLight
        "SOLAR_AMBER" -> if (darkTheme) SolarAmberDark else SolarAmberLight
        else -> if (darkTheme) BentoSlateDark else BentoSlateLight
    }

    // Adapt layout direction dynamically: Persian is RTL, English is LTR
    val layoutDirection = if (languageCode == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                content()
            }
        }
    )
}
