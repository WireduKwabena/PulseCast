package com.masterminds.pulsecast.ui.live_multistream_studio

import android.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*

import com.masterminds.pulsecast.ui.pre_stream_go_live_safety_checklist.PreFlightChecklistScreen
import com.masterminds.pulsecast.streaming.BroadcastDestinationStore
import com.masterminds.pulsecast.streaming.ConnectionState

@Composable
fun LiveMultiStreamStudioScreen(
    modifier: Modifier = Modifier,
    isBroadcasting: Boolean = false,
    onGoLive: () -> Unit = {},
    onStopBroadcast: () -> Unit = {},
    onAddCustomRTMP: () -> Unit = {},
    onOpenFacecam: () -> Unit = {},
    onOpenMixer: () -> Unit = {},
    onOpenOverlays: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var showSafetyChecklist by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgBase)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiveViewfinderCanvas(onOpenFacecam)

            MultistreamTargetHub(onAddCustomRTMP)

            StudioWidgetsOverlay(onOpenOverlays)

            BroadcastAudioMixer(onOpenMixer)

            StudioScenePresets()

            BottomActionDock(
                isBroadcasting = isBroadcasting,
                onToggleBroadcast = { 
                    if (!isBroadcasting) showSafetyChecklist = true
                    else onStopBroadcast()
                },
                onOpenSettings = onOpenSettings
            )

            Spacer(Modifier.height(80.dp)) // Nav bar padding
        }

        if (showSafetyChecklist) {
            PreFlightChecklistScreen(
                onDismiss = { showSafetyChecklist = false },
                onLaunch = { 
                    showSafetyChecklist = false
                    onGoLive()
                }
            )
        }
    }
}

@Composable
private fun LiveViewfinderCanvas(onOpenFacecam: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .heightIn(max = 520.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090E19)) // surface-container-lowest
    ) {
        // 1. Simulated Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_menu_gallery), // Placeholder
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.85f),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090E19).copy(alpha = 0.6f),
                            Color.Transparent,
                            Color(0xFF090E19).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // 2. Safe Zones (Simplified Guidelines)
        Box(modifier = Modifier.fillMaxSize().padding(16.dp).alpha(0.4f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("9:16 SAFE ZONE", style = PulseCastType.labelTelemetrySm, color = CyberCyan, modifier = Modifier.background(BgBase.copy(alpha = 0.8f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                Text("PUNCH-HOLE AVOID", style = PulseCastType.labelTelemetrySm, color = CyberCyan, modifier = Modifier.background(BgBase.copy(alpha = 0.8f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        // 3. Top HUD Strip
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live State Pill
            Surface(
                color = BgBase.copy(alpha = 0.9f),
                shape = CircleShape,
                modifier = Modifier.height(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PulseStatusDot(color = PrimaryContainer)
                    Text("STANDBY", style = PulseCastType.labelTelemetrySm, color = ElectricRuby, fontWeight = FontWeight.Bold)
                    Text("•", color = OnSurfaceMuted, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Group, null, Modifier.size(14.dp), CyberCyan)
                        Text("2.04k", style = PulseCastType.labelTelemetrySm, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Controls
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = BgBase.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { }) {
                        Icon(Icons.Default.Grid4x4, null, Modifier.size(18.dp), OnSurface)
                    }
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = BgBase.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { }) {
                        Icon(Icons.Default.AspectRatio, null, Modifier.size(18.dp), OnSurface)
                    }
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = BgBase.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onOpenFacecam)) {
                        Icon(Icons.Default.AutoFixHigh, null, Modifier.size(18.dp), OnSurface)
                    }
                }
            }
        }

        // 4. Middle Layer: Alert Box
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                color = BgBase.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectricRuby.copy(alpha = 0.2f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .padding(top = 60.dp) // Below standby row
                    .fillMaxWidth(0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(NeonAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Stars, null, Modifier.size(20.dp), NeonAmber)
                    }
                    Column(Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SUPER CHAT $25.00", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontWeight = FontWeight.Bold)
                            Text("YouTube", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        }
                        Text("NeoValkyrie: \"CLUTCH THIS DUO BRO!! 🔥\"", style = PulseCastType.bodySm, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp), OnSurfaceMuted)
                }
            }
        }

        // 5. Bottom Overlay: Chat + PIP
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
            // Chat Bubble
            Surface(
                color = Color(0xFF090E19).copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.6f).height(80.dp)
            ) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("LIVE CHAT (75%)", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    }
                    Text("Kira99: peek sniper mid!", style = PulseCastType.bodySm, color = OnSurface, maxLines = 1)
                    Text("AuraGamer: GG insane flick", style = PulseCastType.bodySm, color = OnSurface, maxLines = 1)
                }
            }

            // PIP Inset - ensuring fully visible and popping up
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 48.dp) // Lifted up further
                    .size(112.dp, 148.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceMid,
                shadowElevation = 16.dp,
                border = BorderStroke(2.dp, CyberCyan.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, Modifier.size(40.dp), OnSurfaceMuted.copy(alpha = 0.2f))
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                            .background(Color(0xFF090E19).copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("CAM 1", style = PulseCastType.labelTelemetrySm, color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                            .background(BgBase.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.OpenWith, null, Modifier.size(12.dp), OnSurfaceMuted)
                    }
                }
            }
        }

        // 6. Bottom Telemetry Bar
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("6,200 kbps", style = PulseCastType.labelTelemetrySm.copy(fontSize = 10.sp), color = OnSurface, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("60.0 FPS", style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = CyberCyan)
                    Text("0% DROPS", style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = OnSurfaceMuted)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Bolt, null, Modifier.size(12.dp), SecondaryFixedDim)
                        Text("24ms", style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = SecondaryFixedDim)
                    }
                }
            }
        }
    }
}

