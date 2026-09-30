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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(PrimaryContainer.copy(alpha = 0.08f), CircleShape)
                .blur(40.dp)
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SignalGreen)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CAPTURE READINESS",
                        style = PulseCastType.labelTelemetrySm,
                        color = OnSurfaceMuted
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = SurfaceHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SdCard,
                            contentDescription = null,
                            tint = SecondaryFixedDim,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            freeSpaceText,
                            style = PulseCastType.labelTelemetrySm.copy(fontSize = 10.sp),
                            color = SecondaryFixedDim
                        )
                    }
                }
            }

            // Storage Meter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Internal Storage Load", style = PulseCastType.bodySm, color = OnSurface)
                    Text(
                        "${(storagePercentage * 100).toInt()}% Used",
                        style = PulseCastType.labelTelemetrySm,
                        color = if (storagePercentage > 0.90f) ElectricRuby else OnSurfaceMuted
                    )
                }
                LinearProgressIndicator(
                    progress = { storagePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (storagePercentage > 0.90f) ElectricRuby else PrimaryContainer,
                    trackColor = SurfaceHigh
                )
            }

            // Hardware Controls
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Resolution Selector
                val resolutions = listOf("720p", "1080p", "2K", "4K")
                resolutions.forEach { res ->
                    val isSelected = resolution == res
                    FilterChip(
                        selected = isSelected,
                        onClick = { onResolutionChange(res) },
                        label = { Text(res) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) PrimaryContainer else SurfaceHigh,
                            labelColor = if (isSelected) Color.Black else OnSurface
                        )
                    )
                }

                // FPS Selector
                val fpsList = listOf(30, 60, 120)
                fpsList.forEach { f ->
                    val isSelected = fps == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFpsChange(f) },
                        label = { Text("${f}FPS") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) SecondaryFixedDim else SurfaceHigh,
                            labelColor = if (isSelected) Color.Black else OnSurface
                        )
                    )
                }
            }

            // Audio Source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, null, Modifier.size(16.dp), CyberCyan)
                    Spacer(Modifier.width(6.dp))
                    Text("Audio Source:", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        when(audioMode) {
                            AudioMode.INTERNAL_ONLY -> "Internal Only"
                            AudioMode.MIC_ONLY -> "Mic Only"
                            AudioMode.INTERNAL_AND_MIC -> "Internal + Mic"
                        },
                        style = PulseCastType.buttonText,
                        color = OnSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AudioMode.entries.forEach { mode ->
                        val isSelected = audioMode == mode
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) CyberCyan else SurfaceHigh,
                            modifier = Modifier.clickable { onAudioModeChange(mode) }
                        ) {
                            Text(
                                when(mode) {
                                    AudioMode.INTERNAL_ONLY -> "Sys"
                                    AudioMode.MIC_ONLY -> "Mic"
                                    AudioMode.INTERNAL_AND_MIC -> "Both"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = PulseCastType.labelTelemetrySm,
                                color = if (isSelected) Color.Black else OnSurfaceMuted
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
                    title = item.title,
                    category = item.mimeType,
                    time = item.formattedDate,
                    size = item.formattedSize,
                    audio = "Audio Muxed",
                    duration = item.formattedDuration,
                    res = item.resolution,
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
                    }
                )
            }
        }
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
    resColor: Color = PrimaryContainer,
    onClick: () -> Unit = {}
) {
    PulseCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        backgroundColor = SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(112.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF303541)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, null, Modifier.size(32.dp), OnSurfaceMuted.copy(alpha = 0.3f))
                    
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
                            PulsePill(text = category, containerColor = CyberCyan.copy(alpha = 0.2f), contentColor = CyberCyan)
                            Text(size, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        }
                        Text(
                            title,
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
                        Text(time, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        Text(audio, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                    }
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
