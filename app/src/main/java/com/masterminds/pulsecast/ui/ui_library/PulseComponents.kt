package com.masterminds.pulsecast.ui.ui_library

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*

/**
 * DESIGN.md's "Level 1 (Docked Containers & Surfaces)" — solid
 * surface-low with a uniform 1px stroke-subtle outline, non-blurred.
 */
@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = PulseCastShapes.md,
    backgroundColor: Color = SurfaceLow,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .border(1.dp, StrokeSubtle, shape)
            .padding(PulseCastSpacing.base),
    ) {
        content()
    }
}

/** A telemetry readout chip — small dot + JetBrains Mono value, e.g. "6200 kbps". */
@Composable
fun TelemetryChip(
    label: String,
    modifier: Modifier = Modifier,
    contentColor: Color = OnSurface,
    containerColor: Color = SurfaceHigh
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = PulseCastType.labelTelemetrySm,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/** A compact pill badge — DESIGN.md's "Pills & Tabs" treatment. */
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

/** A status dot with ping animation. */
@Composable
fun PulseStatusDot(
    modifier: Modifier = Modifier,
    color: Color = ElectricRuby
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/** A telemetry ring for percentage readouts. */
@Composable
fun PulseTelemetryRing(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = CyberCyan
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = SurfaceMid,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * percentage,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = PulseCastType.labelTelemetryLg,
                color = OnSurface,
                lineHeight = 1.sp
            )
            Text(
                text = label,
                style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp),
                color = OnSurfaceMuted
            )
        }
    }
}

/** A VU meter strip for audio levels. */
@Composable
fun PulseVUIndicator(
    label: String,
    level: Float,
    dbValue: String,
    modifier: Modifier = Modifier,
    color: Color = CyberCyan
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp),
                color = OnSurfaceMuted
            )
            Text(
                text = dbValue,
                style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp),
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        // Thin segmented VU bar matching HTML exactly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF090E19)) // surface-container-lowest
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Main level (secondary-fixed / color)
            Box(
                modifier = Modifier
                    .weight(0.72f)
                    .fillMaxHeight(0.6f)
                    .clip(CircleShape)
                    .background(color)
            )
            // Warning level (tertiary / NeonAmber)
            Box(
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxHeight(0.6f)
                    .clip(CircleShape)
                    .background(NeonAmber)
            )
            // Peak level (surface-highest / ElectricRuby)
            Box(
                modifier = Modifier
                    .weight(0.16f)
                    .fillMaxHeight(0.6f)
                    .clip(CircleShape)
                    .background(if (level > 0.85f) ElectricRuby else Color(0xFF303541)) // surface-container-highest
            )
        }
    }
}

/** The master recording trigger button. */
@Composable
fun PulseRecordButton(
    onClick: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isRecording) ErrorContainer else PrimaryContainer
    val contentColor = Color.Black // Design explicitly asks for black text
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRecording) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Recording Active (00:01)",
                    style = PulseCastType.headlineSm,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            } else {
                // Design shows black status dot and black text
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Start Screen Recording",
                    style = PulseCastType.headlineSm,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
