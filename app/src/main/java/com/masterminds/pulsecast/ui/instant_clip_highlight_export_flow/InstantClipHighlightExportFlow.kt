package com.masterminds.pulsecast.ui.instant_clip_highlight_export_flow

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*
import java.util.Locale

@Composable
fun InstantClipHighlightExportFlowScreen(
    onBack: () -> Unit = {},
    onSaveToVault: () -> Unit = {}
) {
    var selectedRatio by remember { mutableStateOf("9:16") }
    var isPlaying by remember { mutableStateOf(false) }
    var trimStartSec by remember { mutableFloatStateOf(14.2f) }
    var trimEndSec by remember { mutableFloatStateOf(42.6f) }
    
    // Customization Toggles
    var pipDockingEnabled by remember { mutableStateOf(true) }
    var pipScalePct by remember { mutableFloatStateOf(40f) }
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var slowMoEnabled by remember { mutableStateOf(true) }
    var logoStampEnabled by remember { mutableStateOf(true) }
    
    var selectedShareTarget by remember { mutableStateOf("TikTok") }
    var isRendering by remember { mutableStateOf(false) }

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
            // Sub-header & Buffer Badge (Context Header)
            HeaderContextSection(onBack = onBack)

            // Aspect Ratio Mode Selector
            AspectRatioSelector(
                selectedRatio = selectedRatio,
                onRatioSelect = { selectedRatio = it }
            )

            // Video Viewport with Interactive Framing Overlay & Facecam PIP
            VideoViewport(
                selectedRatio = selectedRatio,
                isPlaying = isPlaying,
                onPlayToggle = { isPlaying = !isPlaying },
                pipEnabled = pipDockingEnabled,
                subtitlesEnabled = subtitlesEnabled
            )

            // AI Gaming Highlight Jump Marks
            AiEventMarkersSection(
                onMarkerClick = { start, end ->
                    trimStartSec = start
                    trimEndSec = end
                }
            )

            // Smart Video Trimmer & Audio Waveform Deck
            PrecisionTrimmerDeck(
                trimStartSec = trimStartSec,
                trimEndSec = trimEndSec,
                onTrimChange = { start, end ->
                    trimStartSec = start
                    trimEndSec = end
                }
            )

            // Clip Customization & AI VideoFX
            ClipCustomizationSection(
                pipDockingEnabled = pipDockingEnabled,
                onPipDockingToggle = { pipDockingEnabled = it },
                pipScalePct = pipScalePct,
                onPipScaleChange = { pipScalePct = it },
                subtitlesEnabled = subtitlesEnabled,
                onSubtitlesToggle = { subtitlesEnabled = it },
                slowMoEnabled = slowMoEnabled,
                onSlowMoToggle = { slowMoEnabled = it },
                logoStampEnabled = logoStampEnabled,
                onLogoStampToggle = { logoStampEnabled = it }
            )

            // Multichannel Instant Export & Share Targets
            QuickShareDirectSection(
                selectedShareTarget = selectedShareTarget,
                onShareTargetSelect = { selectedShareTarget = it }
            )

            // Primary Bottom Actions Sticky Drawer
            BottomActionDrawer(
                selectedRatio = selectedRatio,
                onRenderClick = { isRendering = true },
                onSaveVaultClick = onSaveToVault
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (isRendering) {
        RenderingDialog(onDismiss = { isRendering = false })
    }
}

@Composable
private fun HeaderContextSection(onBack: () -> Unit) {
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
                    .size(36.dp)
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface, modifier = Modifier.size(18.dp))
                }
            }

            Column {
                Text("Instant Highlight Studio", style = PulseCastType.headlineSm, color = OnSurface)
                Text("In-Game VOD Capture #942", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 11.sp)
            }
        }

        Surface(
            color = SurfaceHigh,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer)
                )
                Text("BUFFER: 60s", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun AspectRatioSelector(
    selectedRatio: String,
    onRatioSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Export Frame Framing", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
            Text("Smart AI Centered", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
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
                listOf(
                    Triple("9:16", "9:16 Shorts/TikTok", Icons.Default.StayCurrentPortrait),
                    Triple("16:9", "16:9 Wide VOD", Icons.Default.StayCurrentLandscape),
                    Triple("1:1", "1:1 Square", Icons.Default.CropSquare)
                ).forEach { (ratioKey, label, icon) ->
                    val isSelected = selectedRatio == ratioKey
                    Surface(
                        color = if (isSelected) SurfaceHigh else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRatioSelect(ratioKey) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) SecondaryFixedDim else OnSurfaceMuted, modifier = Modifier.size(14.dp))
                            Text(label, style = PulseCastType.buttonText, color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoViewport(
    selectedRatio: String,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    pipEnabled: Boolean,
    subtitlesEnabled: Boolean
) {
    val heightDp = when (selectedRatio) {
        "16:9" -> 190.dp
        "1:1" -> 250.dp
        else -> 300.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090E19)),
        contentAlignment = Alignment.Center
    ) {
        // Base Cyberpunk Video Background Pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF090E19))
                )
            )
        }

        // Active Framing Guide Box
        val guideWidthFraction = when (selectedRatio) {
            "16:9" -> 1.0f
            "1:1" -> 0.8f
            else -> 0.56f
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(guideWidthFraction)
                .border(1.dp, SecondaryFixedDim.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Reticle center
            Icon(Icons.Default.FilterCenterFocus, contentDescription = null, tint = SecondaryFixedDim.copy(alpha = 0.25f), modifier = Modifier.size(24.dp))

            // Subtitle Overlay Mock
            if (subtitlesEnabled) {
                Surface(
                    color = Color(0xFF090E19).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TRIPLE HEADSHOT!", style = PulseCastType.headlineSm, color = SecondaryFixedDim, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("\"No way he pushed that angle!\"", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
                    }
                }
            }
        }

        // Facecam PIP Overlay
        if (pipEnabled) {
            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SecondaryFixedDim.copy(alpha = 0.5f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceMuted.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    Surface(
                        color = Color(0xFF090E19).copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Text("PIP CAM", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 6.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }
        }

        // Live Telemetry Watermarks
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.7f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(PrimaryContainer))
                Text("AlexViper Gaming", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
            }
        }

        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.7f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(10.dp))
                Text("60 FPS RAW", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            }
        }

        // Center Play/Pause HUD Trigger
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.75f),
            shape = CircleShape,
            border = BorderStroke(1.dp, SecondaryFixedDim.copy(alpha = 0.4f)),
            modifier = Modifier
                .size(48.dp)
                .clickable { onPlayToggle() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = SecondaryFixedDim, modifier = Modifier.size(28.dp))
            }
        }

        // Bottom Timecode HUD
        Surface(
            color = Color(0xFF090E19).copy(alpha = 0.85f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("00:22.4", style = PulseCastType.labelTelemetryMd, color = PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("/ 00:60.0", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Smart-Track Tracked", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AiEventMarkersSection(onMarkerClick: (Float, Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(14.dp))
                Text("AI Event Markers", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }
            Text("Tap to snap trim", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val markers = listOf(
                Triple("⭐ Triple Kill", "00:18s", 12f to 30f),
                Triple("🔥 Victory Drop", "00:35s", 30f to 50f),
                Triple("🗣️ Mic Scream", "00:22s", 18f to 36f)
            )
            items(markers) { (title, time, range) ->
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { onMarkerClick(range.first, range.second) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(title, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text(time, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrecisionTrimmerDeck(
    trimStartSec: Float,
    trimEndSec: Float,
    onTrimChange: (Float, Float) -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(16.dp))
                    Text("Precision Clip Trimmer", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(
                    color = PrimaryContainer.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Trimmed: ${String.format(Locale.US, "00:%.1fs", trimEndSec - trimStartSec)}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filmstrip & Waveform Deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090E19)),
                contentAlignment = Alignment.Center
            ) {
                // Waveform Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = size.width / 40
                    repeat(40) { index ->
                        val x = index * step
                        val h = (Math.sin(index.toDouble()) * 20 + 25).toFloat()
                        drawLine(
                            color = if (index in 12..28) SecondaryFixedDim else Color.DarkGray,
                            start = Offset(x, size.height / 2 - h / 2),
                            end = Offset(x, size.height / 2 + h / 2),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Trim Sliders
                RangeSlider(
                    value = trimStartSec..trimEndSec,
                    onValueChange = { range -> onTrimChange(range.start, range.endInclusive) },
                    valueRange = 0f..60f,
                    colors = SliderDefaults.colors(thumbColor = SecondaryFixedDim, activeTrackColor = SecondaryFixedDim.copy(alpha = 0.6f), inactiveTrackColor = Color.Black.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start: ${String.format(Locale.US, "00:%.1fs", trimStartSec)}", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                Text("Auto-Snapped to Headshot", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                Text("End: ${String.format(Locale.US, "00:%.1fs", trimEndSec)}", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun ClipCustomizationSection(
    pipDockingEnabled: Boolean,
    onPipDockingToggle: (Boolean) -> Unit,
    pipScalePct: Float,
    onPipScaleChange: (Float) -> Unit,
    subtitlesEnabled: Boolean,
    onSubtitlesToggle: (Boolean) -> Unit,
    slowMoEnabled: Boolean,
    onSlowMoToggle: (Boolean) -> Unit,
    logoStampEnabled: Boolean,
    onLogoStampToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Clip Customization", style = PulseCastType.buttonText, color = OnSurface)
            Text("AI VideoFX", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
        }

        // Facecam PIP Docking Tile
        Surface(
            color = SurfaceLow,
            shape = RoundedCornerShape(12.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Facecam PIP Docking", style = PulseCastType.buttonText, color = OnSurface)
                            Text("Align stream reaction cam over 9:16 gameplay", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        }
                    }
                    Switch(checked = pipDockingEnabled, onCheckedChange = onPipDockingToggle, modifier = Modifier.scale(0.8f))
                }

                if (pipDockingEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Scale", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        Slider(
                            value = pipScalePct,
                            onValueChange = onPipScaleChange,
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                        )
                        Text("${pipScalePct.toInt()}% Top", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }
        }

        // Cyber Neon Subtitles Tile
        Surface(
            color = SurfaceLow,
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
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Cyber Neon Subtitles", style = PulseCastType.buttonText, color = OnSurface)
                        Text("Auto speech-to-text with glow highlight", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }
                Switch(checked = subtitlesEnabled, onCheckedChange = onSubtitlesToggle, modifier = Modifier.scale(0.8f))
            }
        }

        // Slow Mo & Logo Duo Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = SurfaceLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kill Slow-Mo", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Text("0.5x Ramp", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                    Switch(checked = slowMoEnabled, onCheckedChange = onSlowMoToggle, modifier = Modifier.scale(0.7f))
                }
            }

            Surface(
                color = SurfaceLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Logo Stamp", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Text("Top Corner", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                    Switch(checked = logoStampEnabled, onCheckedChange = onLogoStampToggle, modifier = Modifier.scale(0.7f))
                }
            }
        }
    }
}

@Composable
private fun QuickShareDirectSection(
    selectedShareTarget: String,
    onShareTargetSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Quick Share Direct", style = PulseCastType.buttonText, color = OnSurface)
            Text("Zero Re-encoding", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
        }

        val sharePlatforms = listOf(
            Triple("TikTok", Icons.Default.MusicNote, SecondaryFixedDim),
            Triple("YT Shorts", Icons.Default.PlayCircle, PrimaryContainer),
            Triple("Reels", Icons.Default.AutoStories, NeonAmber),
            Triple("Discord", Icons.AutoMirrored.Filled.Chat, SecondaryFixedDim),
            Triple("Gallery", Icons.Default.DownloadForOffline, OnSurface)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sharePlatforms.forEach { (name, icon, color) ->
                val isSelected = selectedShareTarget == name
                Surface(
                    color = if (isSelected) SurfaceHigh else SurfaceLow,
                    shape = RoundedCornerShape(10.dp),
                    border = if (isSelected) BorderStroke(1.dp, color) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShareTargetSelect(name) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = color.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(name, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Surface(
            color = Color(0xFF090E19),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("1080p 60FPS • 12 Mbps CBR", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(12.dp))
                    Text("Est: ~42.8 MB", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun BottomActionDrawer(
    selectedRatio: String,
    onRenderClick: () -> Unit,
    onSaveVaultClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onRenderClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Render & Export $selectedRatio Clip", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Button(
            onClick = onSaveVaultClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryFixedDim)
                Text("Save to PulseCast Vault", style = PulseCastType.buttonText)
            }
        }
    }
}

@Composable
private fun RenderingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = SecondaryFixedDim) }
        },
        title = { Text("Rendering MP4 Clip...", style = PulseCastType.headlineSm) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(color = PrimaryContainer)
                Text("Hardware encoder processing frame-accurate 60 FPS trim...", style = PulseCastType.bodySm, color = OnSurfaceMuted, textAlign = TextAlign.Center)
            }
        },
        containerColor = SurfaceLow,
        titleContentColor = OnSurface
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun InstantClipHighlightExportFlowPreview() {
    PulseCastTheme {
        InstantClipHighlightExportFlowScreen()
    }
}
