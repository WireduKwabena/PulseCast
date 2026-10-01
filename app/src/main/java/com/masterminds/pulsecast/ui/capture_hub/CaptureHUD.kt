package com.masterminds.pulsecast.ui.capture_hub

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.core.MediaStoreMediaRepository
import com.masterminds.pulsecast.core.VaultMediaItem
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*
import kotlinx.coroutines.MainScope

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
    onNavigateToPresets: () -> Unit = {},
    onNavigateToFacecam: () -> Unit = {},
    onNavigateToFloatingSettings: () -> Unit = {}
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
        
        TargetPresetsSection(
            activeResolution = resolution,
            onPresetSelect = { newRes, newFps, newAudio ->
                onResolutionChange(newRes)
                onFpsChange(newFps)
                onAudioModeChange(newAudio)
            },
            onCustomize = onNavigateToPresets
        )
        
        SecondarySetupGrid(
            onNavigateToFacecam = onNavigateToFacecam,
            onNavigateToFloatingSettings = onNavigateToFloatingSettings
        )
        
        FloatingBallSimulator(
            onNavigateToSettings = onNavigateToFloatingSettings
        )
        
        StudioVaultPreview(onViewAll = onNavigateToVault)
        
        Spacer(Modifier.height(80.dp))
    }
}

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
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                    Text(
                        "CAPTURE ENGINE READY",
                        style = PulseCastType.labelTelemetryMd,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = SurfaceHigh,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                        Text("LOW LATENCY", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Telemetry Ring & Spec Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Storage Circular Ring Gauge
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 6.dp.toPx()
                        // Track
                        drawCircle(
                            color = Color(0xFF303541),
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            color = SecondaryFixedDim,
                            startAngle = -90f,
                            sweepAngle = storagePercentage * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(storagePercentage * 100).toInt()}%", style = PulseCastType.labelTelemetryLg, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("STORAGE", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }

                // Spec Matrix Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = SurfaceHigh,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable {
                                val nextRes = when (resolution) {
                                    "1080p" -> "2K"
                                    "2K" -> "4K"
                                    "4K" -> "720p"
                                    else -> "1080p"
                                }
                                onResolutionChange(nextRes)
                            }
                        ) {
                            Text(resolution, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }

                        Surface(
                            color = SurfaceHigh,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable {
                                val nextFps = if (fps == 60) 120 else if (fps == 120) 30 else 60
                                onFpsChange(nextFps)
                            }
                        ) {
                            Text("${fps} FPS", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                        }

                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                            val bitrateText = when (resolution) {
                                "2K" -> "24 Mbps"
                                "4K" -> "45 Mbps"
                                "720p" -> "6 Mbps"
                                else -> "12 Mbps"
                            }
                            Text(bitrateText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 9.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                        Text(freeSpaceText, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                }
            }

            // Dual Audio Input & VU Meter Strip
            Surface(
                color = SurfaceMid,
                shape = RoundedCornerShape(10.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                            Text("Dual Stream Audio", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                                Text("48kHz • STEREO", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 7.sp)
                            }
                        }

                        Surface(
                            color = SecondaryFixedDim.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.clickable {
                                val nextMode = when (audioMode) {
                                    AudioMode.INTERNAL_AND_MIC -> AudioMode.MIC_ONLY
                                    AudioMode.MIC_ONLY -> AudioMode.INTERNAL_ONLY
                                    AudioMode.INTERNAL_ONLY -> AudioMode.INTERNAL_AND_MIC
                                }
                                onAudioModeChange(nextMode)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ToggleOn, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                                Text(
                                    when (audioMode) {
                                        AudioMode.INTERNAL_AND_MIC -> "Internal + Mic"
                                        AudioMode.MIC_ONLY -> "Mic Only"
                                        AudioMode.INTERNAL_ONLY -> "Internal Only"
                                    },
                                    style = PulseCastType.buttonText,
                                    color = SecondaryFixedDim,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Dual VU Meter Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SYSTEM/GAME", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                                Text("-8 dB", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                            }
                            LinearProgressIndicator(
                                progress = { systemAudioLevel },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = SecondaryFixedDim,
                                trackColor = Color(0xFF090E19)
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VOICE MIC", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                                Text("-3 dB", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp)
                            }
                            LinearProgressIndicator(
                                progress = { micAudioLevel },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = PrimaryContainer,
                                trackColor = Color(0xFF090E19)
                            )
                        }
                    }
                }
            }
        }
    }
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
private fun TargetPresetsSection(
    activeResolution: String,
    onPresetSelect: (resolution: String, fps: Int, audioMode: AudioMode) -> Unit,
    onCustomize: () -> Unit
) {
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
                PresetCard(
                    title = "Pro Gaming",
                    desc = "2K • 60fps • Both Audio",
                    icon = Icons.Default.SportsEsports,
                    color = PrimaryContainer,
                    isActive = activeResolution == "2K",
                    onClick = { onPresetSelect("2K", 60, AudioMode.INTERNAL_AND_MIC) }
                )
            }
            item {
                PresetCard(
                    title = "Tutorial Cast",
                    desc = "1080p • Brush • Mic",
                    icon = Icons.Default.Draw,
                    color = CyberCyan,
                    isActive = activeResolution == "1080p",
                    onClick = { onPresetSelect("1080p", 60, AudioMode.MIC_ONLY) }
                )
            }
            item {
                PresetCard(
                    title = "Reaction PIP",
                    desc = "4K • Dual Cam • Both",
                    icon = Icons.Default.VideoCameraFront,
                    color = NeonAmber,
                    isActive = activeResolution == "4K",
                    onClick = { onPresetSelect("4K", 60, AudioMode.INTERNAL_AND_MIC) }
                )
            }
            item {
                PresetCard(
                    title = "Eco Clip",
                    desc = "720p • 30fps • Sys Audio",
                    icon = Icons.Default.BatterySaver,
                    color = OnSurfaceMuted,
                    isActive = activeResolution == "720p",
                    onClick = { onPresetSelect("720p", 30, AudioMode.INTERNAL_ONLY) }
                )
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
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    PulseCard(
        modifier = Modifier.width(144.dp).clickable { onClick() },
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
private fun SecondarySetupGrid(
    onNavigateToFacecam: () -> Unit = {},
    onNavigateToFloatingSettings: () -> Unit = {}
) {
    var isRegionFull by remember { mutableStateOf(true) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp), 
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        SecondaryToggleCard(
            title = "Region",
            detail = "Screen Bounds",
            badge = if (isRegionFull) "FULL" else "CROP",
            icon = Icons.Default.CropFree,
            color = CyberCyan,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            onClick = { isRegionFull = !isRegionFull }
        )
        SecondaryToggleCard(
            title = "Facecam PIP",
            detail = "Circle • Front",
            badge = "CHROMA",
            icon = Icons.Default.AccountBox,
            color = NeonAmber,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            onClick = onNavigateToFacecam
        )
        SecondaryToggleCard(
            title = "Float Ball",
            detail = "Auto Hide",
            badge = "ON",
            icon = Icons.Default.BubbleChart,
            color = PrimaryContainer,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            onClick = onNavigateToFloatingSettings
        )
    }
}

@Composable
private fun SecondaryToggleCard(
    title: String,
    detail: String,
    badge: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    PulseCard(
        modifier = modifier.clickable { onClick() },
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
private fun FloatingBallSimulator(
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current

    PulseCard(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() },
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF303541), CircleShape)
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
                    Box(
                        Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF303541))
                            .padding(2.dp)
                    ) {
                        Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF9cf0ff)))
                    }
                }
                Column {
                    Text("Active Ball Overlay", style = PulseCastType.bodyMd.copy(fontSize = 13.sp), fontWeight = FontWeight.Bold)
                    Text("Docked to screen right • Tap to preview", style = PulseCastType.bodySm.copy(fontSize = 11.sp), color = OnSurfaceMuted)
                }
            }
            
            // Draw & Screenshot action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = SurfaceHigh
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Telestrator Brush Armed: Draw on screen during capture", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Draw, null, Modifier.size(16.dp), CyberCyan)
                    }
                }

                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = SurfaceHigh
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Screenshot Captured & Saved to Vault", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Screenshot, null, Modifier.size(16.dp), NeonAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioVaultPreview(onViewAll: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mediaItems by remember { mutableStateOf<List<VaultMediaItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val repo = MediaStoreMediaRepository(context)
        mediaItems = repo.queryStudioMedia().take(3)
    }

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
                Text("VIEW ALL (${mediaItems.size})", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer)
                Icon(Icons.Default.ChevronRight, null, Modifier.size(14.dp), PrimaryContainer)
            }
        }

        if (mediaItems.isEmpty()) {
            PulseCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceLow,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("No recorded videos found yet. Tap 'Start Screen Recording' to record your first video!", style = PulseCastType.bodySm, color = OnSurfaceMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(12.dp))
            }
        } else {
            mediaItems.forEach { item ->
                VaultClipCard(
                    item = item,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(item.contentUri, "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Play Video"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not play video", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTrim = onViewAll,
                    onGif = onViewAll,
                    onShare = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/*"
                                putExtra(Intent.EXTRA_STREAM, item.contentUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Video Clip"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not share video", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = {
                        val repo = MediaStoreMediaRepository(context)
                        scope.launch {
                            repo.deleteDirect(item.contentUri)
                            mediaItems = repo.queryStudioMedia().take(3)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VaultClipCard(
    item: VaultMediaItem,
    onClick: () -> Unit = {},
    onTrim: () -> Unit = {},
    onGif: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val context = LocalContext.current
    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(item.contentUri) {
        val repo = MediaStoreMediaRepository(context)
        thumbnailBitmap = repo.loadThumbnail(item.contentUri)
    }

    PulseCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        backgroundColor = SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Left-side Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(112.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF303541)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailBitmap != null) {
                        Image(
                            bitmap = thumbnailBitmap!!,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = OnSurfaceMuted.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    }

                    // Resolution Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (item.isClip) NeonAmber else PrimaryContainer)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.resolution,
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }

                    // Duration Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(item.formattedDuration, style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), color = OnSurface)
                    }
                }

                // Right-side Info Area
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
                            PulsePill(text = if (item.isClip) "Clip" else "Recording", containerColor = CyberCyan.copy(alpha = 0.2f), contentColor = CyberCyan)
                            Text(item.formattedDate, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        }
                        Text(
                            item.title,
                            style = PulseCastType.headlineSm.copy(fontSize = 14.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.formattedSize, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                         Text("Stereo Audio", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                    }
                }
            }

            HorizontalDivider(color = SurfaceHigh, thickness = 1.dp)

            // Quick Action Capsule Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onTrim() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                    Text("Trim", style = PulseCastType.buttonText, color = OnSurfaceMuted, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.clickable { onGif() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.GifBox, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                    Text("GIF", style = PulseCastType.buttonText, color = OnSurfaceMuted, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.clickable { onShare() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(16.dp))
                    Text("Share", style = PulseCastType.buttonText, color = OnSurfaceMuted, fontSize = 11.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ElectricRuby, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun CaptureHubPreview() {
    PulseCastTheme {
        CaptureHubScreen(
            isRecording = false,
            onRecordClick = {}
        )
    }
}
