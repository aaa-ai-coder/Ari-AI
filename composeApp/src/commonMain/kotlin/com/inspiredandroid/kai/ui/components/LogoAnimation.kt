package com.inspiredandroid.kai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LogoAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val animatable = remember { Animatable(1f) }
    var drawDarkFirst by remember { mutableStateOf(true) }
    val infiniteTransition = rememberInfiniteTransition(label = "orbital")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    LaunchedEffect(Unit) {
        while (true) {
            animatable.animateTo(-1f, tween(767, easing = EaseInOut))
            drawDarkFirst = !drawDarkFirst
            animatable.animateTo(1f, tween(767, easing = EaseInOut))
            drawDarkFirst = !drawDarkFirst
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val center = this.center
        val radius = center.y * 0.75f
        val displacement = radius * animatable.value
        val darkCenter = Offset(center.x + displacement, center.y)
        val lightCenter = Offset(center.x - displacement, center.y)

        // Soft ambient radial glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x5500E5FF), Color(0x337C4DFF), Color.Transparent),
                center = center,
                radius = radius * 1.6f,
            ),
            radius = radius * 1.6f,
            center = center,
        )

        // Dual harmonic energy orbs
        if (drawDarkFirst) {
            drawCircle(Color(0xFF7C4DFF), radius, darkCenter)
            drawCircle(Color(0xFF00E5FF), radius, lightCenter)
        } else {
            drawCircle(Color(0xFF00E5FF), radius, lightCenter)
            drawCircle(Color(0xFF7C4DFF), radius, darkCenter)
        }

        // Luminous orbital boundary ring
        drawCircle(
            color = Color(0x6600E5FF),
            radius = radius * 1.25f,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}
