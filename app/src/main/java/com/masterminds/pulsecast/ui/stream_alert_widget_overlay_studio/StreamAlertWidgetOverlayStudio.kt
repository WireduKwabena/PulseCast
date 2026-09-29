package com.masterminds.pulsecast.ui.stream_alert_widget_overlay_studio

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
fun StreamAlertWidgetOverlayStudioScreen(
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {}
) {
    var isLandscapeAspect by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf("Cyber Horizon Pro Theme") }
    
    // Layer Visibility States
    var alertsEnabled by remember { mutableStateOf(true) }
    var tipTickerEnabled by remember { mutableStateOf(true) }
    var goalMeterEnabled by remember { mutableStateOf(true) }
    var chatOverlayEnabled by remember { mutableStateOf(true) }
    var watermarkEnabled by remember { mutableStateOf(true) }
    
    // Settings States
    var minTipThreshold by remember { mutableFloatStateOf(1.0f) }
    var activeGoalMode by remember { mutableStateOf("Subs") }
    var activeSoundPack by remember { mutableStateOf("Cyber Synth") }
    var activeEntranceAnimation by remember { mutableStateOf("Slide & Glitch") }
    
    // Trigger Simulation States
    var isAlertAnimating by remember { mutableStateOf(false) }
    var isStormSimulating by remember { mutableStateOf(false) }

    val alertScale by animateFloatAsState(
        targetValue = if (isAlertAnimating || isStormSimulating) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "alertScale"
    )

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
            // Top Context Header & Actions Bar
            HeaderSection(
                onReset = {
                    alertsEnabled = true
                    tipTickerEnabled = true
                    goalMeterEnabled = true
                    chatOverlayEnabled = true
                    watermarkEnabled = true
                    minTipThreshold = 1.0f
                },
                onSave = onPublish
            )

            // Theme Preset Capsule Selector
            PresetCapsuleSelector(selectedPreset = selectedPreset)

            // Studio Stage Preview: Dynamic Interactive Canvas (16:9 / 9:16)
            StudioCanvasViewport(
                isLandscape = isLandscapeAspect,
                onAspectChange = { isLandscapeAspect = it },
                alertsEnabled = alertsEnabled,
                goalMeterEnabled = goalMeterEnabled,
                chatOverlayEnabled = chatOverlayEnabled,
                watermarkEnabled = watermarkEnabled,
                alertScale = alertScale,
                activeGoalMode = activeGoalMode
            )

            // Active Widget Deck: Studio Layer & Control Stack
            ActiveWidgetDeck(
                alertsEnabled = alertsEnabled,
                onAlertsToggle = { alertsEnabled = it },
                tipTickerEnabled = tipTickerEnabled,
                onTipTickerToggle = { tipTickerEnabled = it },
                goalMeterEnabled = goalMeterEnabled,
                onGoalMeterToggle = { goalMeterEnabled = it },
                chatOverlayEnabled = chatOverlayEnabled,
                onChatOverlayToggle = { chatOverlayEnabled = it },
                watermarkEnabled = watermarkEnabled,
                onWatermarkToggle = { watermarkEnabled = it },
                minTipThreshold = minTipThreshold,
                onTipThresholdChange = { minTipThreshold = it },
                activeGoalMode = activeGoalMode,
                onGoalModeChange = { activeGoalMode = it },
                onTriggerTestAlert = { isAlertAnimating = !isAlertAnimating }
            )

            // Studio Audio FX & Entrance Animation Preferences
            FXAndMotionDynamicsSection(
                activeSoundPack = activeSoundPack,
                onSoundPackSelect = { activeSoundPack = it },
                activeEntranceAnimation = activeEntranceAnimation,
                onEntranceAnimationSelect = { activeEntranceAnimation = it }
            )

            // Studio Bottom Primary Action Bar
            BottomActionArea(
                onSimulateStorm = { isStormSimulating = !isStormSimulating },
                onPublish = onPublish
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection(onReset: () -> Unit, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SecondaryFixedDim)
                    )
                    Text(
                        text = "STUDIO CANVAS ENGINE",
                        style = PulseCastType.labelTelemetrySm,
                        color = SecondaryFixedDim,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Overlay & Widget Studio",
                    style = PulseCastType.headlineLg,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onReset() }
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Canvas", tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Save", style = PulseCastType.buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCapsuleSelector(selectedPreset: String) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                    }
                }
                Column {
                    Text("Active Profile Preset", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text(selectedPreset, style = PulseCastType.buttonText, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = SecondaryFixedDim.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "v3.4 LIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = PulseCastType.labelTelemetrySm,
                        color = SecondaryFixedDim,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StudioCanvasViewport(
    isLandscape: Boolean,
    onAspectChange: (Boolean) -> Unit,
    alertsEnabled: Boolean,
    goalMeterEnabled: Boolean,
    chatOverlayEnabled: Boolean,
    watermarkEnabled: Boolean,
    alertScale: Float,
    activeGoalMode: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                Text("Interactive Canvas Viewport", style = PulseCastType.buttonText, color = OnSurface)
            }

            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    Surface(
                        color = if (isLandscape) SurfaceLow else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onAspectChange(true) }
                    ) {
                        Text(
                            text = "16:9 Landscape",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = PulseCastType.labelTelemetrySm,
                            color = if (isLandscape) OnSurface else OnSurfaceMuted,
                            fontWeight = if (isLandscape) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                    Surface(
                        color = if (!isLandscape) SurfaceLow else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onAspectChange(false) }
                    ) {
                        Text(
                            text = "9:16 Shorts",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = PulseCastType.labelTelemetrySm,
                            color = if (!isLandscape) OnSurface else OnSurfaceMuted,
                            fontWeight = if (!isLandscape) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Viewport Box Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLandscape) 210.dp else 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090E19)),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop Gradient & Grid Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepX = size.width / 3
                val stepY = size.height / 3
                val stroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                drawRect(Color(0xFF0F1524))
                
                // Alignment Grid Snap Lines
                drawLine(SecondaryFixedDim.copy(alpha = 0.15f), Offset(stepX, 0f), Offset(stepX, size.height), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
                drawLine(SecondaryFixedDim.copy(alpha = 0.15f), Offset(stepX * 2, 0f), Offset(stepX * 2, size.height), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
                drawLine(SecondaryFixedDim.copy(alpha = 0.15f), Offset(0f, stepY), Offset(size.width, stepY), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
                drawLine(SecondaryFixedDim.copy(alpha = 0.15f), Offset(0f, stepY * 2), Offset(size.width, stepY * 2), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
            }

            // Top Center: Custom Brand Watermark Badge
            if (watermarkEnabled) {
                Surface(
                    color = Color(0xFF090E19).copy(alpha = 0.8f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(12.dp))
                        Text("PULSE PRO", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Top-Left: Recent Follower Alert Widget
            if (alertsEnabled) {
                Surface(
                    color = SurfaceHigh.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.4f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .scale(alertScale)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = PrimaryContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            }
                        }
                        Column {
                            Text("NEW FOLLOWER", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            Text("@ViperFan ⚡", style = PulseCastType.labelTelemetryMd, color = OnSurface, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top-Right: Sub Goal Meter Widget
            if (goalMeterEnabled) {
                Surface(
                    color = SurfaceHigh.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .width(110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$activeGoalMode Goal", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                            Text("74%", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { 0.74f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = SecondaryFixedDim,
                            trackColor = SurfaceMid
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("742", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 7.sp)
                            Text("1,000", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 7.sp)
                        }
                    }
                }
            }

            // Bottom-Left: Live Stream Chat Box Widget
            if (chatOverlayEnabled) {
                Surface(
                    color = Color(0xFF090E19).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .width(130.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(Modifier.size(4.dp).clip(CircleShape).background(SecondaryFixedDim))
                                Text("Live Chat", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 7.sp)
                            }
                            Text("8s fade", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 7.sp)
                        }
                        Text("Kira99: Clutch round!! 🔥", style = PulseCastType.bodySm, color = OnSurface, fontSize = 8.sp, maxLines = 1)
                        Text("GamerX: clip that now", style = PulseCastType.bodySm, color = OnSurface, fontSize = 8.sp, maxLines = 1)
                        Text("Zacko: 120 FPS clean", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp, maxLines = 1)
                    }
                }
            }

            // Bottom-Right: Dynamic Facecam PIP Frame Widget
            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SecondaryFixedDim.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(width = 64.dp, height = 72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceMuted.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    Surface(
                        color = Color(0xFF090E19).copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Text("CAM FEED", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 6.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 1.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                }
            }

            // Center Reticle Focus
            Icon(Icons.Default.FilterCenterFocus, contentDescription = null, tint = SecondaryFixedDim.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ActiveWidgetDeck(
    alertsEnabled: Boolean,
    onAlertsToggle: (Boolean) -> Unit,
    tipTickerEnabled: Boolean,
    onTipTickerToggle: (Boolean) -> Unit,
    goalMeterEnabled: Boolean,
    onGoalMeterToggle: (Boolean) -> Unit,
    chatOverlayEnabled: Boolean,
    onChatOverlayToggle: (Boolean) -> Unit,
    watermarkEnabled: Boolean,
    onWatermarkToggle: (Boolean) -> Unit,
    minTipThreshold: Float,
    onTipThresholdChange: (Float) -> Unit,
    activeGoalMode: String,
    onGoalModeChange: (String) -> Unit,
    onTriggerTestAlert: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                Text("Active Widget Deck", style = PulseCastType.buttonText, color = OnSurface)
            }
            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("5 Layers Attached", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }
        }

        // Layer 1: Follower & Sub Alerts Card
        LayerCard(
            title = "Follower & Sub Alerts",
            subtitle = "Neon banner on trigger",
            icon = Icons.Default.Campaign,
            color = PrimaryContainer,
            enabled = alertsEnabled,
            onToggle = onAlertsToggle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E19), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                    Text("Chime: Cyber Synth Bell", style = PulseCastType.labelTelemetrySm, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onTriggerTestAlert() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                        Text("Trigger Test Alert", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Layer 2: Live Tip / Donation Ticker Card
        LayerCard(
            title = "Live Tip / Donation Ticker",
            subtitle = "Horizontal scrolling ledger",
            icon = Icons.Default.Payments,
            color = SecondaryFixedDim,
            enabled = tipTickerEnabled,
            onToggle = onTipTickerToggle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E19), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Minimum Tip Threshold", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                    Text(String.format(Locale.US, "$%.2f", minTipThreshold), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = minTipThreshold,
                    onValueChange = onTipThresholdChange,
                    valueRange = 1.0f..25.0f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                    modifier = Modifier.height(20.dp)
                )
            }
        }

        // Layer 3: Dynamic Goal Meter Card
        LayerCard(
            title = "Dynamic Goal Meter",
            subtitle = "Progress tracker bar",
            icon = Icons.Default.Flag,
            color = NeonAmber,
            enabled = goalMeterEnabled,
            onToggle = onGoalMeterToggle
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090E19), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Subs", "Stars", "Followers").forEach { mode ->
                        val isSelected = activeGoalMode == mode
                        Surface(
                            color = if (isSelected) SurfaceHigh else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onGoalModeChange(mode) }
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 4.dp),
                                style = PulseCastType.labelTelemetrySm,
                                color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF090E19),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Goal:", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                            Text("1,000", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = Color(0xFF090E19),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Theme:", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(SecondaryFixedDim, PrimaryContainer)))
                            )
                        }
                    }
                }
            }
        }

        // Layer 4: Stream Chat Overlay Box Card
        LayerCard(
            title = "Stream Chat Overlay",
            subtitle = "Transparent chat feed",
            icon = Icons.AutoMirrored.Filled.Chat,
            color = OnSurface,
            enabled = chatOverlayEnabled,
            onToggle = onChatOverlayToggle
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniSettingPill("Backdrop Opacity", "75% Glass", Modifier.weight(1f))
                MiniSettingPill("Max Messages", "5 Messages", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniSettingPill("Fade Timeout", "8.0 Seconds", Modifier.weight(1f))
                MiniSettingPill("Profanity Filter", "Shield ON", Modifier.weight(1f), isShield = true)
            }
        }

        // Layer 5: Brand Watermark Card
        LayerCard(
            title = "Brand Watermark",
            subtitle = "Anchor: Top-Center (30% scale)",
            icon = Icons.Default.FileDownloadOff,
            color = OnSurfaceMuted,
            enabled = watermarkEnabled,
            onToggle = onWatermarkToggle
        )
    }
}

