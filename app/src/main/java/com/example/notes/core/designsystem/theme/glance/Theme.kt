package com.example.notes.core.designsystem.theme.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.glance.color.ColorProvider
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import com.example.notes.core.designsystem.theme.notes.DarkColorScheme
import com.example.notes.core.designsystem.theme.notes.LightColorScheme
import com.example.notes.core.designsystem.theme.notes.PastelBlueDark
import com.example.notes.core.designsystem.theme.notes.PastelBlueLight
import com.example.notes.core.designsystem.theme.notes.PastelLavenderDark
import com.example.notes.core.designsystem.theme.notes.PastelLavenderLight
import com.example.notes.core.designsystem.theme.notes.PastelMintDark
import com.example.notes.core.designsystem.theme.notes.PastelMintLight
import com.example.notes.core.designsystem.theme.notes.PastelPinkDark
import com.example.notes.core.designsystem.theme.notes.PastelPinkLight
import com.example.notes.core.designsystem.theme.notes.PastelYellowDark
import com.example.notes.core.designsystem.theme.notes.PastelYellowLight
import com.example.notes.core.model.AppColor

object NotesGlanceColorProvider{
    val colors = ColorProviders(
        light = LightColorScheme,
        dark = DarkColorScheme
    )
}

@Immutable
data class NotesGlancePalette(
    val lavender: ColorProvider,
    val pink: ColorProvider,
    val blue: ColorProvider,
    val mint: ColorProvider,
    val yellow: ColorProvider
)

val LocalNotesGlancePalette = staticCompositionLocalOf {
    NotesGlancePalette(
        lavender = ColorProvider(day = PastelLavenderLight, night = PastelLavenderDark),
        pink = ColorProvider(day = PastelPinkLight, night = PastelPinkDark),
        blue = ColorProvider(day = PastelBlueLight, night = PastelBlueDark),
        mint = ColorProvider(day = PastelMintLight, night = PastelMintDark),
        yellow = ColorProvider(day = PastelYellowLight, night = PastelYellowDark)
    )
}

@Composable
fun getGlanceColorForNote(color: AppColor): ColorProvider {
    val palette = LocalNotesGlancePalette.current
    return when (color) {
        AppColor.LAVENDER -> palette.lavender
        AppColor.PINK   -> palette.pink
        AppColor.BLUE    -> palette.blue
        AppColor.MINT    -> palette.mint
        AppColor.YELLOW  -> palette.yellow
    }
}