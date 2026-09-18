package com.masterminds.pulsecast.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.masterminds.pulsecast.ui.theme.BgBase
import com.masterminds.pulsecast.ui.theme.CyberCyan
import com.masterminds.pulsecast.ui.theme.ElectricRuby
import com.masterminds.pulsecast.ui.theme.NeonAmber
import com.masterminds.pulsecast.ui.theme.SignalGreen
import com.masterminds.pulsecast.ui.theme.SurfaceHigh
import com.masterminds.pulsecast.ui.theme.SurfaceLow
import com.masterminds.pulsecast.ui.ui_library.PulseCastAppBar

/** Routes are named after the Stitch specification so source and design stay traceable. */
object PulseCastRoute {
    const val CaptureHub = "capture_hub"
    const val LiveStudio = "live_multistream_studio"
    const val TimelineEditor = "timeline_video_editor"
    const val FloatingSettings = "floating_ball_settings"
    const val OrbCustomization = "floating_ball_customization"
    const val Vault = "studio_vault"
    const val Diagnostics = "performance_analytics"
    const val CreatorHub = "creator_profile"
    const val AudioMixer = "audio_mixer"
    const val OverlayStudio = "overlay_studio"
    const val BroadcastSetup = "broadcast_setup"
    const val FacecamChroma = "facecam_chroma"
    const val LiveHud = "live_hud"
    const val LiveChat = "live_chat"
    const val ClipExport = "clip_export"
    const val SafetyChecklist = "safety_checklist"
    const val CustomIngest = "custom_ingest"

    val primary = setOf(CaptureHub, LiveStudio, TimelineEditor, FloatingSettings)
}

@Composable
fun PulseCastNavigation(
    isRecording: Boolean,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        modifier = modifier,
        containerColor = BgBase,
        topBar = {
            PulseCastAppBar(
                onHome = { navController.navigateRoot(PulseCastRoute.CaptureHub) },
                onVault = { navController.navigate(PulseCastRoute.Vault) },
                onDiagnostics = { navController.navigate(PulseCastRoute.Diagnostics) },
                onProfile = { navController.navigate(PulseCastRoute.CreatorHub) }
            )
        },
        bottomBar = {
            if (route in PulseCastRoute.primary) {
                PulseCastBottomNavigation(route) { navController.navigateRoot(it) }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = PulseCastRoute.CaptureHub,
            modifier = Modifier.padding(padding)
        ) {
            composable(PulseCastRoute.CaptureHub) { CaptureHubDestination(isRecording, onRecord) }
            composable(PulseCastRoute.LiveStudio) {
                LiveStudioDestination(
                    onOpenFacecam = { navController.navigate(PulseCastRoute.FacecamChroma) },
                    onOpenMixer = { navController.navigate(PulseCastRoute.AudioMixer) },
                    onOpenOverlays = { navController.navigate(PulseCastRoute.OverlayStudio) },
                    onOpenSettings = { navController.navigate(PulseCastRoute.BroadcastSetup) },
                    onAddIngest = { navController.navigate(PulseCastRoute.CustomIngest) },
                    onGoLive = { navController.navigate(PulseCastRoute.SafetyChecklist) }
                )
            }
            composable(PulseCastRoute.TimelineEditor) { DestinationPlaceholder("Timeline Video Editor", "SCREEN_29") }
            composable(PulseCastRoute.FloatingSettings) { DestinationPlaceholder("Floating Ball & Settings", "SCREEN_31") }
            composable(PulseCastRoute.OrbCustomization) { OrbCustomizationDestination() }
            composable(PulseCastRoute.Vault) { DestinationPlaceholder("Studio Vault & Media Library", "SCREEN_24") }
            composable(PulseCastRoute.Diagnostics) { DestinationPlaceholder("Performance & Stream Diagnostics", "SCREEN_17") }
            composable(PulseCastRoute.CreatorHub) { DestinationPlaceholder("Creator Profile & Connected Channels", "SCREEN_15") }
            composable(PulseCastRoute.AudioMixer) { AudioMixerDestination() }
            composable(PulseCastRoute.OverlayStudio) { OverlayStudioDestination() }
            composable(PulseCastRoute.BroadcastSetup) { BroadcastSetupDestination() }
            composable(PulseCastRoute.FacecamChroma) { FacecamDestination() }
            composable(PulseCastRoute.LiveHud) { DestinationPlaceholder("Live In-Game HUD", "SCREEN_18") }
            composable(PulseCastRoute.LiveChat) { DestinationPlaceholder("Live Chat & Moderation", "SCREEN_6") }
            composable(PulseCastRoute.ClipExport) { DestinationPlaceholder("Instant Clip & Highlight Export", "SCREEN_9") }
            composable(PulseCastRoute.SafetyChecklist) { SafetyChecklistDestination(onDismiss = { navController.popBackStack() }, onLaunch = { navController.navigate(PulseCastRoute.LiveHud) }) }
            composable(PulseCastRoute.CustomIngest) { CustomIngestDestination(onDismiss = { navController.popBackStack() }, onConnect = { navController.popBackStack() }) }
        }
    }
}

