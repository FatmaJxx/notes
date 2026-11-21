package com.example.notes.core.designsystem.theme.notes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val PillShape = RoundedCornerShape(percent = 50)
val CardShape = RoundedCornerShape(20.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FabShape = RoundedCornerShape(20.dp)
val DialogShape = RoundedCornerShape(28.dp)
