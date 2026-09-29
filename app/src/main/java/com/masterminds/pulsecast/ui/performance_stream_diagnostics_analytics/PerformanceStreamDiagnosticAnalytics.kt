package com.masterminds.pulsecast.ui.performance_stream_diagnostics_analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterminds.pulsecast.ui.theme.*
import java.util.Locale

@Composable
fun PerformanceStreamDiagnosticAnalyticsScreen(
    viewModel: DiagnosticsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val fps by viewModel.fps.collectAsState()
    val bitrateMbps by viewModel.bitrateMbps.collectAsState()
    val socTempC by viewModel.socTempC.collectAsState()
    val batteryPct by viewModel.batteryPct.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()
    val bitrateHistory by viewModel.bitrateHistory.collectAsState()

    var activeTimeTab by remember { mutableStateOf("5m") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.startMonitoring()
        viewModel.optimizeEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        containerColor = BgBase,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sub-header Bar & Status
            SubHeaderStatusStrip(
                onBack = onBack,
                onAutoTune = { viewModel.autoOptimize() }
            )

            // Primary Broadcast Telemetry Master Card
            PrimaryBroadcastTelemetryCard(
                fps = fps,
                bitrateMbps = bitrateMbps
            )

            // Throughput & Pacing Bezier Graph
            ThroughputAndPacingGraphSection(
                activeTimeTab = activeTimeTab,
                onTimeTabSelect = { activeTimeTab = it },
                fpsHistory = fpsHistory,
                bitrateHistory = bitrateHistory,
                currentBitrate = bitrateMbps
            )

            // Thermal & SoC Telemetry Deck
            ThermalAndSocTelemetryDeck(
                socTempC = socTempC,
                batteryPct = batteryPct
            )

            // Multistream Audience & Session Analytics
            MultistreamAudienceSection()

            // Diagnostic Suite & Quick Tools
            DiagnosticSuiteToolsSection(
                onFlushBuffers = {
                    viewModel.autoOptimize()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SubHeaderStatusStrip(onBack: () -> Unit, onAutoTune: () -> Unit) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("DIAGNOSTICS & HUD", style = PulseCastType.headlineSm, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    }
                    Text("HEALTH: 98.4% • STAGE A • NOMINAL", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onAutoTune() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                        Text("AUTO-TUNE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = SurfaceHigh,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Download, contentDescription = "Export Logs", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryBroadcastTelemetryCard(fps: Float, bitrateMbps: Float) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                        Text("COMPOSITE ENGINE VITALS", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("98.4%", style = PulseCastType.headlineXl, color = OnSurface, fontWeight = FontWeight.Bold)
                        Surface(color = SecondaryFixedDim.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                            Text("EXCELLENT", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("SESSION CLOCK", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    Surface(
                        color = Color(0xFF090E19),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(PrimaryContainer))
                            Text("01:14:28", style = PulseCastType.labelTelemetryMd, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Telemetry Quad Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuadTelemetryPill("FRAMERATE", String.format(Locale.US, "%.1f / 60 FPS", fps), "Loss: 0.01%", Icons.Default.Speed, SecondaryFixedDim, Modifier.weight(1f))
                QuadTelemetryPill("INGEST BITRATE", String.format(Locale.US, "%d Kbps", (bitrateMbps * 1000).toInt()), "Target: 8,500 CBR", Icons.Default.SwapVert, SecondaryFixedDim, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuadTelemetryPill("RENDER LATENCY", "14.2 ms", "GPU DSP: 3.8 ms", Icons.Default.Timer, SecondaryFixedDim, Modifier.weight(1f))
                QuadTelemetryPill("NETWORK BUFFER", "0.00 %", "Zero Drop Lock", Icons.Default.CloudSync, PrimaryContainer, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuadTelemetryPill(title: String, mainValue: String, subDetail: String, icon: ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
            }
            Text(mainValue, style = PulseCastType.labelTelemetryLg, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subDetail, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
        }
    }
}

@Composable
private fun ThroughputAndPacingGraphSection(
    activeTimeTab: String,
    onTimeTabSelect: (String) -> Unit,
    fpsHistory: List<Float>,
    bitrateHistory: List<Float>,
    currentBitrate: Float
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(width = 4.dp, height = 14.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Throughput & Pacing", style = PulseCastType.buttonText, color = OnSurface)
                }

                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        listOf("5m", "15m", "Live", "All").forEach { tab ->
                            val isSelected = activeTimeTab == tab
                            Surface(
                                color = if (isSelected) SurfaceHigh else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onTimeTabSelect(tab) }
                            ) {
                                Text(
                                    text = tab,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = PulseCastType.labelTelemetrySm,
                                    color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Live Telemetry Sparkline & Canvas Container
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(width = 8.dp, height = 2.dp).background(SecondaryFixedDim))
                                Text("Bitrate (Kbps)", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(4.dp).clip(CircleShape).background(PrimaryContainer))
                                Text("Jitter Spike", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp)
                            }
                        }
                        Text("AVG: 8,460", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }

                    // Canvas Graphic Curve
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxBitrate = 12f
                            val maxFps = 120f
                            val stepX = size.width / 60f

                            // Grid reference lines
                            val strokeGrid = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                            drawLine(Color.DarkGray.copy(alpha = 0.3f), Offset(0f, size.height * 0.25f), Offset(size.width, size.height * 0.25f), strokeWidth = strokeGrid.width, pathEffect = strokeGrid.pathEffect)
                            drawLine(Color.DarkGray.copy(alpha = 0.3f), Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = strokeGrid.width, pathEffect = strokeGrid.pathEffect)
                            drawLine(Color.DarkGray.copy(alpha = 0.3f), Offset(0f, size.height * 0.75f), Offset(size.width, size.height * 0.75f), strokeWidth = strokeGrid.width, pathEffect = strokeGrid.pathEffect)

                            // Bitrate History Path
                            if (bitrateHistory.isNotEmpty()) {
                                val bitratePath = Path()
                                val fillPath = Path()
                                fillPath.moveTo(0f, size.height)

                                bitrateHistory.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = size.height - (value / maxBitrate * size.height)
                                    if (index == 0) {
                                        bitratePath.moveTo(x, y)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        bitratePath.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }
                                }

                                fillPath.lineTo(size.width, size.height)
                                fillPath.close()

                                drawPath(
                                    fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(SecondaryFixedDim.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                                drawPath(bitratePath, SecondaryFixedDim, style = Stroke(width = 2.dp.toPx()))
                            }

                            // FPS Curve Overlay
                            if (fpsHistory.isNotEmpty()) {
                                val fpsPath = Path()
                                fpsHistory.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = size.height - (value / maxFps * size.height)
                                    if (index == 0) fpsPath.moveTo(x, y) else fpsPath.lineTo(x, y)
                                }
                                drawPath(fpsPath, PrimaryContainer.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))
                            }
                        }

                        // Floating Bitrate Pin
                        Surface(
                            color = SurfaceHigh.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 24.dp, top = 8.dp)
                        ) {
                            Text(
                                text = "${(currentBitrate * 1000).toInt()} Kbps",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = PulseCastType.labelTelemetrySm,
                                color = SecondaryFixedDim,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Time Markers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("-05:00", "-03:45", "-02:30", "-01:15", "NOW").forEach { mark ->
                            Text(mark, style = PulseCastType.labelTelemetrySm, color = if (mark == "NOW") SecondaryFixedDim else OnSurfaceMuted, fontSize = 8.sp, fontWeight = if (mark == "NOW") FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThermalAndSocTelemetryDeck(socTempC: Float, batteryPct: Int) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(width = 4.dp, height = 14.dp).clip(CircleShape).background(NeonAmber))
                    Text("Thermal & SoC Telemetry", style = PulseCastType.buttonText, color = OnSurface)
                }

                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("NOMINAL", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thermal Core
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SOC TEMP", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(14.dp))
                        }
                        Text(String.format(Locale.US, "%.1f°C", socTempC), style = PulseCastType.labelTelemetryLg, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LinearProgressIndicator(
                            progress = { (socTempC / 60f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = NeonAmber,
                            trackColor = SurfaceMid
                        )
                        Text("Threshold: < 48.0°C", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }

                // Battery Discharge
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BATTERY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Icon(Icons.Default.BatterySaver, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                        }
                        Text("$batteryPct%", style = PulseCastType.labelTelemetryLg, color = PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LinearProgressIndicator(
                            progress = { batteryPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = PrimaryContainer,
                            trackColor = SurfaceMid
                        )
                        Text("~3h 12m stream left", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    }
                }
            }

            // GPU & CPU Load Progress Bars
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GPU Pipeline (Vulkan Compute)", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                            Text("68%", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        LinearProgressIndicator(progress = { 0.68f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = SecondaryFixedDim, trackColor = SurfaceMid)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CPU Core Load (Octa-Thread)", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                            Text("52%", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        LinearProgressIndicator(progress = { 0.52f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = OnSurfaceMuted, trackColor = SurfaceMid)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RAM Buffer: 1.42 GB / 12 GB", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        Text("Leak Protection: ACTIVE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MultistreamAudienceSection() {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(width = 4.dp, height = 14.dp).clip(CircleShape).background(PrimaryContainer))
                    Text("Multistream Audience", style = PulseCastType.buttonText, color = OnSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(PrimaryContainer))
                    Text("14,892 PEAK", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }

            // Channel Performance Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudienceChannelCard("YouTube Live", "RTMP • 22ms", "8,240", PrimaryContainer, Modifier.weight(1f))
                AudienceChannelCard("Twitch SRT", "SRT • 18ms", "4,170", SecondaryFixedDim, Modifier.weight(1f))
            }

            // Session Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                        Column {
                            Text("CHAT VELOCITY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Text("342 msgs/min", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                        Column {
                            Text("SESSION REVENUE", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Text("$184.50 USD", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudienceChannelCard(platform: String, pingDetail: String, viewers: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(platform, style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                Text(pingDetail, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(viewers, style = PulseCastType.headlineSm, color = OnSurface, fontWeight = FontWeight.Bold)
                Text("VIEWERS", style = PulseCastType.labelTelemetrySm, color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DiagnosticSuiteToolsSection(onFlushBuffers: () -> Unit) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(width = 4.dp, height = 14.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Diagnostic Suite & Tools", style = PulseCastType.buttonText, color = OnSurface)
                }
                Text("HEURISTIC V4", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            }

            // Wi-Fi 6E Uplink Card
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.WifiTethering, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Current Wi-Fi 6E Uplink", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                            Text("94.2 Mbps Stable Bandwidth", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                        }
                    }

                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Re-Test", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = PulseCastType.buttonText, color = OnSurface, fontSize = 9.sp)
                    }
                }
            }

            // Quick Actions Duo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFlushBuffers() }
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                        Text("Flush Buffers", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Text("Zero-drop render cache reset", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }

                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                        Text("Adaptive Guard", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Text("Enabled (Cellular failover)", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun PerformanceStreamDiagnosticAnalyticsPreview() {
    PulseCastTheme {
        PerformanceStreamDiagnosticAnalyticsScreen()
    }
}
