package com.masterminds.pulsecast.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.masterminds.pulsecast.ui.CaptureViewModel
import com.masterminds.pulsecast.ui.broadcast_setup_RTMP_studio.BroadcastSetupRTMPStudioScreen
import com.masterminds.pulsecast.ui.capture_hub.CaptureHubScreen
import com.masterminds.pulsecast.ui.creator_profile_connected_channels_hub.CreatorProfile_and_ConnectedChannels_HubScreen
import com.masterminds.pulsecast.ui.custom_RTMP_SRT_ingest_node_modal.CustomIngestNodeModalScreen
import com.masterminds.pulsecast.ui.custom_RTMP_SRT_ingest_node_modal.IngestNodeViewModel
import com.masterminds.pulsecast.ui.facecam_chroma_key_studio.FacecamChromaKeyStudioScreen
import com.masterminds.pulsecast.ui.floating_ball_and_settings.FloatingBallSettingsScreen
import com.masterminds.pulsecast.ui.floating_ball_customization_gesture_binder.FloatingBallCustomizationScreen
import com.masterminds.pulsecast.ui.instant_clip_highlight_export_flow.InstantClipHighlightExportFlowScreen
import com.masterminds.pulsecast.ui.live_in_game_hud_overlay.LiveInGameHUDOverlayScreen
import com.masterminds.pulsecast.ui.live_multistream_studio.LiveMultiStreamStudioScreen
import com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer.LiveChatDrawer
import com.masterminds.pulsecast.ui.performance_stream_diagnostics_analytics.DiagnosticsViewModel
import com.masterminds.pulsecast.ui.performance_stream_diagnostics_analytics.PerformanceStreamDiagnosticAnalyticsScreen
import com.masterminds.pulsecast.ui.pre_stream_go_live_safety_checklist.PreFlightChecklistScreen
import com.masterminds.pulsecast.ui.pro_multi_track_audio_mixer_DMCA_shield.AudioMixerViewModel
import com.masterminds.pulsecast.ui.pro_multi_track_audio_mixer_DMCA_shield.ProMultiTrackAudioMixerScreen
import com.masterminds.pulsecast.ui.stream_alert_widget_overlay_studio.StreamAlertWidgetOverlayStudioScreen
import com.masterminds.pulsecast.ui.studio_vault_media_library.StudioVaultScreen
import com.masterminds.pulsecast.ui.timeline_video_editor.TimelineVideoEditorScreen
import com.masterminds.pulsecast.ui.theme.BgBase
import com.masterminds.pulsecast.ui.theme.CyberCyan
import com.masterminds.pulsecast.ui.theme.ElectricRuby
import com.masterminds.pulsecast.ui.theme.NeonAmber
import com.masterminds.pulsecast.ui.theme.SignalGreen
import com.masterminds.pulsecast.ui.theme.SurfaceHigh
import com.masterminds.pulsecast.ui.theme.SurfaceLow
import com.masterminds.pulsecast.ui.ui_library.BottomNavRoute
import com.masterminds.pulsecast.ui.ui_library.PulseCastAppBar
import com.masterminds.pulsecast.ui.ui_library.PulseCastBottomNavigation
import kotlinx.coroutines.launch
import java.net.URI

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

    @Composable
    fun getPrimaryRoutes() = listOf(
        BottomNavRoute(CaptureHub, "Record", Icons.Default.Videocam),
        BottomNavRoute(LiveStudio, "Live", Icons.Default.Sensors),
        BottomNavRoute(TimelineEditor, "Editor", Icons.Default.Movie),
        BottomNavRoute(FloatingSettings, "Tools", Icons.Default.Tune)
    )
}

