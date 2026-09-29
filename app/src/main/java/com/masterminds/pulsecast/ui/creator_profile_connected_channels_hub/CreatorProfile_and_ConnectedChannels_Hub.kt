package com.masterminds.pulsecast.ui.creator_profile_connected_channels_hub

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*

@Composable
fun CreatorProfile_and_ConnectedChannels_HubScreen(
    onBack: () -> Unit = {},
    onAddIngest: () -> Unit = {},
    onManagePro: () -> Unit = {},
    onCustomizeOrb: () -> Unit = {}
) {
    // Channel Toggles
    var youtubeEnabled by remember { mutableStateOf(true) }
    var twitchEnabled by remember { mutableStateOf(true) }
    var tiktokEnabled by remember { mutableStateOf(true) }
    var kickEnabled by remember { mutableStateOf(false) }

    // Storage Sync Policies
    var autoSyncRecordings by remember { mutableStateOf(true) }
    var wifiOnlyBackup by remember { mutableStateOf(true) }
    var autoExportYoutube by remember { mutableStateOf(true) }

    // Watermark & Branding
    var watermarkOpacity by remember { mutableFloatStateOf(80f) }
    var watermarkAnchor by remember { mutableStateOf("TR") }

    // Audio & DMCA
    var dmcaIsolationEnabled by remember { mutableStateOf(true) }

    var showLogoutDialog by remember { mutableStateOf(false) }

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
            // Screen Navigation Bar
            ScreenNavBar(onBack = onBack)

            // Creator Identity Card
            CreatorIdentityCard(onManagePro = onManagePro)

            // Connected Broadcast Channels & Platforms
            SimulcastChannelsSection(
                youtubeEnabled = youtubeEnabled,
                onYoutubeToggle = { youtubeEnabled = it },
                twitchEnabled = twitchEnabled,
                onTwitchToggle = { twitchEnabled = it },
                tiktokEnabled = tiktokEnabled,
                onTiktokToggle = { tiktokEnabled = it },
                kickEnabled = kickEnabled,
                onKickToggle = { kickEnabled = it },
                onAddIngest = onAddIngest
            )

            // Studio Cloud Storage Vault
            CloudStorageVaultCard(
                autoSync = autoSyncRecordings,
                onAutoSyncToggle = { autoSyncRecordings = it },
                wifiOnly = wifiOnlyBackup,
                onWifiOnlyToggle = { wifiOnlyBackup = it },
                autoExport = autoExportYoutube,
                onAutoExportToggle = { autoExportYoutube = it }
            )

            // Branding, Stinger & Floating Orb Skins
            BrandingAndOverlaysCard(
                watermarkOpacity = watermarkOpacity,
                onOpacityChange = { watermarkOpacity = it },
                watermarkAnchor = watermarkAnchor,
                onAnchorSelect = { watermarkAnchor = it },
                onChangeOrbSkin = onCustomizeOrb
            )

            // Audio & Copyright Licenses (DMCA Shield)
            AudioAndDmcaShieldCard(
                dmcaIsolationEnabled = dmcaIsolationEnabled,
                onDmcaToggle = { dmcaIsolationEnabled = it }
            )

            // Account Session Actions
            AccountSessionActions(
                onLogoutClick = { showLogoutDialog = true }
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(onDismiss = { showLogoutDialog = false })
    }
}