private fun NavHostController.navigateRoot(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
private fun PulseCastBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    Box {
        NavigationBar(containerColor = SurfaceLow) {
            NavigationBarItem(currentRoute == PulseCastRoute.CaptureHub, { onNavigate(PulseCastRoute.CaptureHub) }, { Icon(Icons.Default.Videocam, null) }, label = { Text("Record") })
            NavigationBarItem(currentRoute == PulseCastRoute.LiveStudio, { onNavigate(PulseCastRoute.LiveStudio) }, { Icon(Icons.Default.Settings, null) }, label = { Text("Live") })
            Spacer(Modifier.weight(1f))
            NavigationBarItem(currentRoute == PulseCastRoute.TimelineEditor, { onNavigate(PulseCastRoute.TimelineEditor) }, { Icon(Icons.Default.Edit, null) }, label = { Text("Editor") })
            NavigationBarItem(currentRoute == PulseCastRoute.FloatingSettings, { onNavigate(PulseCastRoute.FloatingSettings) }, { Icon(Icons.Default.Settings, null) }, label = { Text("Tools") })
        }
        FloatingActionButton(
            onClick = { onNavigate(PulseCastRoute.OrbCustomization) },
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp),
            containerColor = ElectricRuby
        ) { Icon(Icons.Default.Settings, "Customize floating orb") }
    }
}

