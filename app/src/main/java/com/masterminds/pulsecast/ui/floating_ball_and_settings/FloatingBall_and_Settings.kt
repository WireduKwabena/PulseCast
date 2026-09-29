package com.masterminds.pulsecast.ui.floating_ball_and_settings

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    Surface(color = SurfaceLow, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("Floating Ball Overlay HUD", style = PulseCastType.buttonText)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "MID • 48px", 
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
                // Background Pattern
                Box(
                    modifier = Modifier.fillMaxSize().alpha(0.05f).background(SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Grid4x4, null, Modifier.size(80.dp), OnSurface)
                }
                
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
                        Surface(color = SurfaceMid, shape = CircleShape, modifier = Modifier.size(48.dp)) {
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
private fun VideoEncoderSection() {
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

            // Resolution Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Master Resolution", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResolutionChip("2K", "1440p", true, Modifier.weight(1f))
                    ResolutionChip("FHD", "1080p", false, Modifier.weight(1f))
                    ResolutionChip("HD", "720p", false, Modifier.weight(1f))
                }
            }

            // FPS Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Frame Rate Target", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Auto", "30", "60", "90", "120").forEach { fps ->
                        val isSelected = fps == "60"
                        Surface(
                            modifier = Modifier.weight(1f).clickable { },
                            color = if (isSelected) SurfaceHigh else SurfaceLow,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) BorderStroke(1.dp, SecondaryFixedDim) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(fps, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, style = PulseCastType.labelTelemetryMd, color = if (isSelected) SecondaryFixedDim else OnSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionChip(title: String, subtitle: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { },
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
                AudioSourceCard("Mute", Icons.Default.MicOff, false, Modifier.weight(1f))
                AudioSourceCard("Mic Only", Icons.Default.Mic, false, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AudioSourceCard("Internal", Icons.AutoMirrored.Filled.VolumeUp, false, Modifier.weight(1f))
                AudioSourceCard("Internal + Mic", Icons.AutoMirrored.Filled.OpenInNew, true, Modifier.weight(1f))
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AI Neural Noise Cancellation", style = PulseCastType.buttonText)
                    Text("Eliminates fan whine & clicks", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                }
                Switch(checked = true, onCheckedChange = {})
            }
        }
    }
}

@Composable
private fun AudioSourceCard(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { },
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
                Switch(checked = false, onCheckedChange = {})
            }
        }
    }
}

@Composable
private fun StorageDestinationSection() {
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storage, null, Modifier.size(20.dp), SecondaryFixedDim)
                    Text("Storage Destination", style = PulseCastType.headlineSm.copy(fontSize = 14.sp))
                }
                Text("UFS 3.1 • 850 MB/s", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }
            
            Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, SecondaryFixedDim)) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.PhoneAndroid, null, Modifier.size(24.dp), SecondaryFixedDim)
                    Column(Modifier.weight(1f)) {
                        Text("Internal Flash Storage", style = PulseCastType.buttonText)
                        Text("112.4 GB Free (18h 45m)", style = PulseCastType.bodySm, color = SecondaryFixedDim)
                    }
                    Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(20.dp), SecondaryFixedDim)
                }
            }
        }
    }
}

@Composable
private fun ApplyStudioConfigButton() {
    Button(
        onClick = { },
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