@Composable
private fun ScreenNavBar(onBack: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Creator Hub", style = PulseCastType.headlineSm, color = OnSurface)
                    Box(Modifier.size(6.dp).clip(CircleShape).background(SecondaryFixedDim))
                }
                Text("ACCOUNT • INGEST NODES", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.IosShare, contentDescription = "Share", tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }
            Surface(
                color = SurfaceHigh,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CreatorIdentityCard(onManagePro: () -> Unit) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryContainer, SecondaryFixedDim, NeonAmber)))
                            .padding(2.dp)
                    ) {
                        Surface(
                            color = SurfaceHigh,
                            shape = CircleShape,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OnSurface, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Alex 'Viper' Chen", style = PulseCastType.headlineSm, color = OnSurface, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                        }
                        Text("@AlexViper_Live", style = PulseCastType.labelTelemetryMd, color = SecondaryFixedDim, fontSize = 11.sp)
                        Text("Apex Predator • High-FPS Broadcaster", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                    }
                }
            }

            // Pro Membership Tier Badge
            Surface(
                color = SurfaceHigh,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                        Column {
                            Text("PULSECAST PRO STUDIO", style = PulseCastType.buttonText, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Annual Pass • Auto-renews Oct 2025", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        }
                    }

                    Surface(
                        color = NeonAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onManagePro() }
                    ) {
                        Text("Manage", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Creator Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatPill("248K", "Followers", OnSurface, Modifier.weight(1f))
                StatPill("1.4M", "Views", SecondaryFixedDim, Modifier.weight(1f))
                StatPill("342", "Streams", OnSurface, Modifier.weight(1f))
                StatPill("60FPS", "1080p Ultra", PrimaryContainer, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = PulseCastType.labelTelemetryLg, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(label, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
        }
    }
}

@Composable
private fun SimulcastChannelsSection(
    youtubeEnabled: Boolean,
    onYoutubeToggle: (Boolean) -> Unit,
    twitchEnabled: Boolean,
    onTwitchToggle: (Boolean) -> Unit,
    tiktokEnabled: Boolean,
    onTiktokToggle: (Boolean) -> Unit,
    kickEnabled: Boolean,
    onKickToggle: (Boolean) -> Unit,
    onAddIngest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CellTower, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                Text("Simulcast Channels", style = PulseCastType.buttonText, color = OnSurface)
            }

            Surface(
                color = PrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onAddIngest() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AddLink, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Text("Add Ingest", style = PulseCastType.buttonText, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Channel Cards
        ChannelCard(
            name = "YouTube Live",
            channelHandle = "ViperGaming HD • 184K Subs",
            badge = "PRIMARY",
            badgeColor = PrimaryContainer,
            detail = "RTMP ingest.youtube.com • Key: live_984••••",
            enabled = youtubeEnabled,
            onToggle = onYoutubeToggle,
            icon = Icons.Default.SmartDisplay
        )

        ChannelCard(
            name = "Twitch",
            channelHandle = "ViperX_Twitch • 54.2K Followers",
            badge = "LOW LATENCY",
            badgeColor = SecondaryFixedDim,
            detail = "SRT caller://us-east.twitch.tv • Encrypted Handshake",
            enabled = twitchEnabled,
            onToggle = onTwitchToggle,
            icon = Icons.Default.Stream
        )

        ChannelCard(
            name = "TikTok Live",
            channelHandle = "@viper_clips • Verified Creator Server",
            badge = "PORTRAIT 9:16",
            badgeColor = NeonAmber,
            detail = "RTMP push.tiktokv.com • Key: s81f••••",
            enabled = tiktokEnabled,
            onToggle = onTiktokToggle,
            icon = Icons.Default.MusicNote
        )

        ChannelCard(
            name = "Kick Streaming",
            channelHandle = "kick.com/viperchen • Standby",
            badge = "OFFLINE",
            badgeColor = OnSurfaceMuted,
            detail = "RTMP rtmps://fa723794b6f0.global-contribute.live-video.net",
            enabled = kickEnabled,
            onToggle = onKickToggle,
            icon = Icons.Default.SensorsOff,
            isOffline = !kickEnabled
        )

        // Add Custom Target Node Button
        Button(
            onClick = onAddIngest,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLow, contentColor = SecondaryFixedDim),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Add Custom RTMP / SRT Target Node", style = PulseCastType.buttonText)
            }
        }
    }
}

@Composable
private fun ChannelCard(
    name: String,
    channelHandle: String,
    badge: String,
    badgeColor: Color,
    detail: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: ImageVector,
    isOffline: Boolean = false
) {
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isOffline && !enabled) 0.65f else 1.0f)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(name, style = PulseCastType.headlineSm, color = OnSurface)
                            Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                                Text(badge, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = PulseCastType.labelTelemetrySm, color = badgeColor, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(channelHandle, style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrimaryContainer),
                    modifier = Modifier.scale(0.8f)
                )
            }

            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(detail, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CloudStorageVaultCard(
    autoSync: Boolean,
    onAutoSyncToggle: (Boolean) -> Unit,
    wifiOnly: Boolean,
    onWifiOnlyToggle: (Boolean) -> Unit,
    autoExport: Boolean,
    onAutoExportToggle: (Boolean) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                    Text("Cloud Vault", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp)) {
                    Text("PRO 500GB", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
                }
            }

            // Usage Progress Meter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("128.4 GB Used", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("371.6 GB Available (25.6%)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                }

                LinearProgressIndicator(
                    progress = { 0.256f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = PrimaryContainer,
                    trackColor = SurfaceHigh
                )
            }

            // Legend Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VaultSegmentPill("92 GB", "Master VODs", PrimaryContainer, Modifier.weight(1f))
                VaultSegmentPill("24 GB", "Clips & Shorts", SecondaryFixedDim, Modifier.weight(1f))
                VaultSegmentPill("12.4 GB", "Replays", NeonAmber, Modifier.weight(1f))
            }

            // Sync Configuration Toggles
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SyncToggleRow("Auto-sync gameplay recordings", Icons.Default.Sync, autoSync, onAutoSyncToggle)
                SyncToggleRow("Backup via Wi-Fi only (Save 5G)", Icons.Default.Wifi, wifiOnly, onWifiOnlyToggle)
                SyncToggleRow("Auto-export Unlisted VOD to YouTube", Icons.Default.Upload, autoExport, onAutoExportToggle)
            }
        }
    }
}

