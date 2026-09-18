package com.masterminds.pulsecast.ui.ui_library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masterminds.pulsecast.ui.theme.PulseCastShapes
import com.masterminds.pulsecast.ui.theme.PulseCastSpacing
import com.masterminds.pulsecast.ui.theme.PulseCastType
import com.masterminds.pulsecast.ui.theme.StrokeSubtle
import com.masterminds.pulsecast.ui.theme.SurfaceLow
import com.masterminds.pulsecast.ui.theme.SurfaceMid

/**
 * DESIGN.md's "Level 1 (Docked Containers & Surfaces)" — solid
 * surface-low with a uniform 1px stroke-subtle outline, non-blurred.
 * Level 2 (frosted/blurred floating HUDs and bottom sheets) isn't
 * implemented here — Compose's Modifier.blur() needs API 31+ backdrop
 * support to genuinely blur content BEHIND a surface (true
 * glassmorphism), which isn't verifiable without a device; this card is
 * the honest, universally-compatible Level 1 treatment. Level 2's frosted
 * look is a follow-up once real-device testing is possible.
 */
@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = PulseCastShapes.md,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(SurfaceLow, shape)
            .border(1.dp, StrokeSubtle, shape)
            .padding(PulseCastSpacing.base),
    ) {
        content()
    }
}

/** A compact pill badge — DESIGN.md's "Pills & Tabs" treatment for status tags and metrics. */
@Composable
fun PulsePill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceMid,
    contentColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .background(containerColor, PulseCastShapes.full)
            .padding(horizontal = PulseCastSpacing.sm, vertical = PulseCastSpacing.xxs),
    ) {
        Text(text = text, style = PulseCastType.labelTelemetrySm, color = contentColor)
    }
}

/** A telemetry readout chip — small dot + JetBrains Mono value, e.g. "6200 kbps". */
@Composable
fun TelemetryChip(
    label: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    PulsePill(text = label, modifier = modifier, containerColor = SurfaceMid, contentColor = dotColor)
}
