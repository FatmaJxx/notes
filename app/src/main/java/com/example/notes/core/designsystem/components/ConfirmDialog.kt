package com.example.notes.core.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.notes.R
import com.example.notes.core.designsystem.theme.notes.DialogShape
import com.example.notes.core.designsystem.theme.notes.PillShape

@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.btn_action_delete),
    confirmColor: Color = Color(0xFFBA1A1A),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = DialogShape,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                ) {
                    Text(text = confirmText, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    shape = PillShape
                ) {
                    Text(text = stringResource(R.string.btn_action_cancel))
                }
            }
        )
    }
}
