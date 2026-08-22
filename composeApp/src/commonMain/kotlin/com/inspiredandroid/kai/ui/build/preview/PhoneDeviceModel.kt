package com.inspiredandroid.kai.ui.build.preview

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mobile device models supported in the Ari Build Phone Emulator.
 */
@Immutable
enum class PhoneDeviceModel(
    val id: String,
    val displayName: String,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val cornerRadiusDp: Dp,
    val bezelWidthDp: Dp,
    val hasIsland: Boolean = false,
    val hasPunchHole: Boolean = false,
    val hasHomeIndicator: Boolean = true,
) {
    PIXEL_9_PRO(
        id = "pixel_9_pro",
        displayName = "Pixel 9 Pro",
        screenWidthDp = 393.dp,
        screenHeightDp = 852.dp,
        cornerRadiusDp = 28.dp,
        bezelWidthDp = 10.dp,
        hasPunchHole = true,
        hasHomeIndicator = true,
    ),
    IPHONE_16_PRO(
        id = "iphone_16_pro",
        displayName = "iPhone 16 Pro",
        screenWidthDp = 402.dp,
        screenHeightDp = 874.dp,
        cornerRadiusDp = 36.dp,
        bezelWidthDp = 11.dp,
        hasIsland = true,
        hasHomeIndicator = true,
    ),
    GALAXY_S24(
        id = "galaxy_s24",
        displayName = "Galaxy S24",
        screenWidthDp = 412.dp,
        screenHeightDp = 915.dp,
        cornerRadiusDp = 26.dp,
        bezelWidthDp = 10.dp,
        hasPunchHole = true,
        hasHomeIndicator = true,
    ),
    TABLET_IPAD(
        id = "tablet_ipad",
        displayName = "iPad / Tablet",
        screenWidthDp = 768.dp,
        screenHeightDp = 1024.dp,
        cornerRadiusDp = 20.dp,
        bezelWidthDp = 14.dp,
        hasPunchHole = false,
        hasHomeIndicator = true,
    ),
    RESPONSIVE_FULL(
        id = "responsive_full",
        displayName = "Borderless / Full",
        screenWidthDp = 0.dp,
        screenHeightDp = 0.dp,
        cornerRadiusDp = 0.dp,
        bezelWidthDp = 0.dp,
        hasHomeIndicator = false,
    );

    companion object {
        val DEFAULT = PIXEL_9_PRO
    }
}
