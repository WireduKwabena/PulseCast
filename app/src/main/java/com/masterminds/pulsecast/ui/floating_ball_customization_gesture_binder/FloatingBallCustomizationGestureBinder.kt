package com.masterminds.pulsecast.ui.floating_ball_customization_gesture_binder

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingBallCustomizationScreen(
    onClose: () -> Unit = {}
) {
    var idleOpacity by remember { mutableFloatStateOf(0.35f) }
    var activeOpacity by remember { mutableFloatStateOf(1.0f) }
    var dockingMode by remember { mutableStateOf("Magnetic") }
    var skin by remember { mutableStateOf("Cyber Red") }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = BgBase,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 48.dp, height = 4.dp)
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
            OrbCustomizationHeader()

            LivePhysicsSimulator(idleOpacity, skin)

            OpacityControls(
                idleOpacity = idleOpacity,
                onIdleChange = { idleOpacity = it },
                activeOpacity = activeOpacity,
                onActiveChange = { activeOpacity = it }
            )

            DockingPhysicsSection(
                selectedMode = dockingMode,
                onModeSelect = { dockingMode = it }
            )

            GestureActionBinder()

            RadialQuickMenuSlots()

            OrbVisualSkins(
                selectedSkin = skin,
                onSkinSelect = { skin = it }
            )

            BottomActionArea()
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun OrbCustomizationHeader(onReset: () -> Unit = {}) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row containing Icon and Title - weight(1f) allows wrapping if needed
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Adjust, null, Modifier.size(20.dp), PrimaryContainer)
                    Text(
                        text = "Floating Orb Customization",
                        style = PulseCastType.headlineSm
                    )
                }
                
                // Reset Button in its own Box for spacing
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onReset() }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.RestartAlt, null, Modifier.size(14.dp), OnSurfaceMuted)
                            Text("Reset", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        }
                    }
                }
            }
            // Subheader now has full width of the Column to prevent shared-row truncation
            Text(
                text = "Gesture triggers, docking physics & visual skins", 
                style = PulseCastType.bodySm, 
                color = OnSurfaceMuted
            )
        }
    }
}

@Composable
private fun LivePhysicsSimulator(idleOpacity: Float, skin: String) {
    val isCyberRed = skin == "Cyber Red"
    val orbIcon = when (skin) {
        "Stealth Carbon" -> Icons.Default.RadioButtonUnchecked
        "Apex Gold" -> Icons.Default.MilitaryTech
        "Cyan Tron" -> Icons.Default.Grid4x4
        else -> Icons.Default.Adjust
    }
    val orbColor = when (skin) {
        "Stealth Carbon" -> OnSurfaceMuted
        "Apex Gold" -> NeonAmber
        "Cyan Tron" -> SecondaryFixedDim
        else -> PrimaryContainer
    }
    val orbBrush = if (isCyberRed) {
        Brush.linearGradient(listOf(ElectricRuby, CyberCyan))
    } else {
        Brush.linearGradient(listOf(orbColor, orbColor.copy(alpha = 0.6f)))
    }

    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ScreenRotation, null, Modifier.size(18.dp), SecondaryFixedDim)
                    Text(
                        text = "Live Physics Simulator", 
                        style = PulseCastType.buttonText.copy(fontSize = 10.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(color = SurfaceHigh, shape = CircleShape, modifier = Modifier.padding(start = 8.dp)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                        Text("SNAP: AUTO-EDGE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                    }
                }
            }

            // Phone Mockup area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E19))
            ) {
                // Background game scene
                Box(
                    modifier = Modifier.fillMaxSize().alpha(0.3f).background(SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Photo, null, Modifier.size(64.dp), OnSurfaceMuted)
                }

                // Safe Zone lines - Now fits the outer box exactly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp), 
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HUD SAFE ZONE 24PX", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Text("60.2 FPS", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("REC 00:14:32", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                            Text("4.8 KBPS", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp)
                        }
                    }
                }

                // Simulated Orb and Pill - Fixed alignment
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(idleOpacity)
                ) {
                    // 1. The Pill - Stuck flush to the extreme right edge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(6.dp, 24.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            .background(SecondaryContainer.copy(alpha = 0.8f))
                    )

                    // 2. The Orb - Floating with fixed padding from the right edge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 18.dp) // 6dp pill + 12dp space
                            .size(48.dp)
                            .background(Color(0xFF252A36).copy(alpha = 0.8f), CircleShape)
                            .shadow(16.dp, CircleShape, spotColor = orbColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(orbBrush, CircleShape)
                                .padding(1.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090E19), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(orbIcon, null, Modifier.size(20.dp), if (isCyberRed) CyberCyan else orbColor)
                                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(6.dp).background(if (isCyberRed) CyberCyan else orbColor, CircleShape))
                            }
                        }
                    }
                }

                Text(
                    text = "TAP ORB IN PREVIEW TO TEST HUD", 
                    style = PulseCastType.labelTelemetrySm, 
                    color = OnSurfaceMuted.copy(alpha = 0.4f), 
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun OpacityControls(idleOpacity: Float, onIdleChange: (Float) -> Unit, activeOpacity: Float, onActiveChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OpacitySliderCard("Idle Opacity", idleOpacity, Icons.Default.Opacity, SecondaryFixedDim, onIdleChange, Modifier.fillMaxWidth())
        OpacitySliderCard("Active Opacity", activeOpacity, Icons.Default.WbIncandescent, PrimaryContainer, onActiveChange, Modifier.fillMaxWidth())
    }
}