@Composable
private fun CaptureHubDestination(isRecording: Boolean, onRecord: () -> Unit) {
    var preset by remember { mutableStateOf("Pro Gaming") }
    var floatingBall by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StudioPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("CAPTURE ENGINE READY", color = CyberCyan, style = MaterialTheme.typography.labelMedium); Text("Low latency • 112 GB free", color = Color.LightGray) }
                Text("74%", style = MaterialTheme.typography.headlineMedium, color = CyberCyan)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TelemetryChip("2K QHD", CyberCyan); TelemetryChip("60 FPS", Color.White); TelemetryChip("24 Mbps", NeonAmber)
            }
            Spacer(Modifier.height(16.dp))
            Text("DUAL STREAM AUDIO", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
            Spacer(Modifier.height(6.dp))
            AudioMeter("SYSTEM / GAME", .76f, CyberCyan)
            AudioMeter("VOICE MIC", .64f, ElectricRuby)
        }
        StudioPanel(container = SurfaceLow) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Countdown: 3s", color = Color.LightGray); Text("Auto-Stop 60m", color = CyberCyan)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRecord, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby), shape = RoundedCornerShape(14.dp)) {
                Text(if (isRecording) "■ Stop Screen Recording" else "● Start Screen Recording", fontWeight = FontWeight.Bold)
            }
            Text("Overlay badge appears automatically.", Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }
        Text("Target Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Pro Gaming" to "1440p • 60fps", "Tutorial Cast" to "1080p • Brush", "Reaction PIP" to "Dual Cam", "Eco Clip" to "720p • 30fps")) { (name, detail) ->
                Card(onClick = { preset = name }, colors = CardDefaults.cardColors(containerColor = if (preset == name) SurfaceHigh else SurfaceLow), modifier = Modifier.width(148.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(name, fontWeight = FontWeight.Bold); Text(detail, color = CyberCyan, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        StudioPanel {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Floating Ball", fontWeight = FontWeight.Bold); Text("Auto-hide while recording", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
                Switch(floatingBall, { floatingBall = it })
            }
        }
    }
}

@Composable
private fun LiveStudioDestination(
    onOpenFacecam: () -> Unit, onOpenMixer: () -> Unit, onOpenOverlays: () -> Unit,
    onOpenSettings: () -> Unit, onAddIngest: () -> Unit, onGoLive: () -> Unit
) {
    val platforms = remember { mutableStateListOf("YouTube", "Twitch") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Live Multistream Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StudioPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("DESTINATIONS", color = CyberCyan, style = MaterialTheme.typography.labelMedium); Text("2 ARMED", color = SignalGreen, style = MaterialTheme.typography.labelMedium) }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("YouTube", "Twitch", "Kick", "TikTok").forEach { platform ->
                    FilterChip(selected = platform in platforms, onClick = { if (platform in platforms) platforms.remove(platform) else platforms.add(platform) }, label = { Text(platform) })
                }
            }
            TextButton(onClick = onAddIngest) { Text("+ Add Custom RTMP / SRT", color = CyberCyan) }
        }
        StudioPanel(container = SurfaceLow) {
            Text("CAMERA PIP", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(112.dp).background(SurfaceHigh, RoundedCornerShape(12.dp)).clickable(onClick = onOpenFacecam), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Videocam, null, tint = CyberCyan); Text("Tap to configure Facecam + Chroma", color = Color.LightGray) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile("Overlays", onOpenOverlays, Modifier.weight(1f)); ActionTile("Mixer", onOpenMixer, Modifier.weight(1f)); ActionTile("Encoder", onOpenSettings, Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onGoLive, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby), shape = RoundedCornerShape(16.dp)) { Text("GO LIVE", fontWeight = FontWeight.Black) }
        Text("A pre-stream safety check runs before broadcast.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun OrbCustomizationDestination() {
    var idleOpacity by remember { mutableFloatStateOf(.35f) }
    var activeOpacity by remember { mutableFloatStateOf(.92f) }
    var docking by remember { mutableStateOf("Magnetic") }
    var skin by remember { mutableStateOf("Cyber Red") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Floating Ball Customization", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StudioPanel {
            Text("LIVE SIMULATOR", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.CenterEnd) {
                Surface(shape = RoundedCornerShape(99.dp), color = ElectricRuby.copy(alpha = idleOpacity), modifier = Modifier.size(62.dp)) { Box(contentAlignment = Alignment.Center) { Text("◉", color = Color.White, style = MaterialTheme.typography.headlineMedium) } }
            }
            LabeledSlider("Idle opacity", idleOpacity, { idleOpacity = it }); LabeledSlider("Active opacity", activeOpacity, { activeOpacity = it })
        }
        StudioPanel {
            Text("DOCKING MODE", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Magnetic", "Free Float", "Half-Pill").forEach { mode -> FilterChip(mode == docking, { docking = mode }, { Text(mode) }) } }
        }
        StudioPanel {
            Text("GESTURE ACTION BINDER", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            GestureRow("Single Tap", "Radial Menu", CyberCyan); GestureRow("Double Tap", "Instant 30s Clip", ElectricRuby); GestureRow("Long Press (0.8s)", "Mic Mute Toggle", NeonAmber); GestureRow("Swipe Inward", "Live Chat Drawer", CyberCyan)
        }
        StudioPanel {
            Text("ORB VISUAL SKINS", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Cyber Red", "Stealth", "Apex Gold").forEach { option -> FilterChip(option == skin, { skin = option }, { Text(option) }) } }
        }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby)) { Text("✓ Apply Orb Configuration", fontWeight = FontWeight.Bold) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafetyChecklistDestination(onDismiss: () -> Unit, onLaunch: () -> Unit) {
    var dndEnabled by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("Ranked grind to Diamond") }
    val checks = remember { mutableStateListOf(true, true, true, false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Pre-Stream Safety Checklist", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Verify your broadcast before going public.", color = Color.LightGray)
        StudioPanel {
            Text("BROADCAST METADATA", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Stream title") }, singleLine = true)
        }
        StudioPanel {
            listOf("Encoder and network stable", "Mic input detected", "Overlay profile armed", "Do Not Disturb shield enabled").forEachIndexed { index, label ->
                Row(Modifier.fillMaxWidth().clickable { checks[index] = !checks[index] }, verticalAlignment = Alignment.CenterVertically) { Checkbox(checks[index], { checks[index] = it }); Text(label) }
            }
            Button(onClick = { dndEnabled = !dndEnabled }, colors = ButtonDefaults.buttonColors(containerColor = if (dndEnabled) SignalGreen else SurfaceHigh), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text(if (dndEnabled) "DND ARMED" else "Enable DND Shield Now") }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Test Rehearsal (Unlisted)") }
        Button(onClick = onLaunch, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby)) { Text("LAUNCH LIVE MULTISTREAM", fontWeight = FontWeight.Black) }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomIngestDestination(onDismiss: () -> Unit, onConnect: () -> Unit) {
    var transport by remember { mutableStateOf("RTMP(S)") }
    var endpoint by remember { mutableStateOf("rtmps://live.example.com/app") }
    var streamKey by remember { mutableStateOf("") }
    var bitrate by remember { mutableFloatStateOf(8_500f) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Custom RTMP & SRT Ingest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("RTMP(S)", "SRT Caller", "RTSP").forEach { FilterChip(transport == it, { transport = it }, { Text(it) }) } }
        StudioPanel {
            Text("INGEST ENDPOINT", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(endpoint, { endpoint = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Server URL") }, singleLine = true)
            OutlinedTextField(streamKey, { streamKey = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Stream key") }, singleLine = true, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
        }
        StudioPanel {
            Text("Target bitrate ${(bitrate / 1_000).toString().take(3)} Mbps", color = Color.LightGray)
            Slider(bitrate, { bitrate = it }, valueRange = 2_500f..15_000f)
            Text("Uplink utilization: 68% • expected jitter: 0.8 ms", color = CyberCyan, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Test Handshake & Ping") }
        Button(onClick = onConnect, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SignalGreen)) { Text("Connect & Arm Node", fontWeight = FontWeight.Bold) }
    }
    }
}

@Composable
private fun FacecamDestination() {
    var chromaEnabled by remember { mutableStateOf(true) }
    var strength by remember { mutableFloatStateOf(.72f) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Facecam & Chroma Key Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().height(220.dp).background(SurfaceHigh, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("FRONT CAMERA PREVIEW", color = CyberCyan, style = MaterialTheme.typography.labelMedium) }
        StudioPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("Chroma Key", fontWeight = FontWeight.Bold); Text("Remove a colored background", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }; Switch(chromaEnabled, { chromaEnabled = it }) }
            if (chromaEnabled) { Text("Key strength ${(strength * 100).toInt()}%", Modifier.padding(top = 12.dp)); Slider(strength, { strength = it }) }
        }
        StudioPanel { Text("PIP POSITION", color = CyberCyan, style = MaterialTheme.typography.labelMedium); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Top left", "Top right", "Bottom right").forEach { FilterChip(it == "Top right", {}, { Text(it) }) } } }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Apply Facecam Profile") }
    }
}

@Composable
private fun OverlayStudioDestination() {
    var vertical by remember { mutableStateOf(false) }
    val widgets = remember { mutableStateListOf(true, true, false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Stream Alert & Widget Overlay Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(!vertical, { vertical = false }, { Text("16:9") }); FilterChip(vertical, { vertical = true }, { Text("9:16") }) }
        Box(Modifier.fillMaxWidth().height(if (vertical) 260.dp else 160.dp).background(SurfaceHigh, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text("LIVE OVERLAY CANVAS", color = CyberCyan) }
        StudioPanel {
            listOf("Follower alert", "Goal bar", "Chat box").forEachIndexed { index, label -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label); Switch(widgets[index], { widgets[index] = it }) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(true, {}, { Text("Cyber Synth") }); FilterChip(false, {}, { Text("8-Bit") }) }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Trigger Test Alert") }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Publish Overlay to Stream Ingest") }
    }
}

@Composable
private fun AudioMixerDestination() {
    val channels = listOf("Game" to CyberCyan, "Mic" to ElectricRuby, "Discord" to SignalGreen, "Music" to NeonAmber)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Pro Multi-Track Audio Mixer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StudioPanel { Text("MASTER BROADCAST  -4 dB", color = CyberCyan); Slider(.72f, {}) }
        channels.forEach { (name, color) -> StudioPanel { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name, fontWeight = FontWeight.Bold); Text("-8 dB", color = color) }; LinearProgressIndicator(.7f, Modifier.fillMaxWidth().padding(top = 8.dp), color = color, trackColor = SurfaceHigh) } }
        StudioPanel { Text("DMCA SHIELD", color = CyberCyan); Text("Exclude music from VOD audio while preserving live mix.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall); Switch(true, {}) }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Apply Audio Profile") }
    }
}

@Composable
private fun BroadcastSetupDestination() {
    var resolution by remember { mutableStateOf("1440p") }
    var frameRate by remember { mutableStateOf("60 FPS") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Broadcast Setup & RTMP Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StudioPanel { Text("ENCODER PROFILE", color = CyberCyan); Text("H.264 Hardware • CBR • Low latency", Modifier.padding(top = 8.dp), color = Color.LightGray) }
        StudioPanel { Text("Resolution", fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("1080p", "1440p", "4K").forEach { FilterChip(resolution == it, { resolution = it }, { Text(it) }) }; Text("Frame rate", Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold); FilterChip(true, { frameRate = if (frameRate == "60 FPS") "120 FPS" else "60 FPS" }, { Text(frameRate) }) } }
        StudioPanel { Text("NETWORK", color = CyberCyan); Text("Adaptive bitrate enabled • 24 Mbps target", color = Color.LightGray); Slider(.65f, {}) }
        Spacer(Modifier.weight(1f))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Save Broadcast Profile") }
    }
}

@Composable private fun StudioPanel(container: Color = SurfaceLow, content: @Composable ColumnScope.() -> Unit) = Surface(color = container, shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp), content = content) }
@Composable private fun TelemetryChip(text: String, color: Color) = Surface(color = SurfaceHigh, shape = RoundedCornerShape(6.dp)) { Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, style = MaterialTheme.typography.labelSmall) }
@Composable private fun AudioMeter(name: String, value: Float, color: Color) { Column(Modifier.padding(vertical = 4.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name, style = MaterialTheme.typography.labelSmall); Text("-${(value * 10).toInt()} dB", color = color, style = MaterialTheme.typography.labelSmall) }; LinearProgressIndicator(progress = value, modifier = Modifier.fillMaxWidth().height(6.dp), color = color, trackColor = SurfaceHigh) } }
@Composable private fun ActionTile(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) = Card(onClick = onClick, modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceLow)) { Text(label, Modifier.fillMaxWidth().padding(vertical = 18.dp), textAlign = TextAlign.Center, color = CyberCyan, fontWeight = FontWeight.Bold) }
@Composable private fun LabeledSlider(label: String, value: Float, onValueChange: (Float) -> Unit) { Text("$label ${(value * 100).toInt()}%", color = Color.LightGray, style = MaterialTheme.typography.bodySmall); Slider(value, onValueChange) }
@Composable private fun GestureRow(gesture: String, action: String, color: Color) { Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(gesture, fontWeight = FontWeight.Medium); Text(action, color = color, style = MaterialTheme.typography.labelMedium) } }

@Composable
private fun DestinationPlaceholder(title: String, screenId: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().background(SurfaceLow, RoundedCornerShape(20.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(screenId, color = CyberCyan, style = MaterialTheme.typography.labelMedium)
            Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text("Route is ready. The Stitch screen implementation will replace this surface in the next pass.", color = Color.LightGray, textAlign = TextAlign.Center)
        }
    }
}
