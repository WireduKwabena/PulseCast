package com.masterminds.pulsecast.ui.capturehud

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masterminds.pulsecast.ui.ui_library.PulseCard
import com.masterminds.pulsecast.ui.ui_library.PulsePill
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.theme.BgBase
import com.masterminds.pulsecast.ui.theme.CyberCyan
import com.masterminds.pulsecast.ui.theme.ElectricRuby
import com.masterminds.pulsecast.ui.theme.NeonAmber
import com.masterminds.pulsecast.ui.theme.OnSurface
import com.masterminds.pulsecast.ui.theme.OnSurfaceMuted
import com.masterminds.pulsecast.ui.theme.PulseCastSpacing
import com.masterminds.pulsecast.ui.theme.PulseCastType
import com.masterminds.pulsecast.ui.theme.SurfaceMid

/**
 * The primary/default screen (Image 2 in the design set) — capture
 * config, start-recording trigger, target presets, and recent recordings
 * ("Studio Vault"). Structurally faithful to the mockup; NOT claiming
 * pixel-perfect fidelity on things I can't visually verify here (the
 * circular storage progress ring is a plain arc, not the exact
 * gradient/glow treatment shown; the record button doesn't yet have the
 * pulsing glow animation DESIGN.md specifies) — those are real follow-up
 * items once this can actually be seen running.
 */
@Composable
fun RecordScreen(
    onStartRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PulseCastSpacing.base, vertical = PulseCastSpacing.base),
        verticalArrangement = Arrangement.spacedBy(PulseCastSpacing.base),
    ) {
        AppHeader()
        CaptureEngineCard()
        StartRecordingButton(onClick = onStartRecording)
        TargetPresetsRow()
        QuickTogglesRow()
        StudioVaultSection()
        Spacer(Modifier.height(PulseCastSpacing.safeBottomNav))
    }
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PulseCast Studio", style = PulseCastType.headlineSm, color = OnSurface)
                Spacer(Modifier.width(PulseCastSpacing.xs))
                PulsePill(text = "PRO", containerColor = NeonAmber, contentColor = BgBase)
            }
            Text(
                "128 GB Free  •  ~18h 45m",
                style = PulseCastType.bodySm,
                color = OnSurfaceMuted,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ElectricRuby, CircleShape),
        )
    }
}

@Composable
private fun CaptureEngineCard() {
    PulseCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ElectricRuby, CircleShape),
                    )
                    Spacer(Modifier.width(PulseCastSpacing.xs))
                    Text(
                        "CAPTURE ENGINE READY",
                        style = PulseCastType.labelTelemetrySm,
                        color = OnSurface,
                    )
                }
                PulsePill(text = "⚡ LOW LATENCY", containerColor = SurfaceMid, contentColor = CyberCyan)
            }

            Spacer(Modifier.height(PulseCastSpacing.base))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                    CircularProgressIndicator(
                        progress = { 0.74f },
                        modifier = Modifier.fillMaxSize(),
                        color = CyberCyan,
                        trackColor = SurfaceMid,
                        strokeWidth = 6.dp,
                    )
                    Text("74%", style = PulseCastType.labelTelemetryMd, color = OnSurface)
                }

                Spacer(Modifier.width(PulseCastSpacing.base))

                Column(verticalArrangement = Arrangement.spacedBy(PulseCastSpacing.xs)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(PulseCastSpacing.xs)) {
                        PulsePill(text = "2K QHD")
                        PulsePill(text = "60 FPS")
                    }
                    PulsePill(text = "24 Mbps CBR")
                    Text(
                        "112 GB Free • 18.4 hrs remaining",
                        style = PulseCastType.bodySm,
                        color = OnSurfaceMuted,
                    )
                }
            }

            Spacer(Modifier.height(PulseCastSpacing.base))
            AudioMixerRow()
        }
    }
}

@Composable
private fun AudioMixerRow() {
    var systemGainDb by remember { mutableFloatStateOf(-8f) }
    var micGainDb by remember { mutableFloatStateOf(-3f) }
    var internalPlusMic by remember { mutableStateOf(true) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Dual Stream Audio", style = PulseCastType.bodyMd, color = OnSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Internal + Mic", style = PulseCastType.bodySm, color = CyberCyan)
                Switch(checked = internalPlusMic, onCheckedChange = { internalPlusMic = it })
            }
        }
        Spacer(Modifier.height(PulseCastSpacing.sm))
        GainSlider(label = "SYSTEM/GAME", valueDb = systemGainDb, onValueChange = { systemGainDb = it })
        Spacer(Modifier.height(PulseCastSpacing.sm))
        GainSlider(label = "VOICE MIC", valueDb = micGainDb, onValueChange = { micGainDb = it })
    }
}

