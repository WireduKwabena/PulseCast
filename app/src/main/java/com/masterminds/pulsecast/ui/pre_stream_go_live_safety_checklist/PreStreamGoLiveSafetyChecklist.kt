package com.masterminds.pulsecast.ui.pre_stream_go_live_safety_checklist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreFlightChecklistScreen(
    onDismiss: () -> Unit = {},
    onLaunch: () -> Unit = {}
) {
    val viewModel: PreFlightViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.startDiagnostics() }
    DisposableEffect(viewModel) {
        onDispose { viewModel.stopDiagnostics() }
    }
    
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val batteryCharging by viewModel.batteryCharging.collectAsState()
    val thermalStatus by viewModel.thermalStatus.collectAsState()
    val networkQuality by viewModel.networkQuality.collectAsState()
    val micDbLevel by viewModel.micDbLevel.collectAsState()
    val dndGranted by viewModel.dndGranted.collectAsState()
    val dndArmed by viewModel.dndArmed.collectAsState()
    val overallReadiness by viewModel.overallReadiness.collectAsState()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgBase,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 48.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SurfaceHigh)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            HeaderSection()

            // Readiness Scorecard
            ReadinessScoreCard(overallReadiness)

            // Target Destination
            TargetDestinationCard()

            // Broadcast Metadata
            BroadcastMetadataCard()

            // Diagnostic List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AUTOMATED VERIFICATION",
                    style = PulseCastType.labelTelemetryMd.copy(fontSize = 10.sp),
                    color = OnSurfaceMuted,
                    letterSpacing = 0.5.sp
                )
                Text("Real-Time Polling", style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.sp), color = CyberCyan)
            }

            // Verification List
            VerificationList(
                batteryLevel = batteryLevel,
                batteryCharging = batteryCharging,
                thermalStatus = thermalStatus,
                networkQuality = networkQuality,
                micDbLevel = micDbLevel,
                dndGranted = dndGranted,
                dndArmed = dndArmed,
                viewModel = viewModel,
                context = context
            )

            // Launch Area
            LaunchArea(onLaunch)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, null, Modifier.size(18.dp), PrimaryContainer)
                Text(
                    text = "Pre-Flight Checklist", 
                    style = PulseCastType.headlineLg.copy(fontSize = 16.sp), 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    softWrap = false,
                    overflow = TextOverflow.Visible
                )
            }
            Text(
                "Hardware, network & privacy validation \nbefore going live",
                style = PulseCastType.bodySm,
                color = OnSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            color = Color(0xFF252A36), // surface-container-high
            shape = CircleShape,
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Shield, null, Modifier.size(10.dp), CyberCyan)
                Text(
                    text = "SECURE", 
                    style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.sp), 
                    color = CyberCyan, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReadinessScoreCard(readiness: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLow)
    ) {
        // 1. Layer 1: Background Decorative Glow (Top-Right only, behind content)
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.TopEnd)
                .offset(x = 25.dp, y = (-25).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(36.dp)
        )

        // 2. Layer 2: Radiating Glow directly behind the Rocket icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = 5.dp) // Aligned with rocket icon
                .background(
                    Brush.radialGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(20.dp)
        )

        // 3. Layer 3: Content Layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("SYSTEMS DIAGNOSTIC", style = PulseCastType.labelTelemetrySm, color = CyberCyan, letterSpacing = 0.5.sp, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("$readiness", style = PulseCastType.headlineLg.copy(fontSize = 32.sp), fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("%", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, modifier = Modifier.padding(bottom = 4.dp))
                    Text("FLIGHT READY", style = PulseCastType.labelTelemetryMd, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                }
                Text("5 checks verified • 1 manual review \nrecommended", style = PulseCastType.bodySm, color = OnSurfaceMuted)
            }

            // Radial Meter
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF1B202B),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = SecondaryFixedDim,
                        startAngle = -90f,
                        sweepAngle = 360f * (readiness / 100f),
                        useCenter = false,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Icon(
                    imageVector = Icons.Default.RocketLaunch, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp), 
                    tint = SecondaryFixedDim
                )
            }
        }
    }
}

