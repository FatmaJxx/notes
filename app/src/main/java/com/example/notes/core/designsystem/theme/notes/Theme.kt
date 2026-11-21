package com.example.notes.core.designsystem.theme.notes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = OnPurplePrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = Color(0xFF241432),
    secondary = PinkAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF3B071D),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = Color(0xFF381E72),
    primaryContainer = PurpleContainerDark,
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = PinkAccentDark,
    onSecondary = Color(0xFF4A0028),
    secondaryContainer = Color(0xFF650B38),
    onSecondaryContainer = Color(0xFFFFD9E2),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Immutable
data class NotesPalette(
    val lavender: Color,
    val pink: Color,
    val blue: Color,
    val mint: Color,
    val yellow: Color
)

val DarkNotePlate = NotesPalette(
    lavender = PastelLavenderDark,
    pink = PastelPinkDark,
    blue = PastelBlueDark,
    mint = PastelMintDark,
    yellow = PastelYellowDark
)

val LightNotePlate = NotesPalette(
    lavender = PastelLavenderLight,
    pink = PastelPinkLight,
    blue = PastelBlueLight,
    mint = PastelMintLight,
    yellow = PastelYellowLight
)
val LocalNotePalette = staticCompositionLocalOf {
    LightNotePlate
}

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val notePalette = if (darkTheme) {
        DarkNotePlate
    } else {
        LightNotePlate
    }

    CompositionLocalProvider(
        LocalNotePalette provides notePalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = Shapes,
            content = content
        )
    }
}