@Composable
private fun GainSlider(label: String, valueDb: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            Text("$valueDb dB", style = PulseCastType.labelTelemetrySm, color = OnSurface)
        }
        // Dual-zone color grading (cyan -> amber -> ruby at clipping) per
        // DESIGN.md's Fader Strips spec would need a custom Slider track
        // drawn with Canvas — using Material3's default colored Slider
        // here as a structural placeholder for that.
        Slider(
            value = valueDb,
            onValueChange = onValueChange,
            valueRange = -24f..0f,
            colors = SliderDefaults.colors(activeTrackColor = CyberCyan, thumbColor = Color.White),
        )
    }
}

@Composable
private fun StartRecordingButton(onClick: () -> Unit) {
    PulseCard(modifier = Modifier.fillMaxWidth(), shape = PulseCastShapes.md) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Countdown: 3s", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                Text("Auto-Stop 60m", style = PulseCastType.bodySm, color = OnSurfaceMuted)
            }
            Spacer(Modifier.height(PulseCastSpacing.sm))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = PulseCastShapes.full,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby),
            ) {
                Icon(Icons.Filled.FiberManualRecord, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(PulseCastSpacing.xs))
                Text("Start Screen Recording", style = PulseCastType.buttonText, color = Color.Black)
            }
            Spacer(Modifier.height(PulseCastSpacing.xs))
            Text(
                "Tap to capture high bitrate session. Overlay badge appears automatically.",
                style = PulseCastType.bodySm,
                color = OnSurfaceMuted,
            )
        }
    }
}

private data class Preset(val name: String, val detail: String)

@Composable
private fun TargetPresetsRow() {
    val presets = listOf(
        Preset("Pro Gaming", "1440p • 60fps • 32M"),
        Preset("Tutorial Cast", "1080p • Brush Mic"),
        Preset("Reaction", "Dual Cam • Chroma"),
    )
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Target Presets", style = PulseCastType.bodyMd, color = OnSurface)
            Text("CUSTOMIZE", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
        }
        Spacer(Modifier.height(PulseCastSpacing.sm))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PulseCastSpacing.sm),
        ) {
            presets.forEach { preset ->
                PulseCard(modifier = Modifier.width(140.dp)) {
                    Column {
                        Text(preset.name, style = PulseCastType.bodyMd, color = OnSurface)
                        Spacer(Modifier.height(PulseCastSpacing.xxs))
                        Text(preset.detail, style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTogglesRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(PulseCastSpacing.sm), modifier = Modifier.fillMaxWidth()) {
        listOf(
            Triple("Region", "Entire Screen", "FULL"),
            Triple("Facecam PIP", "Circle • Front", "ON"),
            Triple("Float Ball", "Auto Hide", "ON"),
        ).forEach { (title, detail, badge) ->
            PulseCard(modifier = Modifier.weight(1f)) {
                Column {
                    PulsePill(text = badge, containerColor = SurfaceMid, contentColor = CyberCyan)
                    Spacer(Modifier.height(PulseCastSpacing.xs))
                    Text(title, style = PulseCastType.bodySm, color = OnSurface)
                    Text(detail, style = PulseCastType.bodySm, color = OnSurfaceMuted)
                }
            }
        }
    }
}

private data class VaultItem(val title: String, val app: String, val size: String, val audio: String)

@Composable
private fun StudioVaultSection() {
    val items = listOf(
        VaultItem("Final Ring Ranked Clutch", "Apex Mobile", "420 MB", "Stereo Audio"),
        VaultItem("Configuring OBS Multi-Stream", "App Walkthrough", "890 MB", "Mic Only"),
    )
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Studio Vault", style = PulseCastType.bodyMd, color = OnSurface)
            Text("VIEW ALL (14)", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
        }
        Spacer(Modifier.height(PulseCastSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(PulseCastSpacing.sm)) {
            items.forEach { item ->
                PulseCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(item.title, style = PulseCastType.bodyMd, color = OnSurface)
                        Text("${item.app} • ${item.size} • ${item.audio}", style = PulseCastType.bodySm, color = OnSurfaceMuted)
                    }
                }
            }
        }
    }
}
