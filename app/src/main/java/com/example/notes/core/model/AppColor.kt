package com.example.notes.core.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.notes.core.designsystem.theme.notes.LocalNotePalette


enum class AppColor {
    LAVENDER,
    PINK,
    BLUE,
    MINT,
    YELLOW;

    @Composable
    fun get(): Color {
        val palette = LocalNotePalette.current
        return when (this) {
            LAVENDER -> palette.lavender
            PINK -> palette.pink
            BLUE -> palette.blue
            MINT -> palette.mint
            YELLOW -> palette.yellow
        }
    }
}