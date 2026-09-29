package com.masterminds.pulsecast.ui.capture_hub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.masterminds.pulsecast.ui.CaptureViewModel
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*
import kotlinx.coroutines.delay

@Composable
fun CaptureHubScreen(
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
    storagePercentage: Float = 0.74f,
    freeSpaceText: String = "112 GB Free • 18.4 hrs remaining",
    systemAudioLevel: Float = 0.72f,
    micAudioLevel: Float = 0.60f,
    resolution: String = "1080p",
    fps: Int = 60,
    audioMode: AudioMode = AudioMode.MIC_ONLY,
    onResolutionChange: (String) -> Unit = {},
    onFpsChange: (Int) -> Unit = {},
    onAudioModeChange: (AudioMode) -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToPresets: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TelemetryReadinessCard(
            storagePercentage = storagePercentage,
            freeSpaceText = freeSpaceText,
            systemAudioLevel = systemAudioLevel,
            micAudioLevel = micAudioLevel,
            resolution = resolution,
            fps = fps,
            audioMode = audioMode,
            onResolutionChange = onResolutionChange,
            onFpsChange = onFpsChange,
            onAudioModeChange = onAudioModeChange
        )
        
        RecordingTriggerCore(
            isRecording = isRecording,
            onClick = onRecordClick
        )
        
        TargetPresetsSection(onCustomize = onNavigateToPresets)
        
        SecondarySetupGrid()
        
        FloatingBallSimulator()
        
        StudioVaultPreview(onViewAll = onNavigateToVault)
        
        Spacer(Modifier.height(80.dp)) // Padding for bottom nav
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TelemetryReadinessCard(
    storagePercentage: Float,
    freeSpaceText: String,
    systemAudioLevel: Float,
    micAudioLevel: Float,
    resolution: String,
    fps: Int,
    audioMode: AudioMode,
    onResolutionChange: (String) -> Unit,
    onFpsChange: (Int) -> Unit,
    onAudioModeChange: (AudioMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLow)
            .border(1.dp, StrokeSubtle, RoundedCornerShape(12.dp))
    ) {
        // Background Decorative Blurs (Soft blurs per HTML)
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(PrimaryContainer.copy(alpha = 0.12f), CircleShape)
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .background(SecondaryContainer.copy(alpha = 0.08f), CircleShape)
                .blur(32.dp)
        )

        Column(modifier = Modifier.padding(14.dp)) {
            // 1. Header: Ready Status + Low Latency Badge
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = Int.MAX_VALUE
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    PulseStatusDot(color = PrimaryContainer)
                    Text(
                        text = "CAPTURE ENGINE READY",
                        style = PulseCastType.labelTelemetryMd,
                        color = OnSurface,
                        letterSpacing = 0.5.sp,
                        fontSize = 12.sp
                    )
                }
                // Glassmorphic Low Latency Pill
                Surface(
                    color = Color(0xFF252A36).copy(alpha = 0.85f), // surface-container-high
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bolt, null, Modifier.size(13.dp), CyberCyan)
                        Text(
                            text = "LOW LATENCY",
                            style = PulseCastType.labelTelemetrySm,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. Main Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PulseTelemetryRing(
                    percentage = storagePercentage,
                    label = "STORAGE",
                    size = 64.dp,
                    strokeWidth = 5.dp,
                    color = SecondaryFixedDim
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Spec Matrix - using FlowRow for dynamic wrapping
                    // To ensure "24 Mbps CBR" is on the next line as requested
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = 2 // Forces the 3rd item to wrap
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = { onResolutionChange(if (resolution == "1440p") "1080p" else "1440p") },
                            label = { Text(resolution, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan.copy(alpha = 0.18f), selectedLabelColor = CyberCyan)
                        )
                        FilterChip(
                            selected = true,
                            onClick = { onFpsChange(if (fps == 60) 30 else 60) },
                            label = { Text("$fps FPS", fontSize = 10.sp) }
                        )
                        Text("Profile", color = OnSurfaceMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Storage, null, Modifier.size(14.dp), CyberCyan)
                        Text(
                            text = buildAnnotatedString {
                                val parts = freeSpaceText.split("•")
                                append(parts.getOrElse(0) { "" } + "• ")
                                withStyle(SpanStyle(color = OnSurface, fontWeight = FontWeight.Bold)) {
                                    append(parts.getOrElse(1) { "" }.trim())
                                }
                            },
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 10.sp),
                            color = OnSurfaceMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3. Audio VU Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B202B).copy(alpha = 0.85f), RoundedCornerShape(10.dp)) // surface-container
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with extremely tight grouping to fit on one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group 1: Dual Stream Audio
                    Surface(
                        color = Color(0xFF252A36).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.HeadsetMic, null, Modifier.size(14.dp), CyberCyan)
                            Text(
                                text = "Dual Stream\nAudio",
                                style = PulseCastType.bodyMd.copy(fontSize = 10.sp, lineHeight = 11.sp),
                                color = OnSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }

                    // Group 2: 48kHz • STEREO
                    Surface(
                        color = Color(0xFF252A36).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                            "44.1kHz •\nMONO AAC",
                                style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.5.sp, lineHeight = 10.sp),
                                color = OnSurfaceMuted,
                                maxLines = 2
                            )
                        }
                    }
                    
                    // Group 3: Internal + Mic Toggle
                    Surface(
                        color = SecondaryContainer.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.weight(1.1f)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)) {
                                Icon(Icons.Default.ToggleOn, null, Modifier.size(14.dp), CyberCyan)
                                Spacer(Modifier.width(4.dp))
                                Text(audioModeLabel(audioMode), fontSize = 9.sp, lineHeight = 10.sp, color = CyberCyan, maxLines = 2)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                AudioMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(audioModeLabel(mode)) },
                                        onClick = { onAudioModeChange(mode); expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PulseVUIndicator("SYSTEM/GAME", systemAudioLevel, "-8 dB", color = CyberCyan, modifier = Modifier.weight(1f))
                    PulseVUIndicator("VOICE MIC", micAudioLevel, "-3 dB", color = CyberCyan, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun audioModeLabel(mode: AudioMode): String = when (mode) {
    AudioMode.MIC_ONLY -> "Mic only"
    AudioMode.INTERNAL_ONLY -> "Internal audio"
    AudioMode.INTERNAL_AND_MIC -> "Internal + mic"
}

@Composable
private fun RecordingTriggerCore(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(PrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .blur(32.dp)
        )
        
        PulseCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceHigh,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, Modifier.size(16.dp), NeonAmber)
                        Spacer(Modifier.width(6.dp))
                        Text("Countdown: 3s", style = PulseCastType.labelTelemetrySm)
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceMid)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tune, null, Modifier.size(14.dp), CyberCyan)
                        Spacer(Modifier.width(4.dp))
                        Text("Auto-Stop 60m", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
                    }
                }
                
                PulseRecordButton(onClick = onClick, isRecording = isRecording)
                
                Text(
                    "Tap to capture high bitrate session. Overlay badge appears automatically.",
                    style = PulseCastType.bodySm,
                    color = OnSurfaceMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TargetPresetsSection(onCustomize: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target Presets", style = PulseCastType.headlineSm)
            TextButton(onClick = onCustomize) {
                Text("CUSTOMIZE", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
            }
        }
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                PresetCard("Pro Gaming", "1440p • 60fps • 32M", Icons.Default.SportsEsports, PrimaryContainer, true)
            }
            item {
                PresetCard("Tutorial Cast", "1080p • Brush • Mic", Icons.Default.Draw, CyberCyan, false)
            }
            item {
                PresetCard("Reaction PIP", "Dual Cam • Chroma", Icons.Default.VideoCameraFront, NeonAmber, false)
            }
            item {
                PresetCard("Eco Clip", "720p • 30fps • Auto", Icons.Default.BatterySaver, OnSurfaceMuted, false)
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean
) {
    PulseCard(
        modifier = Modifier.width(144.dp),
        backgroundColor = if (isActive) SurfaceHigh else SurfaceLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(18.dp), color)
                }
                if (isActive) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                }
            }
            Column {
                Text(title, style = PulseCastType.buttonText, color = OnSurface)
                Text(desc, style = PulseCastType.labelTelemetrySm.copy(fontSize = 10.sp), color = OnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun SecondarySetupGrid() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp), 
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        SecondaryToggleCard("Region", "Entire Screen", "FULL", Icons.Default.CropFree, CyberCyan, Modifier.weight(1f).fillMaxHeight())
        SecondaryToggleCard("Facecam PIP", "Circle • Front", "OFF", Icons.Default.AccountBox, NeonAmber, Modifier.weight(1f).fillMaxHeight())
        SecondaryToggleCard("Float Ball", "Auto Hide", "ON", Icons.Default.BubbleChart, PrimaryContainer, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun SecondaryToggleCard(
    title: String,
    detail: String,
    badge: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    PulseCard(
        modifier = modifier,
        backgroundColor = SurfaceLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, Modifier.size(20.dp), color)
                Text(badge, style = PulseCastType.labelTelemetrySm, color = color, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(title, style = PulseCastType.buttonText, color = OnSurface)
                Text(detail, style = PulseCastType.bodySm.copy(fontSize = 11.sp), color = OnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun FloatingBallSimulator() {
    PulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Main Orb Box - Removed clip to prevent dot clipping
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF303541), CircleShape) // surface-container-highest
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(16.dp), OnPrimary)
                    }
                    // Status dot - moved slightly further to ensure it's not hidden
                    Box(
                        Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF303541))
                            .padding(2.dp)
                    ) {
                        Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF9cf0ff))) // secondary-fixed
                    }
                }
                Column {
                    Text("Active Ball Overlay", style = PulseCastType.bodyMd.copy(fontSize = 13.sp), fontWeight = FontWeight.Bold)
                    Text("Docked to screen right • Tap to preview", style = PulseCastType.bodySm.copy(fontSize = 11.sp), color = OnSurfaceMuted)
                }
            }
            
            // Design shows two buttons: draw and screenshot
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp), // Added space between buttons
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp) // Added space between text and buttons
            ) {
                listOf(
                    Icons.Default.Draw to OnSurfaceMuted,
                    Icons.Default.Screenshot to OnSurfaceMuted
                ).forEach { (icon, color) ->
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = SurfaceHigh
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { }) {
                            Icon(icon, null, Modifier.size(16.dp), color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioVaultPreview(onViewAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, null, Modifier.size(20.dp), CyberCyan)
                Spacer(Modifier.width(8.dp))
                Text("Studio Vault", style = PulseCastType.headlineSm)
            }
            TextButton(onClick = onViewAll) {
                Text("VIEW ALL (14)", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer)
                Icon(Icons.Default.ChevronRight, null, Modifier.size(14.dp), PrimaryContainer)
            }
        }
        
        VaultClipCard(
            title = "Final Ring Ranked Clutch",
            category = "Apex Mobile",
            time = "Today, 09:15",
            size = "420 MB",
            audio = "Stereo Audio",
            duration = "08:42",
            res = "1080p60"
        )
        
        VaultClipCard(
            title = "Configuring OBS Multi-Stream",
            category = "App Walkthrough",
            time = "Yesterday",
            size = "890 MB",
            audio = "Mic Only",
            duration = "14:10",
            res = "1440p",
            resColor = SecondaryContainer
        )
    }
}

