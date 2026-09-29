package com.masterminds.pulsecast.ui.custom_RTMP_SRT_ingest_node_modal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.masterminds.pulsecast.ui.theme.*
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIngestNodeModalScreen(
    viewModel: IngestNodeViewModel = viewModel(),
    onDismiss: () -> Unit = {},
    onConnect: () -> Unit = {}
) {
    val nodes by viewModel.nodes.collectAsState()
    val pingResult by viewModel.pingResult.collectAsState()
    val validationMessage by viewModel.validationMessage.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProtocol by remember { mutableStateOf("RTMP(S)") }
    var selectedPreset by remember { mutableStateOf("Custom Server") }
    var nodeAlias by remember { mutableStateOf("Asia-East Backup Relay") }
    var serverUrl by remember { mutableStateOf("rtmps://live-hkg.edge-streamer.net/app") }
    var streamKey by remember { mutableStateOf("") }
    var secondaryUrl by remember { mutableStateOf("") }
    var showSecondaryUrl by remember { mutableStateOf(false) }

    var selectedProfile by remember { mutableStateOf("1080p60") }
    var bitrateKbps by remember { mutableFloatStateOf(8_500f) }
    var isSubSecondLowLatency by remember { mutableStateOf(true) }
    var isKeyVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLow,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
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
            // Modal Header & Pipeline Tally Status
            ModalHeader(
                onClose = onDismiss
            )

            // 1. Protocol Mode Selector
            TransportProtocolSection(
                selectedProtocol = selectedProtocol,
                onProtocolSelect = { selectedProtocol = it }
            )

            // 2. Target Platform Preset Chips
            RoutingProfilesSection(
                selectedPreset = selectedPreset,
                onPresetSelect = { preset ->
                    selectedPreset = preset
                    when (preset) {
                        "Trovo" -> {
                            nodeAlias = "Trovo Asia Ingest"
                            serverUrl = "rtmp://live-push.trovo.live/live"
                        }
                        "Bilibili Live" -> {
                            nodeAlias = "Bilibili Stream Relay"
                            serverUrl = "rtmp://live-push.bilivideo.com/live-bvc"
                        }
                        "Facebook Gaming" -> {
                            nodeAlias = "FB Gaming Live"
                            serverUrl = "rtmps://live-api-s.facebook.com:443/rtmp"
                        }
                        "Kick Private" -> {
                            nodeAlias = "Kick Live Node"
                            serverUrl = "rtmps://fa723794b6f0.global-contribute.live-video.net"
                        }
                        else -> {
                            nodeAlias = "Asia-East Backup Relay"
                            serverUrl = "rtmps://live-hkg.edge-streamer.net/app"
                        }
                    }
                }
            )

            // 3. Form Fields: Ingest Identifiers
            IngestIdentifiersForm(
                nodeAlias = nodeAlias,
                onAliasChange = { nodeAlias = it },
                serverUrl = serverUrl,
                onUrlChange = { serverUrl = it },
                streamKey = streamKey,
                onKeyChange = { streamKey = it },
                isKeyVisible = isKeyVisible,
                onKeyVisibilityToggle = { isKeyVisible = !isKeyVisible },
                showSecondaryUrl = showSecondaryUrl,
                onSecondaryUrlToggle = { showSecondaryUrl = !showSecondaryUrl },
                secondaryUrl = secondaryUrl,
                onSecondaryUrlChange = { secondaryUrl = it },
                onPasteUrl = {
                    clipboardManager.getText()?.text?.let { serverUrl = it }
                },
                onCopyKey = {
                    if (streamKey.isNotEmpty()) clipboardManager.setText(AnnotatedString(streamKey))
                }
            )

            // 4. Quality & Tuning Parameters
            IngestTuningSection(
                selectedProfile = selectedProfile,
                onProfileSelect = { selectedProfile = it },
                bitrateKbps = bitrateKbps,
                onBitrateChange = { bitrateKbps = it },
                isSubSecondLowLatency = isSubSecondLowLatency,
                onLowLatencyToggle = { isSubSecondLowLatency = it }
            )

            // 5. Handshake Verification & Probe Station
            ProbeStationSection(
                pingResult = pingResult,
                validationMessage = validationMessage,
                onTestHandshake = {
                    val uri = try { URI(serverUrl) } catch (_: Exception) { null }
                    val host = uri?.host
                    if (selectedProtocol != "RTMP(S)") {
                        viewModel.testHandshake("", -1)
                    } else if (!host.isNullOrBlank()) {
                        val defaultPort = if (uri.scheme.equals("rtmps", ignoreCase = true)) 443 else 1935
                        viewModel.testHandshake(host, uri.port.takeIf { it > 0 } ?: defaultPort)
                    } else {
                        viewModel.testHandshake("", -1)
                    }
                }
            )

            // Armed Destination Nodes Count Indicator
            if (nodes.isNotEmpty()) {
                ArmedNodesSummary(nodes = nodes, onRemove = { viewModel.removeNode(it) })
            }

            // 6. Persistent Docked Action Bar
            BottomActionBar(
                onConnectAndArm = {
                    coroutineScope.launch {
                        if (viewModel.addNode(serverUrl, streamKey, selectedProtocol, bitrateKbps.toInt(), nodeAlias.ifBlank { "Ingest Target Node" })) {
                            onConnect()
                        }
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ModalHeader(onClose: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Ambient Standby Pipeline Tally Header
        Surface(
            color = Color(0xFF090E19),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                    Text("STANDBY PIPELINE", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
                Text("MUX READY • 0 DROP", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onClose() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurface, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Text("Add Ingest Target Node", style = PulseCastType.headlineSm, color = OnSurface)
                    Text("Hardware-accelerated broadcast mux", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 11.sp)
                }
            }

            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SRT • RTMP • WebRTC", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TransportProtocolSection(
    selectedProtocol: String,
    onProtocolSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TRANSPORT PROTOCOL", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                Text("TLS 1.3 Guard", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }
        }

        Surface(
            color = Color(0xFF090E19),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("RTMP(S)", "SRT Caller", "RTSP Relay").forEach { proto ->
                    val isSelected = selectedProtocol == proto
                    Surface(
                        color = if (isSelected) PrimaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onProtocolSelect(proto) }
                    ) {
                        Text(
                            text = proto,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = PulseCastType.buttonText,
                            color = if (isSelected) Color.Black else OnSurfaceMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutingProfilesSection(
    selectedPreset: String,
    onPresetSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("ROUTING PROFILES", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val presets = listOf("Custom Server", "Trovo", "Bilibili Live", "Facebook Gaming", "Kick Private", "PeerTube")
            items(presets) { preset ->
                val isSelected = selectedPreset == preset
                Surface(
                    color = if (isSelected) SurfaceHigh else SurfaceLow,
                    shape = RoundedCornerShape(20.dp),
                    border = if (isSelected) BorderStroke(1.dp, PrimaryContainer) else null,
                    modifier = Modifier.clickable { onPresetSelect(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSelected) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(PrimaryContainer))
                        }
                        Text(preset, style = PulseCastType.buttonText, color = if (isSelected) OnSurface else OnSurfaceMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IngestIdentifiersForm(
    nodeAlias: String,
    onAliasChange: (String) -> Unit,
    serverUrl: String,
    onUrlChange: (String) -> Unit,
    streamKey: String,
    onKeyChange: (String) -> Unit,
    isKeyVisible: Boolean,
    onKeyVisibilityToggle: () -> Unit,
    showSecondaryUrl: Boolean,
    onSecondaryUrlToggle: () -> Unit,
    secondaryUrl: String,
    onSecondaryUrlChange: (String) -> Unit,
    onPasteUrl: () -> Unit,
    onCopyKey: () -> Unit
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
            // Node Alias Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("NODE ALIAS / DESCRIPTOR", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                OutlinedTextField(
                    value = nodeAlias,
                    onValueChange = onAliasChange,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF090E19),
                        unfocusedContainerColor = Color(0xFF090E19),
                        focusedBorderColor = SecondaryFixedDim,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Ingest Endpoint Primary
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("INGEST ENDPOINT (PRIMARY)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text("SSL Enabled", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                }

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = onUrlChange,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF090E19),
                        unfocusedContainerColor = Color(0xFF090E19),
                        focusedBorderColor = SecondaryFixedDim,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = onPasteUrl) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stream Key Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TRANSMISSION KEY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text("NEVER EXPOSE ON STREAM", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }

                OutlinedTextField(
                    value = streamKey,
                    onValueChange = onKeyChange,
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF090E19),
                        unfocusedContainerColor = Color(0xFF090E19),
                        focusedBorderColor = SecondaryFixedDim,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onKeyVisibilityToggle) {
                                Icon(if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Key", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onCopyKey) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dual-Redundancy Secondary URL Accordion
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSecondaryUrlToggle() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                        Text("Dual-Redundancy Secondary URL", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                            Text("HOT FAILOVER", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Icon(if (showSecondaryUrl) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                }

                if (showSecondaryUrl) {
                    OutlinedTextField(
                        value = secondaryUrl,
                        onValueChange = onSecondaryUrlChange,
                        placeholder = { Text("failover-tokyo.edge-streamer.net/app", style = PulseCastType.bodySm, color = OnSurfaceMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF090E19),
                            unfocusedContainerColor = Color(0xFF090E19),
                            focusedBorderColor = NeonAmber,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Automatic zero-frame drop rollover upon 250ms socket starvation.", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun IngestTuningSection(
    selectedProfile: String,
    onProfileSelect: (String) -> Unit,
    bitrateKbps: Float,
    onBitrateChange: (Float) -> Unit,
    isSubSecondLowLatency: Boolean,
    onLowLatencyToggle: (Boolean) -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingest Tuning & Pacing", style = PulseCastType.headlineSm, color = OnSurface)
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("AV1 / H.264 DUAL", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                }
            }

            // Frame Profile Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FRAME PROFILE", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("720p60", "1080p60", "1440p60").forEach { profile ->
                        val isSelected = selectedProfile == profile
                        Surface(
                            color = if (isSelected) PrimaryContainer else SurfaceHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onProfileSelect(profile) }
                        ) {
                            Text(
                                text = profile,
                                modifier = Modifier.padding(vertical = 6.dp),
                                style = PulseCastType.labelTelemetryMd,
                                color = if (isSelected) Color.Black else OnSurfaceMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Video Bitrate Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ENCODER BITRATE (CBR)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text("${(bitrateKbps / 1_000).toString().take(3)} Mbps (${bitrateKbps.toInt()} Kbps)", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = bitrateKbps,
                    onValueChange = onBitrateChange,
                    valueRange = 2_500f..14_000f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                    modifier = Modifier.height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(NeonAmber))
                        Text("4G/5G Uplink overhead: 68% utilized", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp)
                    }
                    Text("Target Max: 12 Mbps", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
            }

            // Latency Mode & Keyframe Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("KEYFRAME", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        Text("2.0s Strict GOP", style = PulseCastType.labelTelemetryMd, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SUB-SECOND", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Text("Ultra-Low LL-HLS", style = PulseCastType.labelTelemetryMd, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Checkbox(checked = isSubSecondLowLatency, onCheckedChange = onLowLatencyToggle, modifier = Modifier.scale(0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProbeStationSection(
    pingResult: String,
    validationMessage: String,
    onTestHandshake: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onTestHandshake() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.NetworkPing, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                        Text("Test Handshake & Ping", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                    }
                }

                Text("TLS Socket Ready", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }

            // Probe Metrics Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProbeMetricPill("RTT PING", "18 ms", SecondaryFixedDim, Modifier.weight(1f))
                ProbeMetricPill("JITTER", "0.8 ms", PrimaryContainer, Modifier.weight(1f))
                ProbeMetricPill("INGEST", "LOCKED", NeonAmber, Modifier.weight(1f))
            }

            if (pingResult.isNotEmpty()) {
                Text(pingResult, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }
            if (validationMessage.isNotEmpty()) {
                Text(validationMessage, style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun ProbeMetricPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            Text(value, style = PulseCastType.labelTelemetryMd, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ArmedNodesSummary(nodes: List<IngestNode>, onRemove: (String) -> Unit) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ARMED INGEST DESTINATIONS (${nodes.size})", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
            nodes.forEach { node ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                        Text("${node.label} (${node.protocol})", style = PulseCastType.bodySm, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { onRemove(node.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(onConnectAndArm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onConnectAndArm,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Podcasts, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Connect & Arm Node", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun CustomIngestNodeModalPreview() {
    PulseCastTheme {
        CustomIngestNodeModalScreen()
    }
}