@Composable
private fun MultistreamTargetHub(onAddCustom: () -> Unit) {
    val destinations by BroadcastDestinationStore.destinations.collectAsState()
    val connectionStates by BroadcastDestinationStore.connectionStates.collectAsState()
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, null, Modifier.size(20.dp), CyberCyan)
                    Text("Multistream\nTargets", style = PulseCastType.headlineSm.copy(fontSize = 14.sp), lineHeight = 18.sp)
                }
                Surface(color = SecondaryContainer.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        if (destinations.isEmpty()) "NO TARGETS" else "${destinations.size} TARGET${if (destinations.size == 1) "" else "S"} ARMED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = PulseCastType.labelTelemetrySm,
                        color = if (destinations.isEmpty()) OnSurfaceMuted else SecondaryFixedDim,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (destinations.isEmpty()) {
                    item { Text("No destinations configured", color = OnSurfaceMuted, style = PulseCastType.labelTelemetrySm) }
                }
                items(destinations, key = { it.id }) { destination ->
                    val state = connectionStates[destination.id] ?: ConnectionState.Idle
                    TargetBadge(
                        title = destination.label,
                        status = connectionStatusLabel(state),
                        icon = Icons.Default.Sensors,
                        color = if (state is ConnectionState.Live) SignalGreen else CyberCyan,
                        isActive = state is ConnectionState.Live
                    )
                }
                item {
                    Surface(
                        onClick = onAddCustom,
                        color = SurfaceMid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AddCircle, null, Modifier.size(18.dp), CyberCyan)
                            Text("Custom RTMP", style = PulseCastType.buttonText, color = CyberCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetBadge(title: String, status: String, icon: ImageVector, color: Color, isActive: Boolean) {
    Surface(
        color = SurfaceHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(44.dp).alpha(if (isActive) 1f else 0.6f)
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(24.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(16.dp), color)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = PulseCastType.bodyMd, fontWeight = FontWeight.Bold)
                    if (isActive) Box(Modifier.size(6.dp).clip(CircleShape).background(CyberCyan))
                }
                Text(status, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

private fun connectionStatusLabel(state: ConnectionState): String = when (state) {
    ConnectionState.Idle -> "Ready • not live"
    ConnectionState.Connecting -> "Connecting…"
    ConnectionState.Live -> "Live"
    is ConnectionState.Reconnecting -> "Retrying • attempt ${state.attempt}"
    is ConnectionState.Failed -> "Failed • ${state.reason}"
}

@Composable
private fun StudioWidgetsOverlay(onOpenOverlays: () -> Unit) {
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).clickable(onClick = onOpenOverlays)
                ) {
                    Icon(Icons.Default.DashboardCustomize, null, Modifier.size(18.dp), NeonAmber)
                    Text(
                        text = "Studio Widgets Overlay", 
                        style = PulseCastType.headlineSm.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
                Text(
                    text = "TAP TO TOGGLE", 
                    style = PulseCastType.labelTelemetrySm.copy(fontSize = 7.5.sp), 
                    color = OnSurfaceMuted, 
                    maxLines = 1,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            
            // Grid with forced uniform heights
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetToggleCard("Live Chat", "Opacity: 75%", "Smart Filter", Icons.Default.ChatBubble, CyberCyan, Modifier.weight(1f).fillMaxHeight())
                    WidgetToggleCard("Alert Box", "Sound: 80%", "RGB Pop", Icons.Default.Celebration, NeonAmber, Modifier.weight(1f).fillMaxHeight())
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetToggleCard("Counter HUD", "Combined aggregate", "2.04k", Icons.Default.Visibility, ElectricRuby, Modifier.weight(1f).fillMaxHeight())
                    WidgetToggleCard("Telemetry", "Stable", "", Icons.Default.Insights, SecondaryFixedDim, Modifier.weight(1f).fillMaxHeight(), isGraph = true)
                }
            }
        }
    }
}

@Composable
private fun WidgetToggleCard(
    title: String, 
    status: String, 
    subStatus: String, 
    icon: ImageVector, 
    color: Color, 
    modifier: Modifier = Modifier, 
    isGraph: Boolean = false
) {
    Surface(color = SurfaceMid, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(icon, null, Modifier.size(13.dp), color)
                    Text(
                        text = title, 
                        style = PulseCastType.buttonText, 
                        color = OnSurface, 
                        fontSize = 9.sp, 
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
                Switch(
                    checked = true, 
                    onCheckedChange = {}, 
                    modifier = Modifier.scale(0.5f).height(12.dp).width(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.Bottom
            ) {
                if (isGraph) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        // SVG Sparkline Simulation
                        Canvas(modifier = Modifier.width(32.dp).height(10.dp)) {
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.8f)
                                quadraticTo(size.width * 0.25f, size.height * 0.2f, size.width * 0.5f, size.height * 0.6f)
                                quadraticTo(size.width * 0.75f, size.height * 0.9f, size.width, size.height * 0.3f)
                            }
                            drawPath(path, color = color, style = Stroke(width = 1.5.dp.toPx()))
                        }
                        Text(
                            text = status, 
                            style = PulseCastType.labelTelemetrySm, 
                            color = color, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                } else {
                    Text(
                        text = status, 
                        style = PulseCastType.labelTelemetrySm, 
                        color = OnSurfaceMuted, 
                        fontSize = 8.sp, 
                        modifier = Modifier.weight(1.3f),
                        maxLines = 2,
                        overflow = TextOverflow.Visible
                    )
                    if (subStatus.isNotEmpty()) {
                        Text(
                            text = subStatus, 
                            style = PulseCastType.labelTelemetrySm, 
                            color = color, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 8.sp, 
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastAudioMixer(onOpenMixer: () -> Unit) {
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable(onClick = onOpenMixer)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(20.dp), CyberCyan)
                    Text("Broadcast Audio\nMixer", style = PulseCastType.headlineSm.copy(fontSize = 14.sp), lineHeight = 18.sp)
                }
                Surface(color = SurfaceMid, shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(CyberCyan))
                        Text("48kHz 24-bit Low\nLatency", style = PulseCastType.labelTelemetrySm, color = CyberCyan, fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2)
                    }
                }
            }
            
            MixerFaderTrack("Master Broadcast", "0.0 dB", Icons.Default.VolumeUp, ElectricRuby, 0.85f)
            MixerFaderTrack("Game Internal FX", "-4.2 dB", Icons.Default.Smartphone, CyberCyan, 0.72f)
            MixerFaderTrack("Mic Input", "-1.8 dB", Icons.Default.Mic, NeonAmber, 0.78f, hasFilter = true)
            
            // Soundboard Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.GridView, null, Modifier.size(14.dp), OnSurfaceMuted)
                    Text("LIVE SOUNDBOARD PADS", style = PulseCastType.labelTelemetrySm, fontWeight = FontWeight.Bold)
                }
                Text("4 SLOTS", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Icons.Default.Campaign to "Airhorn",
                    Icons.Default.MilitaryTech to "GG Alert",
                    Icons.Default.ThumbUp to "Applause",
                    Icons.Default.Headphones to "Lo-Fi Pad"
                ).forEach { (icon, label) ->
                    Surface(color = SurfaceMid, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(54.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(icon, null, Modifier.size(20.dp), PrimaryContainer)
                            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MixerFaderTrack(label: String, db: String, icon: ImageVector, color: Color, value: Float, hasFilter: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, Modifier.size(16.dp), color)
                Text(label, style = PulseCastType.buttonText, color = OnSurface)
                if (hasFilter) {
                    Surface(color = SecondaryContainer.copy(alpha = 0.2f), shape = CircleShape) {
                        Text("AI FILTER ON", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(db, style = PulseCastType.labelTelemetrySm, color = color, fontWeight = FontWeight.Bold)
        }
        PulseVUIndicator(label = "", level = value, dbValue = "", color = color) // Reusing VU Indicator logic
        Slider(value = value, onValueChange = {}, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = color, inactiveTrackColor = SurfaceHigh), modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun StudioScenePresets() {
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("OVERLAY SCENE SELECTOR", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SceneButton("Scene 1: Game+Cam", Icons.Default.Splitscreen, CyberCyan, true, Modifier.weight(1f))
                SceneButton("Scene 2: Full Cam", Icons.Default.Face, OnSurfaceMuted, false, Modifier.weight(1f))
                SceneButton("Scene 3: BRB/Wait", Icons.Default.HourglassTop, OnSurfaceMuted, false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SceneButton(label: String, icon: ImageVector, color: Color, isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isSelected) SurfaceHigh else SurfaceMid,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(64.dp),
        border = if (isSelected) BorderStroke(1.dp, color) else null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(4.dp)) {
            Icon(icon, null, Modifier.size(20.dp), color)
            Text(label, style = PulseCastType.labelTelemetrySm, color = if (isSelected) color else OnSurfaceMuted, textAlign = TextAlign.Center, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 8.sp)
        }
    }
}

@Composable
private fun BottomActionDock(isBroadcasting: Boolean, onToggleBroadcast: () -> Unit, onOpenSettings: () -> Unit) {
    Surface(
        color = BgBase.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = SurfaceHigh) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { }) {
                    Icon(Icons.Default.FlipCameraAndroid, null, Modifier.size(22.dp), OnSurface)
                }
            }
            
            val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
            val liveScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Button(
                onClick = onToggleBroadcast,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .scale(if (isBroadcasting) 1f else liveScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBroadcasting) SurfaceHigh else PrimaryContainer,
                    contentColor = if (isBroadcasting) ElectricRuby else Color.Black
                ),
                shape = CircleShape,
                border = if (isBroadcasting) BorderStroke(2.dp, PrimaryContainer) else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sensors, 
                        null, 
                        Modifier.size(22.dp).graphicsLayer {
                            if (!isBroadcasting) {
                                alpha = liveScale - 0.2f // Subtle flicker
                            }
                        }
                    )
                    Text(if (isBroadcasting) "END BROADCAST" else "GO LIVE NOW", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = SurfaceHigh) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onOpenSettings)) {
                    Icon(Icons.Default.Palette, null, Modifier.size(22.dp), OnSurface) 
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun LiveStudioPreview() {
    PulseCastTheme {
        LiveMultiStreamStudioScreen()
    }
}