@Composable
private fun OpacitySliderCard(label: String, value: Float, icon: ImageVector, color: Color, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(icon, null, Modifier.size(16.dp), color)
                    Text(label, style = PulseCastType.labelTelemetryMd, color = OnSurface)
                }
                Text("${(value * 100).toInt()}%", style = PulseCastType.labelTelemetrySm, color = color)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = color, inactiveTrackColor = SurfaceHigh),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun DockingPhysicsSection(selectedMode: String, onModeSelect: (String) -> Unit) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Timer, null, Modifier.size(18.dp), NeonAmber)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-hide Inactivity Timeout", 
                            style = PulseCastType.buttonText,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Fades down to idle opacity during full screen play", 
                            style = PulseCastType.bodySm, 
                            color = OnSurfaceMuted,
                            lineHeight = 14.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 8.dp)) {
                    Surface(color = NeonAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("3.0s", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetryMd, color = NeonAmber)
                    }
                    Switch(checked = true, onCheckedChange = {}, modifier = Modifier.scale(0.8f))
                }
            }

            Text("SNAP-TO-EDGE DOCKING PRESET", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Magnetic" to Icons.Default.QrCode2, "Free Float" to Icons.Default.PanTool, "Half-Pill" to Icons.Default.VerticalAlignCenter).forEach { (mode, icon) ->
                    val isSelected = selectedMode == mode
                    Surface(
                        color = if (isSelected) SurfaceHigh else SurfaceMid,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).clickable { onModeSelect(mode) },
                        border = if (isSelected) BorderStroke(1.dp, PrimaryContainer) else null
                    ) {
                        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(icon, null, Modifier.size(20.dp), if (isSelected) PrimaryContainer else OnSurfaceMuted)
                            Text(
                                text = mode, 
                                style = PulseCastType.labelTelemetrySm, 
                                color = if (isSelected) OnSurface else OnSurfaceMuted, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
private fun GestureActionBinder() {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.TouchApp, null, Modifier.size(20.dp), PrimaryContainer)
                    Text(
                        text = "Gesture Action Binder", 
                        style = PulseCastType.buttonText.copy(fontSize = 13.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = SecondaryContainer.copy(alpha = 0.15f), 
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "ULTRA FAST <16MS", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                        style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), 
                        color = SecondaryFixedDim,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GestureItem("Single Tap", "Radial Menu", Icons.Default.AdsClick, SecondaryFixedDim, null, "Default primary trigger")
                GestureItem("Double Tap", "Instant 30s Clip", Icons.Default.AutoAwesome, PrimaryContainer, "AI CLIP", "Buffer last 30s replay")
                GestureItem("Long Press (0.8s)", "Mic Mute Toggle", Icons.Default.MicOff, NeonAmber, null, "Tactile haptic confirm")
                GestureItem("Swipe Inward", "Live Chat Drawer", Icons.Default.Forum, SecondaryFixedDim, null, "Edge flick motion")
            }
        }
    }
}