@Composable
private fun VaultSegmentPill(size: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090E19),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Column {
                Text(size, style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                Text(label, style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SyncToggleRow(title: String, icon: ImageVector, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
            Text(title, style = PulseCastType.bodySm, color = OnSurface, fontSize = 10.sp)
        }
        Checkbox(checked = checked, onCheckedChange = onToggle, colors = CheckboxDefaults.colors(checkedColor = PrimaryContainer))
    }
}

@Composable
private fun BrandingAndOverlaysCard(
    watermarkOpacity: Float,
    onOpacityChange: (Float) -> Unit,
    watermarkAnchor: String,
    onAnchorSelect: (String) -> Unit,
    onChangeOrbSkin: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                    Text("Branding & Overlays", style = PulseCastType.buttonText, color = OnSurface)
                }
                Text("3 Active Assets", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp)
            }

            // Custom Watermark Sub-Card
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Custom Watermark", style = PulseCastType.bodySm, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("VIPER_GLOW_LOGO.png", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 8.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Watermark Opacity", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        Text("${watermarkOpacity.toInt()}%", style = PulseCastType.labelTelemetrySm, color = OnSurface, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = watermarkOpacity,
                        onValueChange = onOpacityChange,
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = PrimaryContainer, inactiveTrackColor = SurfaceHigh),
                        modifier = Modifier.height(18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Screen Anchor:", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("TL", "TR", "BL", "BR").forEach { pos ->
                                val isSelected = watermarkAnchor == pos
                                Surface(
                                    color = if (isSelected) PrimaryContainer else SurfaceHigh,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable { onAnchorSelect(pos) }
                                ) {
                                    Text(pos, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = if (isSelected) Color.Black else OnSurfaceMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Live Stinger Video
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Animation, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Broadcast Transition Sting", style = PulseCastType.bodySm, color = OnSurface, fontWeight = FontWeight.Bold)
                            Text("Cyber_Drop_Stinger_1080p.mp4 (00:03s)", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        }
                    }

                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                }
            }

            // Floating Ball Skin Change Tile
            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer)
                        )
                        Column {
                            Text("Floating Ball Assist Skin", style = PulseCastType.bodySm, color = OnSurface, fontWeight = FontWeight.Bold)
                            Text("Active: Neon Cyber Red", style = PulseCastType.labelTelemetrySm, color = PrimaryContainer, fontSize = 8.sp)
                        }
                    }

                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onChangeOrbSkin() }
                    ) {
                        Text("Change", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioAndDmcaShieldCard(
    dmcaIsolationEnabled: Boolean,
    onDmcaToggle: (Boolean) -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(20.dp))
                    Text("Audio & DMCA Shield", style = PulseCastType.buttonText, color = OnSurface)
                }
                Surface(color = SecondaryFixedDim.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                    Text("ARMED", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                color = Color(0xFF090E19),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = SecondaryFixedDim, modifier = Modifier.size(16.dp))
                        Column {
                            Text("Dual-Track DMCA Isolation", style = PulseCastType.bodySm, color = OnSurface, fontWeight = FontWeight.Bold)
                            Text("Splits Spotify / Discord from live Twitch/YT VOD tracks", style = PulseCastType.bodySm, color = OnSurfaceMuted, fontSize = 8.sp)
                        }
                    }
                    Checkbox(checked = dmcaIsolationEnabled, onCheckedChange = onDmcaToggle, colors = CheckboxDefaults.colors(checkedColor = SecondaryFixedDim))
                }
            }
        }
    }
}

@Composable
private fun AccountSessionActions(onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
                    Text("Switch Creator Profile", style = PulseCastType.buttonText, color = OnSurface)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceMuted, modifier = Modifier.size(18.dp))
            }
        }

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Disconnect Session / Log Out", style = PulseCastType.buttonText, fontWeight = FontWeight.Bold)
            }
        }

        Text("PulseCast Studio Engine v4.2.1-prod • Build 8492", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun LogoutConfirmationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = Color.White)) { Text("Log Out") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurface) }
        },
        title = { Text("Log Out?", style = PulseCastType.headlineSm) },
        text = { Text("Are you sure you want to disconnect your channel accounts and cloud vault session?", style = PulseCastType.bodySm, color = OnSurfaceMuted) },
        containerColor = SurfaceLow,
        titleContentColor = OnSurface
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E131E)
@Composable
fun CreatorProfile_and_ConnectedChannels_HubPreview() {
    PulseCastTheme {
        CreatorProfile_and_ConnectedChannels_HubScreen()
    }
}
