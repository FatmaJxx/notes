package com.example.notes.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    circleSize: Dp = 8.dp,
    circleColor: Color = Color.White,
    circlesNumber: Int = 3,
    spaceBetween: Dp = 5.dp,
    travelDistance: Dp = 10.dp,
) {
    val circles = remember {
        List(circlesNumber) { Animatable(initialValue = 0f) }
    }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay((index * 100L).milliseconds)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 using LinearOutSlowInEasing
                        0.5f at 300 using LinearOutSlowInEasing
                        0.0f at 600 using LinearOutSlowInEasing
                        0.0f at 1200 using LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart,
                ),
            )
        }
    }

    // ← distance still fine here, LocalDensity doesn't change
    val distance = with(LocalDensity.current) { travelDistance.toPx() }
    val lastCircle = circles.size - 1

    Row(modifier = modifier) {
        circles.forEachIndexed { index, animatable ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .graphicsLayer {
                        // ✅ Read animatable.value HERE — draw phase only
                        // no recomposition triggered during animation
                        translationY = -animatable.value * distance
                    }
                    .background(circleColor, shape = CircleShape),
            )
            if (index != lastCircle) {
                Spacer(modifier = Modifier.width(spaceBetween))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingAnimation() {
    PulseAnimation()
}