@Composable
private fun GestureItem(gesture: String, action: String, icon: ImageVector, color: Color, badge: String? = null, details: String) {
    Surface(color = SurfaceMid, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(20.dp), color) }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = gesture, 
                            style = PulseCastType.buttonText, 
                            color = OnSurface
                        )
                        if (badge != null) {
                            Surface(
                                color = color.copy(alpha = 0.15f), 
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = badge, 
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                                    style = PulseCastType.labelTelemetrySm, 
                                    color = color, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = details, 
                    style = PulseCastType.labelTelemetrySm.copy(fontSize = 11.sp), 
                    color = OnSurfaceMuted,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(Modifier.width(16.dp))

                Surface(
                    color = SurfaceHigh, 
                    shape = RoundedCornerShape(8.dp), 
                    modifier = Modifier.clickable { },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), 
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = action, 
                            style = PulseCastType.labelTelemetryMd, 
                            color = color
                        )
                        Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp), OnSurfaceMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadialQuickMenuSlots() {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Radial Quick-Menu Slots", style = PulseCastType.buttonText)
                    Text("Active modules pinned to 360° orb \nfan-out", maxLines = 2, style = PulseCastType.bodySm, color = OnSurfaceMuted)
                }
                TextButton(onClick = { }) {
                    Icon(Icons.Default.EditAttributes, null, Modifier.size(16.dp), PrimaryContainer)
                    Spacer(Modifier.width(4.dp))
                    Text("Edit Slots", color = PrimaryContainer, style = PulseCastType.buttonText)
                }
            }

            // Container for Eclipse Effect and Radial Graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E19)),
                contentAlignment = Alignment.Center
            ) {
                // 1. Red Glow Box behind Center Orb (Eclipse effect) - Adjust blur radius here as needed
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .background(PrimaryContainer.copy(alpha = 0.6f), CircleShape)
                            .blur(44.dp) // Red circle glow blur radius (adjustable)
                    )
                }

                // 2. Layer: Radial Graphic Box with clean glassmorphic background (keeps children sharp)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceHigh.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha =.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Coordinate Rings (Simulated with Canvas)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        drawCircle(Color(0xFF5D3F40), radius = 100.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
                        drawCircle(Color(0xFF00DAF3).copy(alpha = 0.2f), radius = 60.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                        drawLine(Color(0xFF5D3F40).copy(alpha = 0.3f), start = Offset(0f, centerY), end = Offset(size.width, centerY))
                        drawLine(Color(0xFF5D3F40).copy(alpha = 0.3f), start = Offset(centerX, 0f), end = Offset(centerX, size.height))
                    }

                    // Center Orb (Eclipse appearance on top of glow)
                    Surface(
                        color = SurfaceHigh, 
                        shape = CircleShape, 
                        modifier = Modifier.size(60.dp), 
                        shadowElevation = 16.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(24.dp), PrimaryContainer)
                            Text("CORE", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Slots
                    val slots = listOf(
                        Icons.Default.PauseCircle to "Rec/Pause",
                        Icons.Default.PhotoCamera to "Screen",
                        Icons.AutoMirrored.Filled.BrandingWatermark to "Facecam",
                        Icons.Default.Draw to "Doodle",
                        Icons.Default.GraphicEq to "SFX Pad",
                        Icons.Default.MonitorHeart to "Health HUD"
                    )

                    slots.forEachIndexed { index, (icon, label) ->
                        val angle = (index * 60 - 90) * (Math.PI / 180).toFloat()
                        val radius = 100.dp
                        
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (radius.value * cos(angle.toDouble())).dp,
                                    y = (radius.value * sin(angle.toDouble())).dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = if (index == 0) PrimaryContainer else SurfaceHigh,
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp),
                                    shadowElevation = 8.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, null, Modifier.size(20.dp), if (index == 0) Color.Black else OnSurface)
                                    }
                                }
                                Surface(color = SurfaceHigh.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    Text("${index + 1}. $label", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrbVisualSkins(selectedSkin: String, onSkinSelect: (String) -> Unit) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Palette, null, Modifier.size(20.dp), NeonAmber)
                    Text("Orb Visual Skins", style = PulseCastType.buttonText)
                }
                Text("4 STYLES", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { SkinCard("Cyber Red", Icons.Default.Adjust, PrimaryContainer, selectedSkin == "Cyber Red", glowColor = ElectricRuby, onSelect = { onSkinSelect("Cyber Red") }) }
                item { SkinCard("Stealth Carbon", Icons.Default.RadioButtonUnchecked, OnSurfaceMuted, selectedSkin == "Stealth Carbon", "MATTE FINISH", glowColor = OnSurfaceMuted, onSelect = { onSkinSelect("Stealth Carbon") }) }
                item { SkinCard("Apex Gold", Icons.Default.MilitaryTech, NeonAmber, selectedSkin == "Apex Gold", "METALLIC", true, glowColor = NeonAmber, onSelect = { onSkinSelect("Apex Gold") }) }
                item { SkinCard("Cyan Tron", Icons.Default.Grid4x4, SecondaryFixedDim, selectedSkin == "Cyan Tron", "HOLO GRID", true, glowColor = SecondaryFixedDim, onSelect = { onSkinSelect("Cyan Tron") }) }
            }
        }
    }
}

