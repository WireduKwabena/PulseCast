package com.masterminds.pulsecast.ui.facecam_chroma_key_studio

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun FacecamChromaKeyStudioScreen(
    onBack: () -> Unit = {},
    onApply: () -> Unit = {}
) {
    // Stage Controls
    var isMirrored by remember { mutableStateOf(true) }
    var isFrontCam by remember { mutableStateOf(true) }
    var isExposureLocked by remember { mutableStateOf(true) }

    // Frame Geometry & Mask
    var selectedShape by remember { mutableStateOf("16:9 Wide") }
    var pipScalePct by remember { mutableFloatStateOf(25f) }
    var selectedDock by remember { mutableStateOf("Top-R") }

    // Chroma Key Engine
    var chromaEnabled by remember { mutableStateOf(true) }
    var selectedHue by remember { mutableStateOf("Studio Green") }
    var similarityPct by remember { mutableFloatStateOf(42f) }
    var smoothnessPct by remember { mutableFloatStateOf(18f) }
    var spillSuppressionPct by remember { mutableFloatStateOf(65f) }
    var rimAuraEnabled by remember { mutableStateOf(true) }

    // RGB Frame & Border
    var selectedBorderStyle by remember { mutableStateOf("Cyber Neon") }
    var strokeThicknessPx by remember { mutableFloatStateOf(3.0f) }
    var audioVisualizerRim by remember { mutableStateOf(true) }

    // Retouch & Lighting
    var skinSofteningPct by remember { mutableFloatStateOf(35f) }
    var ringLightFillPct by remember { mutableFloatStateOf(15f) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = BgBase,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Telemetry Sub-bar
            HeaderTelemetryStrip(onBack = onBack)

            // Main Interactive Viewfinder / Live Feed Stage
            InteractiveViewfinderStage(
                isMirrored = isMirrored,
                onMirrorToggle = { isMirrored = !isMirrored },
                isFrontCam = isFrontCam,
                onCamFlip = { isFrontCam = !isFrontCam },
                isExposureLocked = isExposureLocked,
                onLockToggle = { isExposureLocked = !isExposureLocked },
                selectedShape = selectedShape,
                pipScalePct = pipScalePct,
                selectedDock = selectedDock,
                chromaEnabled = chromaEnabled,
                selectedBorderStyle = selectedBorderStyle
            )

            // Section 1: Frame Geometry & Mask
            FrameGeometryMaskSection(
                selectedShape = selectedShape,
                onShapeSelect = { selectedShape = it },
                pipScalePct = pipScalePct,
                onScaleChange = { pipScalePct = it },
                selectedDock = selectedDock,
                onDockSelect = { selectedDock = it }
            )

            // Section 2: Virtual Chroma Key & AI Background Removal
            ChromaKeyEngineSection(
                chromaEnabled = chromaEnabled,
                onChromaToggle = { chromaEnabled = it },
                selectedHue = selectedHue,
                onHueSelect = { selectedHue = it },
                similarityPct = similarityPct,
                onSimilarityChange = { similarityPct = it },
                smoothnessPct = smoothnessPct,
                onSmoothnessChange = { smoothnessPct = it },
                spillSuppressionPct = spillSuppressionPct,
                onSpillChange = { spillSuppressionPct = it },
                rimAuraEnabled = rimAuraEnabled,
                onRimAuraToggle = { rimAuraEnabled = it }
            )

            // Section 3: Animated RGB Frame & Border Customization
            RgbBorderTuningSection(
                selectedBorderStyle = selectedBorderStyle,
                onBorderStyleSelect = { selectedBorderStyle = it },
                strokeThicknessPx = strokeThicknessPx,
                onThicknessChange = { strokeThicknessPx = it },
                audioVisualizerRim = audioVisualizerRim,
                onVisualizerToggle = { audioVisualizerRim = it }
            )

            // Section 4: Beauty Filters & Studio Lighting Balance
            LightingRetouchSection(
                skinSofteningPct = skinSofteningPct,
                onSkinSofteningChange = { skinSofteningPct = it },
                ringLightFillPct = ringLightFillPct,
                onRingLightChange = { ringLightFillPct = it }
            )

            // Bottom Persistent CTA Section
            BottomActionSection(
                onReset = {
                    selectedShape = "16:9 Wide"
                    pipScalePct = 25f
                    selectedDock = "Top-R"
                    chromaEnabled = true
                    similarityPct = 42f
                    smoothnessPct = 18f
                    spillSuppressionPct = 65f
                    selectedBorderStyle = "Cyber Neon"
                    strokeThicknessPx = 3.0f
                },
                onApply = {
                    onApply()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderTelemetryStrip(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", tint = OnSurface, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Text("CAMERA PIP ENGINE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Facecam & Chroma Studio", style = PulseCastType.headlineSm, color = OnSurface)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = SurfaceHigh, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                    }
                }
                Surface(color = SurfaceHigh, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Sub-bar Telemetry
        Surface(
            color = SurfaceLow,
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
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                    Text("HARDWARE ACCELERATED • GPU VULKAN 3.2", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Text("DSP LATENCY: 3.4ms", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InteractiveViewfinderStage(
    isMirrored: Boolean,
    onMirrorToggle: () -> Unit,
    isFrontCam: Boolean,
    onCamFlip: () -> Unit,
    isExposureLocked: Boolean,
    onLockToggle: () -> Unit,
    selectedShape: String,
    pipScalePct: Float,
    selectedDock: String,
    chromaEnabled: Boolean,
    selectedBorderStyle: String
) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Viewport Stage Area (4:3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1524)),
                contentAlignment = Alignment.Center
            ) {
                // Game Preview Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = size.width / 16
                    val stroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                    repeat(16) { i ->
                        drawLine(SecondaryFixedDim.copy(alpha = 0.1f), Offset(i * step, 0f), Offset(i * step, size.height), strokeWidth = stroke.width)
                    }
                }

                Text("APEX MOBILE // RANKED LOBBY", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted.copy(alpha = 0.4f), fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))

                // Floating Dynamic PIP Node Container
                val pipAlignment = when (selectedDock) {
                    "Top-L" -> Alignment.TopStart
                    "Btm-L" -> Alignment.BottomStart
                    "Btm-R" -> Alignment.BottomEnd
                    "Free" -> Alignment.Center
                    else -> Alignment.TopEnd
                }

                val borderBrush = when (selectedBorderStyle) {
                    "Ranked Master" -> Brush.linearGradient(listOf(NeonAmber, Color(0xFFD97706)))
                    "Rainbow Wave" -> Brush.linearGradient(listOf(PrimaryContainer, SecondaryFixedDim, NeonAmber, PrimaryContainer))
                    "Clean Minimal" -> Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                    else -> Brush.linearGradient(listOf(PrimaryContainer, SecondaryFixedDim)) // Cyber Neon
                }

                val shapeRadius = when (selectedShape) {
                    "Circle" -> CircleShape
                    "Hexagon" -> RoundedCornerShape(16.dp)
                    "Diamond" -> RoundedCornerShape(20.dp)
                    else -> RoundedCornerShape(10.dp)
                }

                Surface(
                    color = Color(0xFF090E19),
                    shape = shapeRadius,
                    border = BorderStroke(2.dp, borderBrush),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .align(pipAlignment)
                        .padding(8.dp)
                        .size(width = (100 * (pipScalePct / 25f)).dp, height = (70 * (pipScalePct / 25f)).dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(28.dp))

                        if (chromaEnabled) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                            ) {
                                Text("CHROMA ACTIVE", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 6.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(1.dp).fillMaxWidth())
                            }
                        }

                        Icon(Icons.Default.OpenWith, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.align(Alignment.TopStart).padding(2.dp).size(10.dp))
                    }
                }

                // Telemetry Badges Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(color = Color(0xFF090E19).copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp)) {
                        Text(if (isFrontCam) "FRONT ULTRA-WIDE 60 FPS" else "REAR MAIN 60 FPS", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color(0xFF090E19).copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp)) {
                        Text(if (isExposureLocked) "EXPOSURE: LOCKED" else "EXPOSURE: AUTO", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Action Toolstrip under live preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickToolButton("Flip Cam", Icons.Default.Cameraswitch, SecondaryFixedDim, onCamFlip, Modifier.weight(1f))
                QuickToolButton(if (isMirrored) "Mirrored" else "Normal", Icons.Default.Flip, SecondaryFixedDim, onMirrorToggle, Modifier.weight(1f))
                QuickToolButton(if (isExposureLocked) "Locked" else "Auto", if (isExposureLocked) Icons.Default.Lock else Icons.Default.LockOpen, NeonAmber, onLockToggle, Modifier.weight(1f))
                QuickToolButton("Inspect", Icons.Default.Fullscreen, PrimaryContainer, {}, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickToolButton(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceHigh,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FrameGeometryMaskSection(
    selectedShape: String,
    onShapeSelect: (String) -> Unit,
    pipScalePct: Float,
    onScaleChange: (Float) -> Unit,
    selectedDock: String,
    onDockSelect: (String) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                    Text("Frame Geometry & Mask", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text(selectedShape.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Shape Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Circle", "Hexagon", "16:9 Wide", "Diamond", "AI Cutout").forEach { shape ->
                    val isSelected = selectedShape == shape
                    Surface(
                        color = if (isSelected) PrimaryContainer else SurfaceHigh,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShapeSelect(shape) }
                    ) {
                        Text(
                            text = shape,
                            modifier = Modifier.padding(vertical = 6.dp),
                            style = PulseCastType.labelTelemetrySm,
                            color = if (isSelected) Color.Black else OnSurfaceMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // PIP Size Slider
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("PIP Scale Factor", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text("${pipScalePct.toInt()}%", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = pipScalePct,
                    onValueChange = onScaleChange,
                    valueRange = 15f..45f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                    modifier = Modifier.height(18.dp)
                )
            }

            // Magnetic Dock Presets
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Magnetic HUD Screen Dock", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Top-L", "Top-R", "Btm-L", "Btm-R", "Free").forEach { dock ->
                        val isSelected = selectedDock == dock
                        Surface(
                            color = if (isSelected) SecondaryFixedDim else SurfaceHigh,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDockSelect(dock) }
                        ) {
                            Text(
                                text = dock,
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
private fun ChromaKeyEngineSection(
    chromaEnabled: Boolean,
    onChromaToggle: (Boolean) -> Unit,
    selectedHue: String,
    onHueSelect: (String) -> Unit,
    similarityPct: Float,
    onSimilarityChange: (Float) -> Unit,
    smoothnessPct: Float,
    onSmoothnessChange: (Float) -> Unit,
    spillSuppressionPct: Float,
    onSpillChange: (Float) -> Unit,
    rimAuraEnabled: Boolean,
    onRimAuraToggle: (Boolean) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Brush, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Chroma Key Engine", style = PulseCastType.buttonText, color = OnSurface)
                        Text("Hardware-accelerated color subtraction", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }
                Switch(checked = chromaEnabled, onCheckedChange = onChromaToggle, modifier = Modifier.scale(0.8f))
            }

            if (chromaEnabled) {
                // Target Screen Hue Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Studio Green" to SignalGreen,
                        "Ultramarine" to Color(0xFF0066FF),
                        "Eyedrop" to NeonAmber
                    ).forEach { (hue, color) ->
                        val isSelected = selectedHue == hue
                        Surface(
                            color = if (isSelected) SurfaceHigh else Color(0xFF090E19),
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) BorderStroke(1.dp, color) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onHueSelect(hue) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(Modifier.size(12.dp).clip(CircleShape).background(color))
                                Text(hue, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                // Precision Sliders
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChromaSliderRow("Similarity & Tolerance", similarityPct, onSimilarityChange)
                    ChromaSliderRow("Edge Smoothness & Feather", smoothnessPct, onSmoothnessChange)
                    ChromaSliderRow("Color Spill Suppression", spillSuppressionPct, onSpillChange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Electric Crimson Rim Aura", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                        Text("Injects cyber backlight along body contour", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                    Switch(checked = rimAuraEnabled, onCheckedChange = onRimAuraToggle, modifier = Modifier.scale(0.7f))
                }
            }
        }
    }
}

@Composable
private fun ChromaSliderRow(title: String, valuePct: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
            Text("${valuePct.toInt()}%", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = valuePct,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
            modifier = Modifier.height(16.dp)
        )
    }
}

@Composable
private fun RgbBorderTuningSection(
    selectedBorderStyle: String,
    onBorderStyleSelect: (String) -> Unit,
    strokeThicknessPx: Float,
    onThicknessChange: (Float) -> Unit,
    audioVisualizerRim: Boolean,
    onVisualizerToggle: (Boolean) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                    Text("RGB Frame & Border Tuning", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("NEON AURA", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Border Style Cards
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Cyber Neon", "Clean Minimal", "Rainbow Wave", "Ranked Master").chunked(2).forEach { rowStyles ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowStyles.forEach { style ->
                            val isSelected = selectedBorderStyle == style
                            Surface(
                                color = if (isSelected) SurfaceHigh else Color(0xFF090E19),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) BorderStroke(1.dp, PrimaryContainer) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onBorderStyleSelect(style) }
                            ) {
                                Text(style, modifier = Modifier.padding(vertical = 8.dp), style = PulseCastType.labelTelemetrySm, color = if (isSelected) PrimaryContainer else OnSurfaceMuted, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Stroke Thickness Slider
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stroke Thickness", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    Text(String.format(Locale.US, "%.1f px", strokeThicknessPx), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = strokeThicknessPx,
                    onValueChange = onThicknessChange,
                    valueRange = 0f..8f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                    modifier = Modifier.height(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mic Audio Visualizer Rim", style = PulseCastType.buttonText, color = OnSurface, fontSize = 11.sp)
                    Text("Border pulses in real-time with voice dynamics", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                }
                Switch(checked = audioVisualizerRim, onCheckedChange = onVisualizerToggle, modifier = Modifier.scale(0.7f))
            }
        }
    }
}

@Composable
private fun LightingRetouchSection(
    skinSofteningPct: Float,
    onSkinSofteningChange: (Float) -> Unit,
    ringLightFillPct: Float,
    onRingLightChange: (Float) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Flare, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                    Text("Studio Lighting & Retouch", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("NEURAL SHADERS", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            ChromaSliderRow("Skin Softening & Blemish Blur", skinSofteningPct, onSkinSofteningChange)
            ChromaSliderRow("Virtual Ring-Light Frontal Fill", ringLightFillPct, onRingLightChange)
        }
    }
}

@Composable
private fun BottomActionSection(onReset: () -> Unit, onApply: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(90.dp).height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Reset", style = PulseCastType.buttonText, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Apply to Live HUD", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Text("Profile saved to Vulkan Framebuffer Slot #1", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun FacecamChromaKeyStudioPreview() {
    PulseCastTheme {
        FacecamChromaKeyStudioScreen()
    }
}
