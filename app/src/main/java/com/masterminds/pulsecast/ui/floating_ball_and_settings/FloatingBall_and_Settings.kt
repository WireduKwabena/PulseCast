package com.masterminds.pulsecast.ui.floating_ball_and_settings

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*

@Composable
fun FloatingBallSettingsScreen(
    onCustomizeOrb: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsHeader()

        FloatingBallOverlayCard(onCustomize = onCustomizeOrb)

        VideoEncoderSection()

        AudioRoutingSection()

        PrivacyBrushSection()

        StorageDestinationSection()

        ApplyStudioConfigButton()

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, null, Modifier.size(20.dp), SecondaryFixedDim)
                Text("Capture Studio Config", style = PulseCastType.headlineSm)
            }
            Text("Hardware encoder calibration & HUD gestures", style = PulseCastType.bodySm, color = OnSurfaceMuted)
        }
        Surface(color = SurfaceHigh, shape = CircleShape) {
            Text(
                text = "ARM64-v8a", 
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                style = PulseCastType.labelTelemetrySm, 
                color = SecondaryFixedDim, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FloatingBallOverlayCard(onCustomize: () -> Unit) {
    var opacity by remember { mutableFloatStateOf(0.85f) }
    var size by remember { mutableStateOf("Medium") }
    var hideDuringRecording by remember { mutableStateOf(false) }
    var shakeSensitivity by remember { mutableStateOf("Normal") }

    var recordShortcut by remember { mutableStateOf(true) }
    var captureShortcut by remember { mutableStateOf(true) }
    var drawShortcut by remember { mutableStateOf(true) }
    var pipShortcut by remember { mutableStateOf(true) }
    var homeShortcut by remember { mutableStateOf(false) }

    Surface(color = SurfaceLow, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Floating Ball Overlay HUD", style = PulseCastType.buttonText)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = if (size == "Small") "MINI • 36px" else if (size == "Large") "PRO • 58px" else "MID • 48px", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                        style = PulseCastType.labelTelemetrySm, 
                        color = SecondaryFixedDim
                    )
                }
            }

            // Interactive Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E19)),
                contentAlignment = Alignment.Center
            ) {
                // Ball Preview
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp)
                        .alpha(opacity)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = SurfaceHigh.copy(alpha = 0.9f), shape = CircleShape) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(16.dp), PrimaryContainer)
                                Icon(Icons.Default.Draw, null, Modifier.size(16.dp), OnSurface)
                                Icon(Icons.Default.Videocam, null, Modifier.size(16.dp), SecondaryFixedDim)
                            }
                        }
                        val orbSizeDp = if (size == "Small") 36.dp else if (size == "Large") 58.dp else 48.dp
                        Surface(color = SurfaceMid, shape = CircleShape, modifier = Modifier.size(orbSizeDp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.BlurOn, null, Modifier.size(24.dp), SecondaryFixedDim)
                            }
                        }
                    }
                }
                
                Text(
                    "Target Canvas (1440p)", 
                    modifier = Modifier.align(Alignment.Center), 
                    style = PulseCastType.labelTelemetrySm, 
                    color = OnSurfaceMuted.copy(alpha = 0.5f)
                )

                Surface(
                    color = SurfaceHigh.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, null, tint = NeonAmber, modifier = Modifier.size(12.dp))
                        Text("Magnetic Snap: Active", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp)
                    }
                }
            }

            // Controls
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ball Scale", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    Row(Modifier.fillMaxWidth().background(SurfaceHigh, RoundedCornerShape(8.dp)).padding(2.dp)) {
                        listOf("Small", "Medium", "Large").forEach { label ->
                            val isSelected = size == label
                            Surface(
                                modifier = Modifier.weight(1f).clickable { size = label },
                                color = if (isSelected) SurfaceMid else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                border = if (isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
                            ) {
                                Text(label, modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetrySm, color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted)
                            }
                        }
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Idle Opacity", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                        Text("${(opacity * 100).toInt()}%", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                    }
                    Slider(
                        value = opacity, 
                        onValueChange = { opacity = it }, 
                        colors = SliderDefaults.colors(activeTrackColor = SecondaryFixedDim),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Radial Dock Shortcut Modules
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Radial Dock Shortcut Modules", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShortcutModuleChip("Record", Icons.Default.RadioButtonChecked, recordShortcut, { recordShortcut = !recordShortcut }, Modifier.weight(1f))
                        ShortcutModuleChip("Capture", Icons.Default.ScreenshotMonitor, captureShortcut, { captureShortcut = !captureShortcut }, Modifier.weight(1f))
                        ShortcutModuleChip("Draw", Icons.Default.Brush, drawShortcut, { drawShortcut = !drawShortcut }, Modifier.weight(1f))
                        ShortcutModuleChip("PIP Cam", Icons.Default.PictureInPicture, pipShortcut, { pipShortcut = !pipShortcut }, Modifier.weight(1f))
                        ShortcutModuleChip("Home", Icons.Default.Home, homeShortcut, { homeShortcut = !homeShortcut }, Modifier.weight(1f))
                    }
                }

                // Hide Ball & Shake to Stop
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hide Ball During Recording", style = PulseCastType.buttonText)
                        Text("Ball completely vanishes when capture starts", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 11.sp)
                    }
                    Switch(checked = hideDuringRecording, onCheckedChange = { hideDuringRecording = it }, modifier = Modifier.scale(0.8f))
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Vibration, null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                            Text("Shake to Stop Recording", style = PulseCastType.buttonText)
                        }
                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                            Text(shakeSensitivity, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().background(SurfaceHigh, RoundedCornerShape(8.dp)).padding(2.dp)) {
                        listOf("Low", "Normal", "High").forEach { level ->
                            val isSelected = shakeSensitivity == level
                            Surface(
                                modifier = Modifier.weight(1f).clickable { shakeSensitivity = level },
                                color = if (isSelected) SurfaceMid else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(level, modifier = Modifier.padding(vertical = 6.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetrySm, color = if (isSelected) NeonAmber else OnSurfaceMuted)
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onCustomize,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Settings, null, Modifier.size(18.dp), CyberCyan)
                        Text("Customize Floating Ball & Gestures", style = PulseCastType.buttonText)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutModuleChip(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = if (active) SurfaceHigh else SurfaceMid.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (active) SecondaryFixedDim else OnSurfaceMuted, modifier = Modifier.size(16.dp))
            Text(label, style = PulseCastType.labelTelemetrySm, color = if (active) OnSurface else OnSurfaceMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun VideoEncoderSection() {
    var selectedRes by remember { mutableStateOf("2K") }
    var selectedFps by remember { mutableStateOf("60") }
    var selectedBitrate by remember { mutableStateOf("24M") }
    var selectedOrientation by remember { mutableStateOf("Landscape") }

    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Movie, null, Modifier.size(20.dp), PrimaryContainer)
                    Text("Video Encoder & Frame Pacing", style = PulseCastType.headlineSm.copy(fontSize = 14.sp))
                }
                Surface(color = PrimaryContainer.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("HEVC / H.265", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }

            // Master Resolution
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Master Resolution", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    Text(if (selectedRes == "2K") "2560 x 1440 QHD" else if (selectedRes == "FHD") "1920 x 1080 FHD" else "1280 x 720 HD", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResolutionChip("2K", "1440p", selectedRes == "2K", { selectedRes = "2K" }, Modifier.weight(1f))
                    ResolutionChip("FHD", "1080p", selectedRes == "FHD", { selectedRes = "FHD" }, Modifier.weight(1f))
                    ResolutionChip("HD", "720p", selectedRes == "HD", { selectedRes = "HD" }, Modifier.weight(1f))
                }
            }

            // Frame Rate Target
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Frame Rate Target", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Auto", "30", "60", "90", "120").forEach { fps ->
                        val isSelected = selectedFps == fps
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedFps = fps },
                            color = if (isSelected) SurfaceHigh else SurfaceLow,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) BorderStroke(1.dp, SecondaryFixedDim) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(fps, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetryMd, color = if (isSelected) SecondaryFixedDim else OnSurface)
                        }
                    }
                }
            }

            // CBR Video Bitrate
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CBR Video Bitrate", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    Text("$selectedBitrate (Ultra High)", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Auto", "8M", "16M", "24M").forEach { rate ->
                        val isSelected = selectedBitrate == rate
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedBitrate = rate },
                            color = if (isSelected) PrimaryContainer else SurfaceLow,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(rate, modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetrySm, color = if (isSelected) Color.Black else OnSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Orientation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Capture Canvas Orientation", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Row(Modifier.fillMaxWidth().background(SurfaceHigh, RoundedCornerShape(8.dp)).padding(2.dp)) {
                    listOf("Auto", "Landscape", "Portrait").forEach { mode ->
                        val isSelected = selectedOrientation == mode
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedOrientation = mode },
                            color = if (isSelected) SurfaceMid else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(mode, modifier = Modifier.padding(vertical = 6.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetrySm, color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionChip(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (selected) SurfaceHigh else SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.dp, SecondaryFixedDim) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = PulseCastType.headlineSm, color = if (selected) SecondaryFixedDim else OnSurface)
            Text(subtitle, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
        }
    }
}

