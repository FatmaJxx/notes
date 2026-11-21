package com.example.notes.feature.agent

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatMessageBubble(
    message: Message, modifier: Modifier = Modifier
) {
    if (message.activeSurface != null) {
        A2uiSurface(
            surfaceModel = message.activeSurface, errorContent = { exception ->
                Log.d("A2UI", "Exception $exception")

                Text(
                    text = "A2UI Error: ${exception.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }, modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        )
    } else if (message.message.isNotEmpty()) {
        val messageType = message.type

        val alignment = if (messageType == Message.Type.HUMAN) {
            Alignment.CenterEnd
        } else {
            Alignment.CenterStart
        }

        val topEnd = if (messageType == Message.Type.HUMAN) {
            4.dp
        } else {
            16.dp
        }

        val topStart = if (messageType == Message.Type.HUMAN) {
            16.dp
        } else {
            4.dp
        }

        val color = if (messageType == Message.Type.HUMAN) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Gray
        }

        Box(modifier = modifier.fillMaxWidth(), contentAlignment = alignment) {
            BoxWithConstraints {
                val targetWidth = maxWidth * 0.6f

                Box(
                    modifier = Modifier
                        .widthIn(max = targetWidth)
                        .clip(
                            RoundedCornerShape(
                                topStart = topStart,
                                topEnd = topEnd,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .background(color)
                        .padding(16.dp), contentAlignment = alignment
                ) {
                    Text(text = message.message, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}