@Composable
private fun TargetDestinationCard() {
    Surface(
        color = Color(0xFF171B27),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Podcasts, null, Modifier.size(18.dp), PrimaryContainer)
                    Text("Target Destination", style = PulseCastType.headlineSm.copy(fontSize = 16.sp))
                }
                Surface(color = Color(0xFF252A36), shape = RoundedCornerShape(4.dp)) {
                    Text("SRT DUAL-RELAY", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = Color(0xFF252A36), shape = RoundedCornerShape(4.dp)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.PlayCircle, null, Modifier.size(14.dp), PrimaryContainer)
                        Text("YouTube Live", style = PulseCastType.buttonText, fontSize = 11.sp)
                    }
                }
                Text("&", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Surface(color = Color(0xFF252A36), shape = RoundedCornerShape(4.dp)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Tv, null, Modifier.size(14.dp), CyberCyan)
                        Text("Twitch SRT", style = PulseCastType.buttonText, fontSize = 11.sp)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("1080p60", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 10.sp)
                }
                Text("•", color = OnSurfaceMuted)
                Text("8,500 Kbps CBR", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 10.sp)
                Text("•", color = OnSurfaceMuted)
                Text("HEVC (H.265)", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun BroadcastMetadataCard() {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Badge, null, Modifier.size(18.dp), PrimaryContainer)
                    Text("Broadcast Metadata", style = PulseCastType.buttonText)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.clickable { }) {
                    Text("Edit", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp), CyberCyan)
                }
            }
            Surface(color = Color(0xFF090E19), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(10.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🔴 ROAD TO PREDATOR RANK | Apex Mobile Season 9 | 120 FPS Ultra", 
                        style = PulseCastType.bodyMd.copy(fontSize = 13.sp), 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 2, 
                        overflow = TextOverflow.Visible
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PillTag("Apex Legends Mobile")
                        PillTag("Ranked Grind")
                    }
                    // Audience on its own line as requested
                    PillTag("Audience: Public (All Ages)", color = SecondaryFixedDim)
                }
            }
        }
    }
}

@Composable
private fun PillTag(text: String, modifier: Modifier = Modifier, color: Color = OnSurface) {
    Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp), modifier = modifier) {
        Text(text = text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = color, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun VerificationList(
    batteryLevel: Int,
    batteryCharging: Boolean,
    thermalStatus: String,
    networkQuality: String,
    micDbLevel: Float,
    dndGranted: Boolean,
    dndArmed: Boolean,
    viewModel: PreFlightViewModel,
    context: android.content.Context
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VerificationItem(
            "Battery & Thermal Power", 
            "${batteryLevel}%", 
            if (batteryCharging) "Charging • Thermal: $thermalStatus" else "Discharging • Thermal: $thermalStatus", 
            if (batteryCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd, 
            if (batteryLevel >= 20) CyberCyan else NeonAmber
        )
        VerificationItem(
            "Wi-Fi / Network Uplink", 
            networkQuality, 
            "Link connection active", 
            Icons.Default.WifiTethering, 
            if (networkQuality != "POOR") CyberCyan else NeonAmber
        )
        AudioVerificationItem(micDbLevel, dndArmed)
        DndWarningItem(dndGranted, dndArmed, viewModel, context)
        VerificationItem("Privacy Mask Engine", "ON", "Auto-blackout on password inputs & PIN screens", Icons.Default.VisibilityOff, CyberCyan)
        VerificationItem("Vulkan Hardware Mux", "38.5°C", "Nominal SoC temp • GPU clock locked at 60 FPS", Icons.Default.Memory, CyberCyan)
    }
}

@Composable
private fun VerificationItem(title: String, badge: String, detail: String, icon: ImageVector, color: Color) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(20.dp), color) }
            }
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title, 
                        style = PulseCastType.buttonText.copy(fontSize = 11.sp), // Reduced to ensure visibility
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(color = Color(0xFF1B202B), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = badge, 
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp), 
                            style = PulseCastType.labelTelemetrySm, 
                            color = color, 
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(detail, style = PulseCastType.bodySm.copy(fontSize = 10.sp), color = OnSurfaceMuted, maxLines = 2, overflow = TextOverflow.Visible)
            }
            Surface(color = color.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(22.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, Modifier.size(14.dp), color) }
            }
        }
    }
}