@Composable
private fun AudioRoutingSection() {
    var selectedAudioMode by remember { mutableStateOf(AudioMode.INTERNAL_AND_MIC) }
    var aiNoiseCancellation by remember { mutableStateOf(true) }

    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Mic, null, Modifier.size(20.dp), SecondaryFixedDim)
                    Text("Audio Routing & AI DSP", style = PulseCastType.headlineSm.copy(fontSize = 14.sp))
                }
                Text("48kHz • 16-bit", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AudioSourceCard("Mic Only", Icons.Default.Mic, selectedAudioMode == AudioMode.MIC_ONLY, { selectedAudioMode = AudioMode.MIC_ONLY }, Modifier.weight(1f))
                AudioSourceCard("Internal", Icons.AutoMirrored.Filled.VolumeUp, selectedAudioMode == AudioMode.INTERNAL_ONLY, { selectedAudioMode = AudioMode.INTERNAL_ONLY }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AudioSourceCard("Internal + Mic", Icons.AutoMirrored.Filled.OpenInNew, selectedAudioMode == AudioMode.INTERNAL_AND_MIC, { selectedAudioMode = AudioMode.INTERNAL_AND_MIC }, Modifier.weight(1f))
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AI Neural Noise Cancellation", style = PulseCastType.buttonText)
                    Text("Eliminates fan whine & clicks", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                }
                Switch(checked = aiNoiseCancellation, onCheckedChange = { aiNoiseCancellation = it }, modifier = Modifier.scale(0.8f))
            }
        }
    }
}