@Composable
fun PulseCastNavigation(
    isRecording: Boolean,
    isBroadcasting: Boolean,
    onRecord: () -> Unit,
    onStartBroadcast: () -> Boolean,
    onStopCapture: () -> Unit,
    captureViewModel: CaptureViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    val isIndependentScreen = route == PulseCastRoute.LiveHud

    LaunchedEffect(isBroadcasting) {
        if (isBroadcasting && navController.currentDestination?.route != PulseCastRoute.LiveHud) {
            navController.navigate(PulseCastRoute.LiveHud) { launchSingleTop = true }
        } else if (!isBroadcasting && navController.currentDestination?.route == PulseCastRoute.LiveHud) {
            navController.popBackStack()
        }
    }
    
    var showOrbCustomization by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = BgBase,
        topBar = {
            if (!isIndependentScreen) {
                PulseCastAppBar(
                    onHome = { navController.navigateRoot(PulseCastRoute.CaptureHub) },
                    onVault = { navController.navigate(PulseCastRoute.Vault) },
                    onDiagnostics = { navController.navigate(PulseCastRoute.Diagnostics) },
                    onProfile = { navController.navigate(PulseCastRoute.CreatorHub) }
                )
            }
        },
        bottomBar = {
            if (!isIndependentScreen && route in PulseCastRoute.primary) {
                PulseCastBottomNavigation(
                    currentRoute = route,
                    onNavigate = { navController.navigateRoot(it) },
                    onQuickOrb = { showOrbCustomization = true },
                    routes = PulseCastRoute.getPrimaryRoutes()
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = PulseCastRoute.CaptureHub,
                modifier = if (isIndependentScreen) Modifier.fillMaxSize() else Modifier.padding(padding)
            ) {
                composable(PulseCastRoute.CaptureHub) {
                    CaptureHubDestination(
                        isRecording = isRecording,
                        onRecord = onRecord,
                        viewModel = captureViewModel,
                        onNavigateToVault = { navController.navigate(PulseCastRoute.Vault) },
                        onNavigateToPresets = { navController.navigate(PulseCastRoute.BroadcastSetup) }
                    )
                }
                composable(PulseCastRoute.LiveStudio) {
                    LiveStudioDestination(
                        isBroadcasting = isBroadcasting,
                        onOpenFacecam = { navController.navigate(PulseCastRoute.FacecamChroma) },
                        onOpenMixer = { navController.navigate(PulseCastRoute.AudioMixer) },
                        onOpenOverlays = { navController.navigate(PulseCastRoute.OverlayStudio) },
                        onOpenSettings = { navController.navigate(PulseCastRoute.BroadcastSetup) },
                        onAddIngest = { navController.navigate(PulseCastRoute.CustomIngest) },
                        onGoLive = { onStartBroadcast() },
                        onStopBroadcast = onStopCapture
                    )
                }
                composable(PulseCastRoute.TimelineEditor) { 
                    TimelineVideoEditorScreen(
                        onBack = { navController.popBackStack() },
                        onExport = { navController.navigate(PulseCastRoute.ClipExport) }
                    )
                }
                composable(PulseCastRoute.FloatingSettings) { 
                    FloatingBallSettingsScreen(
                        onCustomizeOrb = { showOrbCustomization = true }
                    )
                }
                composable(PulseCastRoute.Vault) { 
                    StudioVaultScreen(onBack = { navController.popBackStack() })
                }
                composable(PulseCastRoute.Diagnostics) { 
                    PerformanceStreamDiagnosticAnalyticsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(PulseCastRoute.CreatorHub) { 
                    CreatorProfile_and_ConnectedChannels_HubScreen(
                        onBack = { navController.popBackStack() },
                        onAddIngest = { navController.navigate(PulseCastRoute.CustomIngest) },
                        onCustomizeOrb = { showOrbCustomization = true }
                    )
                }
                composable(PulseCastRoute.AudioMixer) { 
                    ProMultiTrackAudioMixerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(PulseCastRoute.OverlayStudio) { 
                    StreamAlertWidgetOverlayStudioScreen(
                        onBack = { navController.popBackStack() },
                        onPublish = { navController.popBackStack() }
                    )
                }
                composable(PulseCastRoute.BroadcastSetup) { 
                    BroadcastSetupRTMPStudioScreen(
                        onBack = { navController.popBackStack() },
                        onSaveAndLaunch = { navController.navigate(PulseCastRoute.LiveHud) }
                    )
                }
                composable(PulseCastRoute.FacecamChroma) { 
                    FacecamChromaKeyStudioScreen(
                        onBack = { navController.popBackStack() },
                        onApply = { navController.popBackStack() }
                    )
                }
                composable(PulseCastRoute.LiveHud) {
                    LiveInGameHUDOverlayScreen(
                        onStopRecording = {
                            onStopCapture()
                            navController.popBackStack()
                        }
                    )
                }
                composable(PulseCastRoute.ClipExport) { 
                    InstantClipHighlightExportFlowScreen(
                        onBack = { navController.popBackStack() },
                        onSaveToVault = { navController.popBackStack() }
                    )
                }
                composable(PulseCastRoute.CustomIngest) { 
                    CustomIngestNodeModalScreen(
                        onDismiss = { navController.popBackStack() },
                        onConnect = { navController.popBackStack() }
                    )
                }
            }

            if (showOrbCustomization) {
                FloatingBallCustomizationScreen(
                    onClose = { showOrbCustomization = false }
                )
            }
        }
    }
}

private fun NavHostController.navigateRoot(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
private fun CaptureHubDestination(
    isRecording: Boolean,
    onRecord: () -> Unit,
    viewModel: CaptureViewModel,
    onNavigateToVault: () -> Unit,
    onNavigateToPresets: () -> Unit
) {
    val storagePercentage by viewModel.storagePercentage.collectAsState()
    val freeSpaceText by viewModel.freeSpaceText.collectAsState()
    val systemAudioLevel by viewModel.systemAudioLevel.collectAsState()
    val micAudioLevel by viewModel.micAudioLevel.collectAsState()
    val resolution by viewModel.resolution.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val audioMode by viewModel.audioMode.collectAsState()

    CaptureHubScreen(
        isRecording = isRecording,
        onRecordClick = onRecord,
        storagePercentage = storagePercentage,
        freeSpaceText = freeSpaceText,
        systemAudioLevel = systemAudioLevel,
        micAudioLevel = micAudioLevel,
        resolution = resolution,
        fps = fps,
        audioMode = audioMode,
        onResolutionChange = viewModel::setResolution,
        onFpsChange = viewModel::setFps,
        onAudioModeChange = viewModel::setAudioMode,
        onNavigateToVault = onNavigateToVault,
        onNavigateToPresets = onNavigateToPresets
    )
}

@Composable
private fun LiveStudioDestination(
    isBroadcasting: Boolean,
    onOpenFacecam: () -> Unit, onOpenMixer: () -> Unit, onOpenOverlays: () -> Unit,
    onOpenSettings: () -> Unit, onAddIngest: () -> Unit, onGoLive: () -> Unit,
    onStopBroadcast: () -> Unit
) {
    LiveMultiStreamStudioScreen(
        isBroadcasting = isBroadcasting,
        onGoLive = onGoLive,
        onStopBroadcast = onStopBroadcast,
        onAddCustomRTMP = onAddIngest,
        onOpenFacecam = onOpenFacecam,
        onOpenMixer = onOpenMixer,
        onOpenOverlays = onOpenOverlays,
        onOpenSettings = onOpenSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomIngestDestination(onDismiss: () -> Unit, onConnect: () -> Unit) {
    val viewModel: IngestNodeViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val nodes by viewModel.nodes.collectAsState()
    val pingResult by viewModel.pingResult.collectAsState()
    val validationMessage by viewModel.validationMessage.collectAsState()
    var transport by remember { mutableStateOf("RTMP(S)") }
    var endpoint by remember { mutableStateOf("rtmps://live.example.com/app") }
    var streamKey by remember { mutableStateOf("") }
    var bitrate by remember { mutableFloatStateOf(8_500f) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Custom RTMP & SRT Ingest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (nodes.isNotEmpty()) {
                Text("Armed Destinations", color = CyberCyan)
                nodes.forEach { node -> Text("• ${node.label} (${node.protocol})") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("RTMP(S)", "SRT Caller", "RTSP").forEach { FilterChip(transport == it, { transport = it }, { Text(it) }) } }
            StudioPanel {
                Text("INGEST ENDPOINT", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(endpoint, { endpoint = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Server URL") }, singleLine = true)
                OutlinedTextField(streamKey, { streamKey = it }, Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Stream key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            }
            StudioPanel {
                Text("Target bitrate ${(bitrate / 1_000).toString().take(3)} Mbps", color = Color.LightGray)
                Slider(bitrate, { bitrate = it }, valueRange = 2_500f..15_000f)
                Text("Uplink utilization: 68% • expected jitter: 0.8 ms", color = CyberCyan, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            if (pingResult.isNotEmpty()) Text(pingResult, color = CyberCyan)
            if (validationMessage.isNotEmpty()) Text(validationMessage, color = ElectricRuby, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                val uri = try { URI(endpoint) } catch (e: Exception) { null }
                val host = uri?.host
                if (transport != "RTMP(S)") {
                    viewModel.testHandshake("", -1)
                } else if (!host.isNullOrBlank()) {
                    val defaultPort = if (uri.scheme.equals("rtmps", ignoreCase = true)) 443 else 1935
                    viewModel.testHandshake(host, uri.port.takeIf { it > 0 } ?: defaultPort)
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Test Handshake & Ping") }
            Button(onClick = {
                coroutineScope.launch {
                    if (viewModel.addNode(endpoint, streamKey, transport, bitrate.toInt(), "Node ${nodes.size + 1}")) onConnect()
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SignalGreen)) { Text("Connect & Arm Node", fontWeight = FontWeight.Bold) }
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
    val viewModel: AudioMixerViewModel = viewModel()
    val channels by viewModel.channels.collectAsState()
    val masterGain by viewModel.masterGainDb.collectAsState()
    val dmcaEnabled by viewModel.dmcaShieldEnabled.collectAsState()
    val duckingEnabled by viewModel.smartDuckingEnabled.collectAsState()
    val duckingThreshold by viewModel.smartDuckingThreshold.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.profileApplied.collect {
            snackbarHostState.showSnackbar("Audio Profile Applied")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Pro Multi-Track Audio Mixer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StudioPanel { 
                Text("MASTER BROADCAST ${masterGain} dB", color = CyberCyan)
                Slider(masterGain / 20f + 0.5f, { viewModel.setMasterGain((it - 0.5f) * 20f) })
            }
            channels.forEachIndexed { index, channel -> 
                StudioPanel { 
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                        Text(channel.name, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${channel.gainDb} dB", color = Color(channel.color))
                            IconButton(onClick = { viewModel.toggleChannelMute(index) }) {
                                Icon(if (channel.muted) Icons.Default.MicOff else Icons.Default.Mic, "Mute")
                            }
                        }
                    }
                    Slider(channel.gainDb / 20f + 0.5f, { viewModel.setChannelGain(index, (it - 0.5f) * 20f) }) 
                } 
            }
            StudioPanel { 
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("DMCA SHIELD", color = CyberCyan)
                        Text("Exclude music from VOD.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(dmcaEnabled, { viewModel.toggleDmcaShield() })
                }
            }
            StudioPanel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("SMART DUCKING", color = SignalGreen)
                        Text("Auto-lower volume on mic input.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(duckingEnabled, { viewModel.toggleSmartDucking() })
                }
                if (duckingEnabled) {
                    Slider(duckingThreshold, { viewModel.setDuckingThreshold(it) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Airhorn", "Clutch Clap", "GG Wave", "Crowd Cheer", "Air Horn 2", "Subscribe").forEach { sfx ->
                    Box(Modifier.size(48.dp).background(SurfaceHigh, RoundedCornerShape(8.dp)).clickable { }, contentAlignment = Alignment.Center) {
                        Text(sfx.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(onClick = { viewModel.applyProfile() }, modifier = Modifier.fillMaxWidth()) { Text("Apply Audio Profile") }
        }
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

@Composable
private fun DiagnosticsScreen() {
    val viewModel: DiagnosticsViewModel = viewModel()
    val fps by viewModel.fps.collectAsState()
    val bitrate by viewModel.bitrateMbps.collectAsState()
    val temp by viewModel.socTempC.collectAsState()
    val battery by viewModel.batteryPct.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()
    val bitrateHistory by viewModel.bitrateHistory.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.startMonitoring()
        viewModel.optimizeEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Performance & Stream Diagnostics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Live", "1m", "5m", "15m").forEach { FilterChip(it == "Live", {}, { Text(it) }) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StudioPanel { Text("FPS", color = Color.Gray); Text("${fps.toInt()}", style = MaterialTheme.typography.headlineMedium, color = CyberCyan) }
                StudioPanel { Text("Uplink", color = Color.Gray); Text(String.format("%.1f", bitrate), style = MaterialTheme.typography.headlineMedium, color = ElectricRuby) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StudioPanel { Text("Temp", color = Color.Gray); Text("${temp.toInt()}°C", style = MaterialTheme.typography.headlineMedium, color = NeonAmber) }
                StudioPanel { Text("Battery", color = Color.Gray); Text("$battery%", style = MaterialTheme.typography.headlineMedium, color = SignalGreen) }
            }
            Box(Modifier.fillMaxWidth().height(200.dp).background(SurfaceHigh, RoundedCornerShape(16.dp)).padding(16.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    if (fpsHistory.isEmpty()) return@Canvas
                    val maxFps = 120f
                    val maxBitrate = 15f
                    val stepX = size.width / 60f
                    val fpsPath = Path()
                    val bitratePath = Path()
                    fpsHistory.forEachIndexed { index, v ->
                        val x = index * stepX
                        val y = size.height - (v / maxFps * size.height)
                        if (index == 0) fpsPath.moveTo(x, y) else fpsPath.lineTo(x, y)
                    }
                    bitrateHistory.forEachIndexed { index, v ->
                        val x = index * stepX
                        val y = size.height - (v / maxBitrate * size.height)
                        if (index == 0) bitratePath.moveTo(x, y) else bitratePath.lineTo(x, y)
                    }
                    drawPath(fpsPath, Color.Cyan, style = Stroke(3f))
                    drawPath(bitratePath, Color.Red, style = Stroke(3f))
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { viewModel.autoOptimize() }, modifier = Modifier.fillMaxWidth()) { Text("Auto-Optimize Encoder") }
        }
    }
}

@Composable
private fun CreatorHubScreen() {
    var autoSync by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(true) }
    var showLogout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Creator Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(64.dp).background(Color.Gray, RoundedCornerShape(32.dp)))
            Column { Text("Streamer Avatar Placeholder", fontWeight = FontWeight.Bold); Text("Live on 3 platforms", color = CyberCyan) }
        }
        Surface(color = SurfaceLow, shape = RoundedCornerShape(16.dp)) {
            Text("Pro Studio Active", Modifier.padding(16.dp), color = SignalGreen, fontWeight = FontWeight.Bold)
        }
        listOf("YouTube" to "1.2K", "Twitch" to "8.5K", "TikTok" to "45K").forEach { (platform, followers) ->
            StudioPanel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text(platform, fontWeight = FontWeight.Bold); Text("$followers followers", color = Color.LightGray) }
                    Button(onClick = {}) { Text("Manage") }
                }
            }
        }
        StudioPanel {
            Text("Cloud Vault Storage", fontWeight = FontWeight.Bold)
            LinearProgressIndicator(0.75f, Modifier.fillMaxWidth().padding(vertical = 8.dp), color = CyberCyan)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Auto-sync", color = Color.LightGray)
                Checkbox(autoSync, { autoSync = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Wi-Fi only", color = Color.LightGray)
                Checkbox(wifiOnly, { wifiOnly = it })
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { showLogout = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby)) { Text("Disconnect / Log Out") }
    }
    if (showLogout) {
        AlertDialog(onDismissRequest = { showLogout = false }, confirmButton = { Button(onClick = { showLogout = false }) { Text("Log Out") } }, dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancel") } }, title = { Text("Log Out?") }, text = { Text("Are you sure you want to disconnect your accounts?") })
    }
}

@Composable
private fun ClipExportScreen() {
    var ratio by remember { mutableStateOf("9:16") }
    var start by remember { mutableFloatStateOf(0.1f) }
    var end by remember { mutableFloatStateOf(0.9f) }
    var rendering by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Instant Clip Export", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("9:16", "16:9", "1:1").forEach { FilterChip(ratio == it, { ratio = it }, { Text(it) }) }
        }
        Box(Modifier.fillMaxWidth().height(300.dp).background(SurfaceHigh, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("Preview Placeholder", color = CyberCyan)
        }
        StudioPanel {
            Text("Trim Timeline", color = Color.LightGray)
            Row {
                Slider(start, { if (it < end) start = it }, Modifier.weight(1f))
                Slider(end, { if (it > start) end = it }, Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TikTok", "YT Shorts", "Reels", "Discord").forEach { FilterChip(false, {}, { Text(it) }) }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { rendering = true }, modifier = Modifier.fillMaxWidth()) { Text("Render & Export $ratio Clip") }
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Save to PulseCast Vault") }
    }
    if (rendering) {
        AlertDialog(onDismissRequest = {}, confirmButton = {}, title = { Text("Rendering...") }, text = { CircularProgressIndicator() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineEditorScreen() {
    var playhead by remember { mutableFloatStateOf(0.5f) }
    var audioVol by remember { mutableFloatStateOf(0.8f) }
    var showExport by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Timeline Editor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().height(200.dp).background(SurfaceHigh, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("Video Preview Placeholder", color = CyberCyan)
        }
        Slider(playhead, { playhead = it })
        StudioPanel {
            Text("Video Track", color = Color.LightGray)
            Box(Modifier.fillMaxWidth().height(40.dp).background(Color.Blue.copy(alpha=0.3f)))
            Spacer(Modifier.height(8.dp))
            Text("Audio Track", color = Color.LightGray)
            Box(Modifier.fillMaxWidth().height(40.dp).background(Color.Green.copy(alpha=0.3f)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Icon(Icons.Default.ContentCut, null); Text("Split") }
            Button(onClick = {}) { Icon(Icons.Default.Speed, null); Text("Speed") }
        }
        StudioPanel {
            Text("Audio Fader", color = Color.LightGray)
            Slider(audioVol, { audioVol = it })
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { showExport = true }, modifier = Modifier.fillMaxWidth()) { Text("Export") }
    }
    if (showExport) {
        ModalBottomSheet(onDismissRequest = { showExport = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Export Resolution")
                Button(onClick = { showExport = false }) { Text("1080p") }
                Button(onClick = { showExport = false }) { Text("4K") }
            }
        }
    }
}

@Composable private fun StudioPanel(modifier: Modifier = Modifier, container: Color = SurfaceLow, content: @Composable ColumnScope.() -> Unit) = Surface(modifier = modifier, color = container, shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp), content = content) }
