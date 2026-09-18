package com.masterminds.pulsecast.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** DESIGN.md's `rounded` scale (rem values converted at 1rem = 16dp). */
object PulseCastShapes {
    val sm = RoundedCornerShape(4.dp)
    val default = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(24.dp)
    val full = RoundedCornerShape(percent = 50) // 9999px pill/circle equivalent
}