@Composable
private fun AudioSourceCard(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (selected) SurfaceHigh else SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.dp, SecondaryFixedDim) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(18.dp), if (selected) SecondaryFixedDim else OnSurfaceMuted)
            Text(label, style = PulseCastType.labelTelemetrySm, color = if (selected) OnSurface else OnSurfaceMuted, maxLines = 1)
        }
    }
}

@Composable
private fun PrivacyBrushSection() {
    var watermarkActive by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Brush, null, Modifier.size(20.dp), NeonAmber)
                Text("Privacy, Brush & Touches", style = PulseCastType.headlineSm.copy(fontSize = 14.sp))
            }
            
            Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Live Screen Brush Styling", style = PulseCastType.buttonText)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ElectricRuby, CyberCyan, NeonAmber).forEach { color ->
                            Box(Modifier.size(20.dp).clip(CircleShape).background(color).border(2.dp, Color.White.copy(alpha = 0.1f), CircleShape))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Studio Privacy Watermark", style = PulseCastType.buttonText)
                    Text("No Watermark • Pro Active", style = PulseCastType.bodySm, color = SecondaryFixedDim)
                }
                Switch(checked = watermarkActive, onCheckedChange = { watermarkActive = it }, modifier = Modifier.scale(0.8f))
            }

            Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.TouchApp, null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Show Tap Pointer Bubbles", style = PulseCastType.buttonText)
                            Text("Developer Options > Show Taps Bridge", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        }
                    }

                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Developer Options Show Taps Bridge Active", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Setup", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = PulseCastType.buttonText, color = SecondaryFixedDim, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageDestinationSection() {
    var isSdCardSelected by remember { mutableStateOf(false) }

    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storage, null, Modifier.size(20.dp), SecondaryFixedDim)
                    Text("Storage Destination", style = PulseCastType.headlineSm.copy(fontSize = 14.sp))
                }
                Text("UFS 3.1 • 850 MB/s", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }
            
            Surface(
                color = if (!isSdCardSelected) SurfaceHigh else SurfaceLow,
                shape = RoundedCornerShape(12.dp),
                border = if (!isSdCardSelected) BorderStroke(1.dp, SecondaryFixedDim) else null,
                modifier = Modifier.clickable { isSdCardSelected = false }
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.PhoneAndroid, null, Modifier.size(24.dp), SecondaryFixedDim)
                    Column(Modifier.weight(1f)) {
                        Text("Internal Flash Storage", style = PulseCastType.buttonText)
                        Text("112.4 GB Free (18h 45m)", style = PulseCastType.bodySm, color = SecondaryFixedDim)
                    }
                    Icon(if (!isSdCardSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, Modifier.size(20.dp), SecondaryFixedDim)
                }
            }

            Surface(
                color = if (isSdCardSelected) SurfaceHigh else SurfaceLow,
                shape = RoundedCornerShape(12.dp),
                border = if (isSdCardSelected) BorderStroke(1.dp, SecondaryFixedDim) else null,
                modifier = Modifier.clickable { isSdCardSelected = true }
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.SdCard, null, Modifier.size(24.dp), OnSurfaceMuted)
                    Column(Modifier.weight(1f)) {
                        Text("MicroSD Card (SanDisk Extreme)", style = PulseCastType.buttonText, color = OnSurfaceMuted)
                        Text("482.0 GB Free • V30 Speed Class", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    }
                    Icon(if (isSdCardSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, Modifier.size(20.dp), OnSurfaceMuted)
                }
            }

            LinearProgressIndicator(
                progress = { 0.38f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = SecondaryFixedDim,
                trackColor = SurfaceHigh
            )
        }
    }
}

@Composable
private fun ApplyStudioConfigButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            Toast.makeText(context, "Studio Configuration Applied", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Save, null, Modifier.size(20.dp))
            Text("APPLY STUDIO CONFIGURATION", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun FloatingBallSettingsPreview() {
    PulseCastTheme {
        FloatingBallSettingsScreen()
    }
}
