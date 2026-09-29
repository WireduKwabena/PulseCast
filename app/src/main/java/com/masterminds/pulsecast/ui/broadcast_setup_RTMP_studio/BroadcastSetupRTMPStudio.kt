package com.masterminds.pulsecast.ui.broadcast_setup_RTMP_studio

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*
import java.util.Locale

@Composable
fun BroadcastSetupRTMPStudioScreen(
    onBack: () -> Unit = {},
    onSaveAndLaunch: () -> Unit = {}
) {
    // Benchmark Telemetry State
    var throughputMbps by remember { mutableFloatStateOf(38.4f) }
    var pingMs by remember { mutableIntStateOf(14) }
    var isMeasuring by remember { mutableStateOf(false) }

    // Ingest Destinations Toggles
    var youtubeEnabled by remember { mutableStateOf(true) }
    var twitchEnabled by remember { mutableStateOf(true) }
    var tiktokEnabled by remember { mutableStateOf(false) }
    var kickEnabled by remember { mutableStateOf(false) }

    // Custom RTMP Ingest Drawer
    var showCustomRtmpDrawer by remember { mutableStateOf(true) }
    var customServerUrl by remember { mutableStateOf("rtmp://live.streamingserver.com/app/") }
    var customStreamKey by remember { mutableStateOf("live_sec_9934xkz89324hjk") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var manualBitrateKbps by remember { mutableIntStateOf(8000) }
    var isHandshakeValidated by remember { mutableStateOf(false) }

    // Widgets Layer Toggles
    var alertBoxEnabled by remember { mutableStateOf(true) }
    var chatOverlayEnabled by remember { mutableStateOf(true) }
    var audienceGoalEnabled by remember { mutableStateOf(true) }
    var sponsorBadgeEnabled by remember { mutableStateOf(true) }
    var browserSourceEnabled by remember { mutableStateOf(false) }

    // Transmission & Latency
    var ultraLowLatencyEnabled by remember { mutableStateOf(true) }
    var streamDelaySec by remember { mutableFloatStateOf(30f) }

    val snackbarHostState = remember { SnackbarHostState() }

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
            // Top Stepper / Action Bar
            TopStepperActionBar(onBack = onBack)

            // Telemetry & Network Ping Diagnostic Hub
            IngestRouteHealthHub(
                throughputMbps = throughputMbps,
                pingMs = pingMs,
                isMeasuring = isMeasuring,
                onRunBenchmark = {
                    isMeasuring = true
                    throughputMbps = (36..44).random().toFloat()
                    pingMs = (12..18).random()
                    isMeasuring = false
                }
            )

            // Multistreaming Ingest Endpoints
            BroadcastIngestEndpointsSection(
                youtubeEnabled = youtubeEnabled,
                onYoutubeToggle = { youtubeEnabled = it },
                twitchEnabled = twitchEnabled,
                onTwitchToggle = { twitchEnabled = it },
                tiktokEnabled = tiktokEnabled,
                onTiktokToggle = { tiktokEnabled = it },
                kickEnabled = kickEnabled,
                onKickToggle = { kickEnabled = it },
                showCustomRtmpDrawer = showCustomRtmpDrawer,
                onCustomRtmpToggle = { showCustomRtmpDrawer = !showCustomRtmpDrawer },
                customServerUrl = customServerUrl,
                onServerUrlChange = { customServerUrl = it },
                customStreamKey = customStreamKey,
                onStreamKeyChange = { customStreamKey = it },
                isKeyVisible = isKeyVisible,
                onKeyVisibilityToggle = { isKeyVisible = !isKeyVisible },
                manualBitrateKbps = manualBitrateKbps,
                onBitrateChange = { manualBitrateKbps = it },
                isHandshakeValidated = isHandshakeValidated,
                onValidateHandshake = { isHandshakeValidated = true }
            )

            // Studio Overlay & Widget Builder
            StudioOverlayWidgetBuilderSection(
                alertBoxEnabled = alertBoxEnabled,
                onAlertBoxToggle = { alertBoxEnabled = it },
                chatOverlayEnabled = chatOverlayEnabled,
                onChatOverlayToggle = { chatOverlayEnabled = it },
                audienceGoalEnabled = audienceGoalEnabled,
                onAudienceGoalToggle = { audienceGoalEnabled = it },
                sponsorBadgeEnabled = sponsorBadgeEnabled,
                onSponsorBadgeToggle = { sponsorBadgeEnabled = it },
                browserSourceEnabled = browserSourceEnabled,
                onBrowserSourceToggle = { browserSourceEnabled = it }
            )

            // Transmission & Latency Control
            TransmissionLatencyControlSection(
                ultraLowLatencyEnabled = ultraLowLatencyEnabled,
                onUltraLowLatencyToggle = { ultraLowLatencyEnabled = it },
                streamDelaySec = streamDelaySec,
                onDelayChange = { streamDelaySec = it }
            )

            // Bottom Action Area
            BottomActionSection(
                onSaveAndLaunch = onSaveAndLaunch
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopStepperActionBar(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface, modifier = Modifier.size(18.dp))
                }
            }

            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("STAGE 02 / 03: BROADCAST PIPELINE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Presets", tint = OnSurface, modifier = Modifier.size(18.dp))
                }
            }
        }

        Column {
            Text("Stream Setup & Studio Widgets", style = PulseCastType.headlineLg, color = OnSurface)
            Text("Multi-destination ingestion engine, low-latency telemetry & overlay composer", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun IngestRouteHealthHub(
    throughputMbps: Float,
    pingMs: Int,
    isMeasuring: Boolean,
    onRunBenchmark: () -> Unit
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
                    Icon(Icons.Default.Speed, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                    Text("Ingest Route Health", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("US-East (Virginia)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                }
            }

            // Live Telemetry Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TelemetryPill("Throughput", String.format(Locale.US, "%.1f", throughputMbps), "Mbps", SecondaryFixedDim, Modifier.weight(1f))
                TelemetryPill("Latency", "$pingMs", "ms", NeonAmber, Modifier.weight(1f))
                TelemetryPill("Jitter / Loss", "0.02", "ms", PrimaryContainer, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Connection: Pristine 4K60 Ingest Ready", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                }

                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onRunBenchmark() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                        Text(if (isMeasuring) "Measuring..." else "Run Benchmark", style = PulseCastType.buttonText, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(title: String, value: String, unit: String, unitColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, style = PulseCastType.labelTelemetryLg, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(unit, style = PulseCastType.labelTelemetrySm, color = unitColor, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun BroadcastIngestEndpointsSection(
    youtubeEnabled: Boolean,
    onYoutubeToggle: (Boolean) -> Unit,
    twitchEnabled: Boolean,
    onTwitchToggle: (Boolean) -> Unit,
    tiktokEnabled: Boolean,
    onTiktokToggle: (Boolean) -> Unit,
    kickEnabled: Boolean,
    onKickToggle: (Boolean) -> Unit,
    showCustomRtmpDrawer: Boolean,
    onCustomRtmpToggle: () -> Unit,
    customServerUrl: String,
    onServerUrlChange: (String) -> Unit,
    customStreamKey: String,
    onStreamKeyChange: (String) -> Unit,
    isKeyVisible: Boolean,
    onKeyVisibilityToggle: () -> Unit,
    manualBitrateKbps: Int,
    onBitrateChange: (Int) -> Unit,
    isHandshakeValidated: Boolean,
    onValidateHandshake: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Hub, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                Text("Broadcast Ingest Endpoints", style = PulseCastType.buttonText, color = OnSurface)
            }
            Text("2 ACTIVE DESTINATIONS", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
        }

        // Platform Cards
        EndpointCard("YouTube Live", "1080p60", "AuraGamer Official • ••••••••b819", youtubeEnabled, onYoutubeToggle, Icons.Default.SmartDisplay, PrimaryContainer)
        EndpointCard("Twitch", "CBR 6000k", "AuraGaming_Live • Auto US-East", twitchEnabled, onTwitchToggle, Icons.Default.SportsEsports, SecondaryFixedDim)
        EndpointCard("TikTok Live", "9:16 Portrait", "Ready • 1080x1920", tiktokEnabled, onTiktokToggle, Icons.Default.MusicNote, OnSurface)
        EndpointCard("Kick", "1080p60", "AuraStrike • Standby", kickEnabled, onKickToggle, Icons.Default.Bolt, NeonAmber)

        // Custom RTMP Drawer Card
        Surface(
            color = SurfaceLow,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCustomRtmpToggle() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Custom Ingestion / RTMP", style = PulseCastType.headlineSm, color = OnSurface)
                                Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                                    Text("SRT / RTMP", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 7.sp)
                                }
                            }
                            Text("Direct server ingest endpoint & handshake tokens", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        }
                    }
                    Icon(if (showCustomRtmpDrawer) Icons.Default.ExpandLess else Icons.Default.Tune, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                }

                if (showCustomRtmpDrawer) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("RTMP SERVER GATEWAY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            OutlinedTextField(
                                value = customServerUrl,
                                onValueChange = onServerUrlChange,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFF090E19), unfocusedContainerColor = Color(0xFF090E19), focusedBorderColor = SecondaryFixedDim, unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("INGESTION STREAM KEY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            OutlinedTextField(
                                value = customStreamKey,
                                onValueChange = onStreamKeyChange,
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFF090E19), unfocusedContainerColor = Color(0xFF090E19), focusedBorderColor = SecondaryFixedDim, unfocusedBorderColor = Color.Transparent),
                                trailingIcon = {
                                    IconButton(onClick = onKeyVisibilityToggle) {
                                        Icon(if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = onValidateHandshake,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isHandshakeValidated) SignalGreen else SurfaceHigh, contentColor = if (isHandshakeValidated) Color.Black else SecondaryFixedDim),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(if (isHandshakeValidated) Icons.Default.CheckCircle else Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(if (isHandshakeValidated) "Handshake Validated (200 OK)" else "Validate Handshake with RTMP Ingest", style = PulseCastType.buttonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndpointCard(title: String, badge: String, detail: String, enabled: Boolean, onToggle: (Boolean) -> Unit, icon: ImageVector, color: Color) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = PulseCastType.headlineSm, color = OnSurface)
                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                            Text(badge, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(detail, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrimaryContainer),
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}

@Composable
private fun StudioOverlayWidgetBuilderSection(
    alertBoxEnabled: Boolean,
    onAlertBoxToggle: (Boolean) -> Unit,
    chatOverlayEnabled: Boolean,
    onChatOverlayToggle: (Boolean) -> Unit,
    audienceGoalEnabled: Boolean,
    onAudienceGoalToggle: (Boolean) -> Unit,
    sponsorBadgeEnabled: Boolean,
    onSponsorBadgeToggle: (Boolean) -> Unit,
    browserSourceEnabled: Boolean,
    onBrowserSourceToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                Text("Studio Overlay & Widgets", style = PulseCastType.buttonText, color = OnSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                Text("Add Layer", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold)
            }
        }

        // Live Preview Stage Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090E19)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF090E19))))
            }

            // Real-time HUD Badges
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(color = Color(0xFF090E19).copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp)) {
                    Text("PREVIEW 1080p", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                }
                Surface(color = PrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text("REC READY", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (sponsorBadgeEnabled) {
                Surface(
                    color = Color(0xFF090E19).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(10.dp))
                        Text("CyberEnergy.png", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
                    }
                }
            }

            if (audienceGoalEnabled) {
                Surface(
                    color = SurfaceHigh.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp, start = 12.dp, end = 12.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sub Goal", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("842 / 1,000 (84%)", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
                        }
                        LinearProgressIndicator(progress = { 0.84f }, modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape), color = SecondaryFixedDim, trackColor = SurfaceMid)
                    }
                }
            }

            if (chatOverlayEnabled) {
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(4.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Chat Layer: Auto-hide in 8s", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
            }
        }

        // Active Layer Cards
        WidgetLayerRow("Event Alert Box", "Donations • Subs • Sound: Neon Chime 75%", Icons.Default.NotificationsActive, NeonAmber, alertBoxEnabled, onAlertBoxToggle)
        WidgetLayerRow("Dynamic Chat Overlay", "Size 14px • Auto-hide: 8s idle • Badges ON", Icons.Default.Forum, SecondaryFixedDim, chatOverlayEnabled, onChatOverlayToggle)
        WidgetLayerRow("Live Audience Target", "Sub Goal: 842 / 1,000 target count", Icons.Default.TrackChanges, PrimaryContainer, audienceGoalEnabled, onAudienceGoalToggle)
        WidgetLayerRow("Sponsor Badge", "CyberEnergy.png • Top-Right (80% opacity)", Icons.Default.BrandingWatermark, OnSurfaceMuted, sponsorBadgeEnabled, onSponsorBadgeToggle)
        WidgetLayerRow("Browser Source (URL)", "Streamlabs / StreamElements custom widget", Icons.Default.Language, SecondaryFixedDim, browserSourceEnabled, onBrowserSourceToggle)
    }
}

@Composable
private fun WidgetLayerRow(title: String, detail: String, icon: ImageVector, color: Color, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Column {
                    Text(title, style = PulseCastType.headlineSm, color = OnSurface, fontSize = 12.sp)
                    Text(detail, style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrimaryContainer),
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}

@Composable
private fun TransmissionLatencyControlSection(
    ultraLowLatencyEnabled: Boolean,
    onUltraLowLatencyToggle: (Boolean) -> Unit,
    streamDelaySec: Float,
    onDelayChange: (Float) -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                Text("Transmission & Latency Control", style = PulseCastType.buttonText, color = OnSurface)
            }

            // Ultra-Low Latency Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ultra-Low Latency Mode", style = PulseCastType.headlineSm, color = OnSurface, fontSize = 12.sp)
                    Text("Under 1.2s glass-to-glass delay for hyper-reactive gamer chat", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
                Switch(checked = ultraLowLatencyEnabled, onCheckedChange = onUltraLowLatencyToggle, modifier = Modifier.scale(0.8f))
            }

            // Anti-Stream Sniping Delay Buffer
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                        Text("Anti-Stream Sniping Delay", style = PulseCastType.headlineSm, color = OnSurface, fontSize = 11.sp)
                    }
                    Text(if (streamDelaySec == 0f) "OFF (NO DELAY)" else "${streamDelaySec.toInt()} SECONDS", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = streamDelaySec,
                    onValueChange = onDelayChange,
                    valueRange = 0f..120f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = PrimaryContainer, inactiveTrackColor = SurfaceHigh),
                    modifier = Modifier.height(18.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Instant (0s)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    Text("Competitive (30s)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    Text("Tournament (120s)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun BottomActionSection(onSaveAndLaunch: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(100.dp).height(50.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Save", style = PulseCastType.buttonText, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onSaveAndLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color.Black))
                    Text("Save & Launch Live Studio", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        Text("PulseCast RTMP Tunnel v4.2.1 • Hardware Accelerated MediaCodec", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun BroadcastSetupRTMPStudioPreview() {
    PulseCastTheme {
        BroadcastSetupRTMPStudioScreen()
    }
}
