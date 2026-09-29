package com.masterminds.pulsecast.ui.timeline_video_editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*

@Composable
fun TimelineVideoEditorScreen(
    onBack: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playheadPos by remember { mutableFloatStateOf(0.38f) }
    var selectedRatio by remember { mutableStateOf("16:9") }
    var selectedTool by remember { mutableStateOf("Trim / Cut") }
    var activeResolution by remember { mutableStateOf("1080p 60fps") }
    var showExportSettingsModal by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableStateOf("1.0x") }

    Scaffold(
        containerColor = BgBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Utility & Export Header Bar
            TopUtilityHeader(
                activeResolution = activeResolution,
                onResolutionClick = { showExportSettingsModal = !showExportSettingsModal },
                onExportClick = { onExport() }
            )

            // Export Settings Popover Modal
            if (showExportSettingsModal) {
                ExportSettingsPopover(
                    activeResolution = activeResolution,
                    onResolutionSelect = {
                        activeResolution = it
                        showExportSettingsModal = false
                    }
                )
            }

            // Video Player Preview Viewport
            VideoPlayerPreviewViewport(
                selectedRatio = selectedRatio,
                onRatioSelect = { selectedRatio = it },
                isPlaying = isPlaying,
                onPlayToggle = { isPlaying = !isPlaying }
            )

            // Multi-track Precision Timeline Editor
            MultiTrackTimelineEditor(
                playheadPos = playheadPos,
                onPlayheadChange = { playheadPos = it }
            )

            // Horizontal Editing Palette Dock
            EditingPaletteDock(
                selectedTool = selectedTool,
                onToolSelect = { selectedTool = it }
            )

            // Active Tool Adjustment Drawer
            ActiveToolDrawer(
                selectedTool = selectedTool,
                speedMultiplier = speedMultiplier,
                onSpeedMultiplierChange = { speedMultiplier = it }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopUtilityHeader(
    activeResolution: String,
    onResolutionClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = OnSurface, modifier = Modifier.size(16.dp))
                }
            }

            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                }
            }

            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("02:14.18", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onResolutionClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(activeResolution, style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
            }

            Button(
                onClick = onExportClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Export", style = PulseCastType.buttonText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ExportSettingsPopover(
    activeResolution: String,
    onResolutionSelect: (String) -> Unit
) {
    Surface(
        color = SurfaceHigh,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Export Settings", style = PulseCastType.headlineSm, color = OnSurface, fontSize = 13.sp)
                Surface(color = Color(0xFF090E19), shape = RoundedCornerShape(4.dp)) {
                    Text("FHD", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Profile", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                Text(activeResolution, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target Bitrate", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                Text("18.4 Mbps (Ultra)", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Est. File Size", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                Text("~324.6 MB", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("1080p 60fps", "4K 60fps", "720p 30fps").forEach { res ->
                    val isSelected = activeResolution == res
                    Surface(
                        color = if (isSelected) PrimaryContainer else Color(0xFF090E19),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onResolutionSelect(res) }
                    ) {
                        Text(res, modifier = Modifier.padding(vertical = 6.dp), style = PulseCastType.labelTelemetrySm, color = if (isSelected) Color.Black else OnSurfaceMuted, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPlayerPreviewViewport(
    selectedRatio: String,
    onRatioSelect: (String) -> Unit,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    val heightDp = when (selectedRatio) {
        "9:16" -> 280.dp
        "1:1" -> 240.dp
        else -> 190.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090E19)),
        contentAlignment = Alignment.Center
    ) {
        // Player Background Simulation
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF090E19))
                )
            )
        }

        // Pinch Badge
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.8f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Pinch, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(12.dp))
                Text("100% Fit", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            }
        }

        // Timed Caption Watermark Overlay
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("INSANE CLUTCH!", style = PulseCastType.headlineSm, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("🔥", fontSize = 12.sp)
            }
        }

        // Live Controls Scrim
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = PrimaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onPlayToggle() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("02:14 / 08:35", style = PulseCastType.labelTelemetryMd, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }

                // Aspect Switcher Capsule
                Surface(
                    color = SurfaceHigh,
                    shape = CircleShape
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        listOf("16:9", "9:16", "1:1").forEach { ratio ->
                            val isSelected = selectedRatio == ratio
                            Surface(
                                color = if (isSelected) PrimaryContainer else Color.Transparent,
                                shape = CircleShape,
                                modifier = Modifier.clickable { onRatioSelect(ratio) }
                            ) {
                                Text(
                                    text = ratio,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = PulseCastType.labelTelemetrySm,
                                    color = if (isSelected) Color.Black else OnSurfaceMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiTrackTimelineEditor(
    playheadPos: Float,
    onPlayheadChange: (Float) -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Timeline Head Controls & Scrubber Ruler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = SurfaceHigh, shape = RoundedCornerShape(6.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                            Text("Split", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                        }
                    }

                    Surface(color = SurfaceHigh, shape = RoundedCornerShape(6.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                            Text("Marker", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("02:00", "02:14", "02:30", "02:45").forEach { tick ->
                        Text(tick, style = PulseCastType.labelTelemetrySm, color = if (tick == "02:14") OnSurface else OnSurfaceMuted, fontSize = 8.sp, fontWeight = if (tick == "02:14") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Interactive Track Workspace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090E19))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Track 1: Text & Overlay Track
                    Surface(
                        color = Color(0xFF171B27),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("T1: Overlays", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 7.sp)
                            Surface(color = NeonAmber.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                                Text("CLUTCH! 🔥", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Track 2: Primary Video Clip Track with Filmstrip
                    Surface(
                        color = Color(0xFF171B27),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = NeonAmber, shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp), modifier = Modifier.width(10.dp).fillMaxHeight()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                repeat(5) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(SurfaceHigh)
                                    )
                                }
                            }

                            Surface(color = NeonAmber, shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp), modifier = Modifier.width(10.dp).fillMaxHeight()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }

                    // Track 3: Audio & Voiceover Waveform Track
                    Surface(
                        color = Color(0xFF171B27),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(12.dp))
                                Surface(color = SecondaryFixedDim.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text("+200%", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Canvas(modifier = Modifier.weight(1f).height(16.dp).padding(horizontal = 8.dp)) {
                                val step = size.width / 30
                                repeat(30) { i ->
                                    val h = (Math.sin(i.toDouble()) * 8 + 10).toFloat()
                                    drawLine(SecondaryFixedDim, Offset(i * step, size.height / 2 - h / 2), Offset(i * step, size.height / 2 + h / 2), strokeWidth = 1.5.dp.toPx())
                                }
                            }
                        }
                    }
                }

                // Master Scrubber Needle / Playhead
                Slider(
                    value = playheadPos,
                    onValueChange = onPlayheadChange,
                    colors = SliderDefaults.colors(thumbColor = PrimaryContainer, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun EditingPaletteDock(
    selectedTool: String,
    onToolSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Editing Palette", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            Text("Trim & Cut Active", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tools = listOf(
                Triple("Trim / Cut", Icons.Default.ContentCut, PrimaryContainer),
                Triple("Speed", Icons.Default.Speed, NeonAmber),
                Triple("Crop", Icons.Default.AspectRatio, SecondaryFixedDim),
                Triple("Voiceover", Icons.Default.MicExternalOn, PrimaryContainer),
                Triple("Captions", Icons.Default.AutoAwesome, SecondaryFixedDim),
                Triple("Blur Mask", Icons.Default.BlurOn, OnSurfaceMuted),
                Triple("GIF Maker", Icons.Default.GifBox, NeonAmber)
            )

            items(tools) { (label, icon, color) ->
                val isSelected = selectedTool == label
                Surface(
                    color = if (isSelected) PrimaryContainer else SurfaceLow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(width = 72.dp, height = 70.dp)
                        .clickable { onToolSelect(label) }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isSelected) Color.Black else color, modifier = Modifier.size(22.dp))
                        Text(label, style = PulseCastType.buttonText, color = if (isSelected) Color.Black else OnSurface, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveToolDrawer(
    selectedTool: String,
    speedMultiplier: String,
    onSpeedMultiplierChange: (String) -> Unit
) {
    Surface(
        color = SurfaceHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                Column {
                    Text("$selectedTool Mode", style = PulseCastType.buttonText, color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Smooth bullet-time ramp (0.5x → 4.0x)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = Color(0xFF090E19), shape = RoundedCornerShape(6.dp)) {
                    Text(speedMultiplier, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = SurfaceLow,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val nextSpeed = when (speedMultiplier) {
                                "1.0x" -> "0.5x"
                                "0.5x" -> "2.0x"
                                "2.0x" -> "4.0x"
                                else -> "1.0x"
                            }
                            onSpeedMultiplierChange(nextSpeed)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Speed Curve", tint = OnSurface, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun TimelineVideoEditorPreview() {
    PulseCastTheme {
        TimelineVideoEditorScreen()
    }
}
