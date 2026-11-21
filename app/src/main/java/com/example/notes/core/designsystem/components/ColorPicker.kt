package com.example.notes.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.notes.core.model.AppColor

@Composable
fun ColorPicker(
    selectedColor: AppColor,
    onColorSelected: (AppColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppColor.entries.forEach { color ->
            val isSelected = color == selectedColor
            val swatchColor = color.get()

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                        }
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}