@Composable
private fun VaultClipCard(
    title: String,
    category: String,
    time: String,
    size: String,
    audio: String,
    duration: String,
    res: String,
    resColor: Color = PrimaryContainer
) {
    PulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Thumbnail Area
                Box(
                    modifier = Modifier
                        .size(112.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF303541)), // surface-container-highest
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, null, Modifier.size(32.dp), OnSurfaceMuted.copy(alpha = 0.3f))
                    
                    // Duration (Bottom-Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(duration, style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = OnSurface)
                    }
                    
                    // Resolution (Top-Left)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(resColor)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = res,
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = if (resColor == PrimaryContainer) Color.Black else OnSurface
                        )
                    }
                }
                
                // 2. Info Area
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulsePill(
                                text = category,
                                containerColor = Color(0xFF252A36), // surface-container-high
                                contentColor = CyberCyan
                            )
                            Text(
                                text = time,
                                style = PulseCastType.labelTelemetrySm.copy(fontSize = 10.sp),
                                color = OnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = title,
                            style = PulseCastType.bodyMd.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = size,
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 11.sp),
                            color = OnSurfaceMuted
                        )
                        Text(
                            text = audio,
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 11.sp),
                            color = NeonAmber
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
            
            // 3. Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VaultActionButton(Icons.Default.ContentCut, "Trim", CyberCyan)
                VaultActionButton(Icons.Default.GifBox, "GIF", NeonAmber)
                VaultActionButton(Icons.Default.Share, "Share", PrimaryContainer)
                
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultActionButton(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { }
    ) {
        Icon(icon, null, Modifier.size(16.dp), color)
        Text(label, style = PulseCastType.buttonText, fontSize = 12.sp, color = OnSurfaceMuted)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun CaptureHubPreview() {
    PulseCastTheme {
        CaptureHubScreen(
            isRecording = false,
            onRecordClick = {}
        )
    }
}