@Composable
private fun SkinCard(name: String, icon: ImageVector, color: Color, isCurrent: Boolean, status: String = "CURRENT", isPro: Boolean = false, glowColor: Color? = null, onSelect: () -> Unit) {
    val isCyberRed = name == "Cyber Red"
    val resolvedGlowColor = glowColor ?: color
    Surface(
        color = if (isCurrent) SurfaceMid else SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(130.dp).clickable { onSelect() },
        border = if (isCurrent) BorderStroke(2.dp, color) else BorderStroke(1.dp, SurfaceHigh),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SurfaceLow, 
                    shape = CircleShape, 
                    modifier = Modifier.size(56.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Layer 1: Solid Glow behind icon circle (matching Center Orb eclipse effect, no gradient)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(resolvedGlowColor.copy(alpha = 0.65f), CircleShape)
                                .blur(12.dp)
                        )

                        // Black/Dark inner core circle with glowing border (Center Orb eclipse style)
                        if (isCyberRed) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Brush.linearGradient(listOf(ElectricRuby, CyberCyan)), CircleShape)
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF090E19), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, Modifier.size(20.dp), CyberCyan)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(resolvedGlowColor, CircleShape)
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF090E19), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, Modifier.size(20.dp), resolvedGlowColor)
                                }
                            }
                        }
                    }
                }
                Text(
                    text = name, 
                    style = PulseCastType.buttonText, 
                    color = OnSurface, 
                    textAlign = TextAlign.Center, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = if (isCurrent) "CURRENT" else status, 
                    style = PulseCastType.labelTelemetrySm, 
                    color = if (isCurrent) color else OnSurfaceMuted, 
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Bold, 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (isPro) {
                Surface(
                    color = NeonAmber, 
                    shape = RoundedCornerShape(4.dp), 
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4.dp))
                ) {
                    Text(
                        text = "PRO", 
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), 
                        style = PulseCastType.labelTelemetrySm, 
                        fontSize = 7.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomActionArea() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                Text("Apply Orb Configuration", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.BookmarkAdd, null, Modifier.size(18.dp), SecondaryFixedDim)
                Text("Save as Gamer Preset", style = PulseCastType.buttonText)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun FloatingBallPreview() {
    PulseCastTheme {
        FloatingBallCustomizationScreen()
    }
}