@Composable
private fun LayerCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, style = PulseCastType.buttonText, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Box(Modifier.size(6.dp).clip(CircleShape).background(if (enabled) PrimaryContainer else SurfaceMid))
                        }
                        Text(subtitle, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrimaryContainer),
                    modifier = Modifier.scale(0.8f)
                )
            }

            if (content != null && enabled) {
                content()
            }
        }
    }
}

@Composable
private fun MiniSettingPill(label: String, value: String, modifier: Modifier = Modifier, isShield: Boolean = false) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isShield) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(12.dp))
                }
                Text(value, style = PulseCastType.labelTelemetryMd, color = if (isShield) SecondaryFixedDim else OnSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FXAndMotionDynamicsSection(
    activeSoundPack: String,
    onSoundPackSelect: (String) -> Unit,
    activeEntranceAnimation: String,
    onEntranceAnimationSelect: (String) -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                Text("FX & Motion Dynamics", style = PulseCastType.buttonText, color = OnSurface)
            }

            // Sound Pack Picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Alert Audio Sound Pack", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("8-Bit Arcade", "Cyber Synth", "Minimal Chime", "Epic Brass").chunked(2).forEach { colPacks ->
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            colPacks.forEach { pack ->
                                val isSelected = activeSoundPack == pack
                                Surface(
                                    color = if (isSelected) SurfaceHigh else Color(0xFF090E19),
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (isSelected) BorderStroke(1.dp, SecondaryFixedDim) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSoundPackSelect(pack) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(pack, style = PulseCastType.bodySm, color = if (isSelected) SecondaryFixedDim else OnSurfaceMuted, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        Icon(if (isSelected) Icons.Default.CheckCircle else Icons.Default.MusicNote, contentDescription = null, tint = if (isSelected) SecondaryFixedDim else OnSurfaceMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Entrance Animation Mode
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Widget Entrance Animation Style", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Slide & Glitch", "Neon Zoom In", "Dissolve").forEach { anim ->
                        val isSelected = activeEntranceAnimation == anim
                        Surface(
                            color = if (isSelected) PrimaryContainer else SurfaceHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEntranceAnimationSelect(anim) }
                        ) {
                            Text(
                                text = anim,
                                modifier = Modifier.padding(vertical = 6.dp),
                                style = PulseCastType.labelTelemetrySm,
                                color = if (isSelected) Color.Black else OnSurfaceMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionArea(onSimulateStorm: () -> Unit, onPublish: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onSimulateStorm,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = SecondaryFixedDim),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Simulate Alert Storm in Preview", style = PulseCastType.buttonText)
            }
        }

        Button(
            onClick = onPublish,
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
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Publish Overlay to Stream Ingest", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun StreamAlertWidgetOverlayStudioPreview() {
    PulseCastTheme {
        StreamAlertWidgetOverlayStudioScreen()
    }
}
