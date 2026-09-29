package com.masterminds.pulsecast.ui.pro_multi_track_audio_mixer_DMCA_shield

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterminds.pulsecast.ui.theme.*
import java.util.Locale

@Composable
fun ProMultiTrackAudioMixerScreen(
    viewModel: AudioMixerViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val channels by viewModel.channels.collectAsState()
    val masterGainDb by viewModel.masterGainDb.collectAsState()
    val dmcaShieldEnabled by viewModel.dmcaShieldEnabled.collectAsState()
    val smartDuckingEnabled by viewModel.smartDuckingEnabled.collectAsState()
    val smartDuckingThreshold by viewModel.smartDuckingThreshold.collectAsState()

    var masterMuted by remember { mutableStateOf(false) }
    var masterVolumePct by remember { mutableFloatStateOf(92f) }
    var activeSfxIndex by remember { mutableStateOf(-1) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.profileApplied.collect {
            snackbarHostState.showSnackbar("Audio DSP Profile Synchronized")
        }
    }

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
            // Screen Header Sub-strip
            HeaderSubStrip()

            // Master Broadcast Output Card & Dual VU Segmented Meters
            MasterOutputCard(
                masterGainDb = masterGainDb,
                masterVolumePct = masterVolumePct,
                onMasterVolumeChange = { masterVolumePct = it; viewModel.setMasterGain((it / 100f - 0.5f) * 20f) },
                masterMuted = masterMuted,
                onMasterMuteToggle = { masterMuted = !masterMuted }
            )

            // Multi-Track Audio Channels Header
            ChannelStripsHeader()

            // Live Channel Strips
            ChannelStrips(
                channels = channels,
                smartDuckingEnabled = smartDuckingEnabled,
                smartDuckingThreshold = smartDuckingThreshold,
                onGainChange = { index, gain -> viewModel.setChannelGain(index, gain) },
                onPanChange = { index, pan -> viewModel.setChannelPan(index, pan) },
                onMuteToggle = { index -> viewModel.toggleChannelMute(index) },
                onDuckingToggle = { viewModel.toggleSmartDucking() },
                onDuckingThresholdChange = { viewModel.setDuckingThreshold(it) }
            )

            // DMCA Shield Routing Matrix
            DmcaShieldMatrixCard(
                dmcaShieldEnabled = dmcaShieldEnabled,
                onDmcaToggle = { viewModel.toggleDmcaShield() }
            )

            // SFX Soundboard Quick Deck
            SfxQuickDeck(
                activeSfxIndex = activeSfxIndex,
                onSfxClick = { activeSfxIndex = it }
            )

            // Profile & Preset Action Bar
            BottomActionBar(
                onApply = { viewModel.applyProfile() }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSubStrip() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SecondaryFixedDim)
                )
                Text(
                    text = "AUDIO ENGINE & DSP CORE",
                    style = PulseCastType.labelTelemetrySm,
                    color = SecondaryFixedDim,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "4.2 ms LATENCY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = PulseCastType.labelTelemetrySm,
                    color = SecondaryFixedDim,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "24-Bit / 48kHz PCM Floating Point",
                style = PulseCastType.labelTelemetrySm,
                color = OnSurfaceMuted
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                Text(
                    text = "DMCA Filter Hardware Mode",
                    style = PulseCastType.labelTelemetrySm,
                    color = PrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MasterOutputCard(
    masterGainDb: Float,
    masterVolumePct: Float,
    onMasterVolumeChange: (Float) -> Unit,
    masterMuted: Boolean,
    onMasterMuteToggle: () -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Master Output Bus", style = PulseCastType.headlineSm, color = OnSurface)
                        Text("L/R Broadcast Stream Target", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "CLIP LIMITER",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = PulseCastType.labelTelemetrySm,
                            color = OnSurfaceMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f dB", masterGainDb - 1.2f),
                        style = PulseCastType.labelTelemetryLg,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dual Stereo LED Peak VU Bars
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E19), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VuBarRow("L", activeLEDs = 20)
                VuBarRow("R", activeLEDs = 17)

                // Decibel Scale markings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("-∞", "-24", "-12", "-6", "-3", "0", "+6 dB").forEach { mark ->
                        Text(
                            text = mark,
                            style = PulseCastType.labelTelemetrySm,
                            color = if (mark == "+6 dB") PrimaryContainer else OnSurfaceMuted,
                            fontSize = 8.sp,
                            fontWeight = if (mark == "+6 dB") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Master Fader & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = if (masterMuted) ErrorContainer else SurfaceHigh,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onMasterMuteToggle() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (masterMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Master Mute",
                            tint = if (masterMuted) Color.White else OnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Master Broadcast Fader", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        Text("${masterVolumePct.toInt()}%", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = masterVolumePct,
                        onValueChange = onMasterVolumeChange,
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = PrimaryContainer, inactiveTrackColor = SurfaceHigh),
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VuBarRow(label: String, activeLEDs: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(12.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(24) { index ->
                val ledColor = when {
                    index >= 20 -> PrimaryContainer
                    index >= 16 -> NeonAmber
                    else -> SecondaryFixedDim
                }
                val isLit = index < activeLEDs
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isLit) ledColor else SurfaceMid.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun ChannelStripsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Equalizer, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
            Text("Live Channel Strips", style = PulseCastType.buttonText, color = OnSurface)
        }
        Surface(
            color = SurfaceHigh,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("4 TRACKS DIRECT", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ChannelStrips(
    channels: List<ChannelState>,
    smartDuckingEnabled: Boolean,
    smartDuckingThreshold: Float,
    onGainChange: (Int, Float) -> Unit,
    onPanChange: (Int, Float) -> Unit,
    onMuteToggle: (Int) -> Unit,
    onDuckingToggle: () -> Unit,
    onDuckingThresholdChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Track 1: Game Audio
        if (channels.isNotEmpty()) {
            val game = channels[0]
            ChannelStripCard(
                name = "Game Audio",
                subtitle = "Loopback DSP Target",
                icon = Icons.Default.SportsEsports,
                badge = "HW LOOP",
                color = SecondaryFixedDim,
                muted = game.muted,
                onMuteToggle = { onMuteToggle(0) }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = SecondaryFixedDim.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text("FPS Bass Boost / Footsteps", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                        Text("High-Pass 80Hz", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                    }
                }
                VolAndPanControls(
                    gainDb = game.gainDb,
                    pan = game.pan,
                    color = SecondaryFixedDim,
                    onGainChange = { onGainChange(0, it) },
                    onPanChange = { onPanChange(0, it) }
                )
            }
        }

        // Track 2: Streamer Mic
        if (channels.size > 1) {
            val mic = channels[1]
            ChannelStripCard(
                name = "Streamer Mic",
                subtitle = "Studio Voice (Cardioid USB-C)",
                icon = Icons.Default.Mic,
                badge = "SOLO",
                color = PrimaryContainer,
                muted = mic.muted,
                onMuteToggle = { onMuteToggle(1) }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = PrimaryContainer.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text("AI De-Noise Active", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                        Text("Gate -42dB", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
                    }
                    Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                        Text("Compressor 3:1", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp)
                    }
                }
                VolAndPanControls(
                    gainDb = mic.gainDb,
                    pan = mic.pan,
                    color = PrimaryContainer,
                    onGainChange = { onGainChange(1, it) },
                    onPanChange = { onPanChange(1, it) }
                )
            }
        }

        // Track 3: Discord / Voice Comms
        if (channels.size > 2) {
            val discord = channels[2]
            ChannelStripCard(
                name = "Discord Comms",
                subtitle = "Party Audio • Auto-Duck Target",
                icon = Icons.Default.Forum,
                badge = "TRACK 3",
                color = NeonAmber,
                muted = discord.muted,
                onMuteToggle = { onMuteToggle(2) }
            ) {
                // Auto-Ducking Module
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LowPriority, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(14.dp))
                                Text("Smart Voice Ducking", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("-40% ON MIC", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = smartDuckingEnabled,
                                    onCheckedChange = { onDuckingToggle() },
                                    modifier = Modifier.scale(0.7f)
                                )
                            }
                        }
                        if (smartDuckingEnabled) {
                            Slider(
                                value = smartDuckingThreshold,
                                onValueChange = onDuckingThresholdChange,
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SecondaryFixedDim, inactiveTrackColor = SurfaceHigh),
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }
                }

                VolAndPanControls(
                    gainDb = discord.gainDb,
                    pan = discord.pan,
                    color = NeonAmber,
                    onGainChange = { onGainChange(2, it) },
                    onPanChange = { onPanChange(2, it) }
                )
            }
        }

        // Track 4: Music & Media (Protected Track)
        if (channels.size > 3) {
            val music = channels[3]
            ChannelStripCard(
                name = "Music / Media",
                subtitle = "Spotify / Apple Music Feed",
                icon = Icons.Default.MusicNote,
                badge = "LIVE ONLY",
                color = PrimaryContainer,
                muted = music.muted,
                onMuteToggle = { onMuteToggle(3) }
            ) {
                Surface(
                    color = Color(0xFF090E19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                            Text("Stripped from Twitch/YouTube VOD", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        }
                        Text("CH 6 Routed", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    }
                }

                VolAndPanControls(
                    gainDb = music.gainDb,
                    pan = music.pan,
                    color = PrimaryContainer,
                    onGainChange = { onGainChange(3, it) },
                    onPanChange = { onPanChange(3, it) }
                )
            }
        }
    }
}

@Composable
private fun ChannelStripCard(
    name: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    color: Color,
    muted: Boolean,
    onMuteToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }

                    Column {
                        Text(name, style = PulseCastType.headlineSm, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(subtitle, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(badge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        color = if (muted) ErrorContainer else SurfaceHigh,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onMuteToggle() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = if (muted) Color.White else OnSurface, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun VolAndPanControls(
    gainDb: Float,
    pan: Float,
    color: Color,
    onGainChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Vol", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.width(36.dp), fontSize = 9.sp)
            Slider(
                value = gainDb,
                onValueChange = onGainChange,
                valueRange = -20f..10f,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = color, inactiveTrackColor = SurfaceHigh),
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
            )
            Text(String.format(Locale.US, "%.1f dB", gainDb), style = PulseCastType.labelTelemetrySm, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp), textAlign = TextAlign.End, fontSize = 9.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Pan", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.width(36.dp), fontSize = 9.sp)
            Slider(
                value = pan,
                onValueChange = onPanChange,
                valueRange = -50f..50f,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = OnSurfaceMuted, inactiveTrackColor = SurfaceHigh),
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
            )
            Text("L0 / R0", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.width(48.dp), textAlign = TextAlign.End, fontSize = 9.sp)
        }
    }
}

@Composable
private fun DmcaShieldMatrixCard(
    dmcaShieldEnabled: Boolean,
    onDmcaToggle: () -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(20.dp))
                    Text("DMCA Shield Matrix", style = PulseCastType.buttonText, color = OnSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (dmcaShieldEnabled) "HARDWARE ACTIVE" else "SHIELD OFF",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = PulseCastType.labelTelemetrySm,
                            color = PrimaryContainer,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = dmcaShieldEnabled,
                        onCheckedChange = { onDmcaToggle() },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Text(
                text = "Dual-track dynamic routing strips copyrighted audio tracks in real-time from Twitch VODs, YouTube recordings, and automatic highlight clips.",
                style = PulseCastType.bodySm,
                color = OnSurfaceMuted
            )

            // Matrix Table
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Destination", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.weight(1.5f), fontSize = 8.sp)
                        Text("Game", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 8.sp)
                        Text("Mic", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 8.sp)
                        Text("Voice", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 8.sp)
                        Text("Music", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 8.sp)
                    }

                    // Row 1: Live Broadcast Output
                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Broadcast", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 8.sp)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    // Row 2: VOD Master Vault
                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VOD / Replays", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 8.sp)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✓", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("✕ Shield", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 7.sp)
                        }
                    }
                }
            }

            Surface(
                color = PrimaryContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(14.dp))
                    Text("Strike Protection: 100% Safe VOD storage guarantee enabled.", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun SfxQuickDeck(
    activeSfxIndex: Int,
    onSfxClick: (Int) -> Unit
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Icon(Icons.Default.GridView, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                    Text("SFX Quick Deck", style = PulseCastType.buttonText, color = OnSurface)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Trim 80%", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                }
            }

            val pads = listOf(
                Triple("Victory Horn", Icons.Default.Celebration, SecondaryFixedDim),
                Triple("Airhorn", Icons.Default.Campaign, PrimaryContainer),
                Triple("Clutch Clap", Icons.Default.ThumbUp, NeonAmber),
                Triple("Censor Bleep", Icons.AutoMirrored.Filled.VolumeOff, ErrorContainer),
                Triple("Dram Drum", Icons.Default.Album, SecondaryFixedDim),
                Triple("GG Sound", Icons.Default.SentimentVerySatisfied, NeonAmber)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pads.chunked(3).forEachIndexed { rowIndex, rowPads ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPads.forEachIndexed { colIndex, (title, icon, padColor) ->
                            val padIndex = rowIndex * 3 + colIndex
                            val isActive = activeSfxIndex == padIndex
                            Surface(
                                color = if (isActive) padColor.copy(alpha = 0.25f) else SurfaceHigh,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isActive) BorderStroke(1.dp, padColor) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSfxClick(padIndex) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(icon, contentDescription = null, tint = padColor, modifier = Modifier.size(22.dp))
                                    Text(title, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun BottomActionBar(onApply: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Apply Audio Profile", style = PulseCastType.headlineSm, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onApply() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Save, contentDescription = "Save Preset", tint = OnSurface, modifier = Modifier.size(20.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Active Preset: FPS Gamer Master", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
            Text("Low Jitter DSP Synced", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun ProMultiTrackAudioMixerPreview() {
    PulseCastTheme {
        ProMultiTrackAudioMixerScreen()
    }
}