@Composable
private fun AudioVerificationItem(micDbLevel: Float, dndArmed: Boolean) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = CyberCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Mic, null, Modifier.size(20.dp), CyberCyan) }
                }
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Mic Input & DMCA Shield", 
                            style = PulseCastType.buttonText.copy(fontSize = 11.sp), 
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(color = Color(0xFF1B202B), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = if (dndArmed) "ARMED" else "UNARMED", 
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp), 
                                style = PulseCastType.labelTelemetrySm, 
                                color = CyberCyan, 
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                    Text("Cardioid Active • Low latency filter active", style = PulseCastType.bodySm.copy(fontSize = 10.sp), color = OnSurfaceMuted, maxLines = 1, overflow = TextOverflow.Visible)
                }
                Surface(color = CyberCyan.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(22.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, Modifier.size(14.dp), CyberCyan) }
                }
            }
            // Mini VU Meter
            Surface(color = Color(0xFF090E19), shape = RoundedCornerShape(8.dp)) {
                Row(Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${micDbLevel.toInt()}dB", style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = OnSurfaceMuted)
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        val fraction = ((micDbLevel + 60f) / 60f).coerceIn(0f, 1f)
                        val totalBoxes = 8
                        val activeBoxes = (fraction * totalBoxes).toInt()
                        for (i in 0 until totalBoxes) {
                            val color = if (i < activeBoxes) {
                                if (i < 5) SecondaryFixedDim else NeonAmber
                            } else {
                                Color(0xFF303541)
                            }
                            Box(Modifier.weight(1f).height(if (i < 5) 3.5.dp else 3.0.dp).clip(CircleShape).background(color))
                        }
                    }
                    Text(if (micDbLevel > -50f) "OPTIMAL" else "SILENT", style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DndWarningItem(dndGranted: Boolean, dndArmed: Boolean, viewModel: PreFlightViewModel, context: android.content.Context) {
    Surface(color = Color(0xFF281900).copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.3f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = NeonAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(if (dndArmed) Icons.Default.DoNotDisturbOn else Icons.Default.NotificationsActive, null, Modifier.size(20.dp), NeonAmber) }
                }
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (dndArmed) "Do Not Disturb (DND): Active" else "Do Not Disturb (DND): Inactive",
                            style = PulseCastType.buttonText.copy(fontSize = 11.sp, lineHeight = 13.sp), 
                            color = NeonAmber,
                            maxLines = 2,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(color = NeonAmber, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = if (dndArmed) "ARMED" else "WARNING", 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                                style = PulseCastType.labelTelemetrySm, 
                                color = Color(0xFF332B00), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                    Text("Incoming WhatsApp, calls, or popups may be captured directly on live video stream!", style = PulseCastType.bodySm.copy(fontSize = 10.sp), color = OnSurfaceMuted)
                }
            }
            Button(
                onClick = {
                    if (dndGranted) viewModel.armDndShield(context)
                    else viewModel.openDndSettings(context)
                },
                modifier = Modifier.align(Alignment.End).height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = Color(0xFF332B00)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.DoNotDisturbOn, null, Modifier.size(14.dp))
                    Text(if (dndArmed) "DND Armed ✓" else if (dndGranted) "Enable DND Shield Now" else "Grant DND Permission", style = PulseCastType.buttonText.copy(fontSize = 11.sp))
                }
            }
        }
    }
}

@Composable
private fun LaunchArea(onLaunch: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onLaunch,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    PulseStatusDot(color = Color.Black)
                }
                Text("LAUNCH LIVE MULTISTREAM", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
            }
        }
        
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Science, null, Modifier.size(18.dp), CyberCyan)
                Text("Test Rehearsal (Private Unlisted Test)", style = PulseCastType.buttonText.copy(fontSize = 11.sp), maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun PreFlightPreview() {
    PulseCastTheme {
        PreFlightChecklistScreen()
    }
}
