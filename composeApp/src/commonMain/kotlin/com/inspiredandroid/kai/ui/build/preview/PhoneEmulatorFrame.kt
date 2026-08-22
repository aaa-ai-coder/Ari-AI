package com.inspiredandroid.kai.ui.build.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.settings.monoStyle

/**
 * High-fidelity hardware phone frame emulator (Google AI Studio / Bolt style).
 * Features metallic titanium bezel, camera notch/island, realistic status bar,
 * and bottom home indicator.
 */
@Composable
fun PhoneEmulatorFrame(
    model: PhoneDeviceModel,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (model == PhoneDeviceModel.RESPONSIVE_FULL) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
            content()
        }
        return
    }

    val targetWidth = if (isLandscape) model.screenHeightDp else model.screenWidthDp
    val targetHeight = if (isLandscape) model.screenWidthDp else model.screenHeightDp
    val frameWidth = targetWidth + (model.bezelWidthDp * 2)
    val frameHeight = targetHeight + (model.bezelWidthDp * 2)
    val outerRadius = model.cornerRadiusDp + model.bezelWidthDp
    val innerRadius = model.cornerRadiusDp

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        val availWidth = maxWidth.value
        val availHeight = maxHeight.value
        val scaleX = (availWidth / frameWidth.value).coerceAtMost(1f)
        val scaleY = (availHeight / frameHeight.value).coerceAtMost(1f)
        val scale = minOf(scaleX, scaleY).coerceAtLeast(0.3f)

        Box(
            modifier = Modifier
                .scale(scale)
                .size(width = frameWidth, height = frameHeight)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(outerRadius))
                .clip(RoundedCornerShape(outerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2C2D30),
                            Color(0xFF1E1F22),
                            Color(0xFF151618),
                            Color(0xFF2C2D30),
                        ),
                    ),
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF656871),
                            Color(0xFF2A2B2E),
                            Color(0xFF555861),
                        ),
                    ),
                    shape = RoundedCornerShape(outerRadius),
                )
                .padding(model.bezelWidthDp),
        ) {
            // Screen Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(innerRadius))
                    .background(Color.Black),
            ) {
                // Phone Top Status Bar
                PhoneStatusBar(
                    hasIsland = model.hasIsland && !isLandscape,
                    hasPunchHole = model.hasPunchHole && !isLandscape,
                )

                // Interactive App / Web Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    content()
                }

                // Phone Bottom Home Indicator Bar
                if (model.hasHomeIndicator) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.6f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneStatusBar(
    hasIsland: Boolean,
    hasPunchHole: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color.Black)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Left: Time
        Text(
            text = "9:41",
            style = monoStyle(11.5.sp, Color.White),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        // Center: Hardware Camera / Island
        when {
            hasIsland -> {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF000000))
                        .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F1420))
                                .border(1.dp, Color(0xFF1A233A), CircleShape),
                        )
                    }
                }
            }
            hasPunchHole -> {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0E18))
                        .border(1.dp, Color(0xFF1E283E), CircleShape),
                )
            }
        }

        // Right: Status Icons (5G, Wi-Fi, Battery)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.NetworkCell,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("100%", style = monoStyle(9.5.sp, Color(0xFF4CAF50)), fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
