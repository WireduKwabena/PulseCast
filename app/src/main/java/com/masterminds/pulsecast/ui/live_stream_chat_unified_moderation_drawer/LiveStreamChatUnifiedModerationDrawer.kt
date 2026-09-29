package com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterminds.pulsecast.streaming.*
import com.masterminds.pulsecast.ui.theme.*
import com.masterminds.pulsecast.ui.ui_library.*

import androidx.compose.foundation.text.BasicTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.filteredMessages.collectAsState()
    val velocity by viewModel.chatVelocity.collectAsState()
    val filter by viewModel.activePlatformFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.connectDefaultChannels()
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = BgBase,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // Meta Bar & Filters Section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChatMetaBar()
                ChatTelemetryRow(velocity.toString())
                PlatformFilterStrip(filter) { viewModel.setFilter(it) }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { PinnedSuperChatCard() }
                item { ModeratorShieldsHUD() }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LIVE STREAM FEED", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Sync, null, Modifier.size(12.dp), SecondaryFixedDim)
                            Text("Real-Time Lock", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    if (message.isSpamFlagged) {
                        SpamLinkFilteredCard(message, onApprove = { viewModel.approveMessage(message.id) }, onBan = { viewModel.banUser(message.id) })
                    } else {
                        ChatMessageItem(message)
                    }
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("QUICK COMMAND MACROS", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                        QuickCommandMacros()
                    }
                }
            }

            // Input Dock
            BroadcastInputDock(onSend = { viewModel.sendChatMessage(it) })
        }
    }
}

@Composable
private fun ChatMetaBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PulseStatusDot(color = PrimaryContainer)
            Text(
                text = "Unified Chat\nStream", 
                style = PulseCastType.headlineSm.copy(lineHeight = 22.sp),
                modifier = Modifier.padding(end = 8.dp)
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
                Icon(Icons.Default.Hub, null, Modifier.size(14.dp), SecondaryFixedDim)
                Text("3 FEEDS SYNCED", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChatTelemetryRow(velocity: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TelemetryBox(label = "Velocity", value = velocity, unit = "msg/m", icon = Icons.Default.Speed, color = NeonAmber, modifier = Modifier.weight(1f))
        TelemetryBox(label = "Auto-Mod", value = "14", unit = "blocked", icon = Icons.Default.Security, color = ElectricRuby, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TelemetryBox(label: String, value: String, unit: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(icon, null, Modifier.size(14.dp), color)
                    Text(
                        text = label, 
                        style = PulseCastType.bodySm.copy(fontSize = 10.sp, lineHeight = 11.sp), 
                        color = OnSurfaceMuted,
                        maxLines = 2
                    )
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, style = PulseCastType.labelTelemetryLg.copy(fontSize = 15.sp), color = OnSurface)
                Text(unit, style = PulseCastType.labelTelemetrySm.copy(fontSize = 8.sp), color = OnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun PlatformFilterStrip(activeFilter: PlatformType?, onFilterSelect: (PlatformType?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            FilterChip(
                selected = activeFilter == null,
                onClick = { onFilterSelect(null) },
                label = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AllInclusive, null, Modifier.size(14.dp))
                    Text("All Platforms")
                    Surface(color = Color.Black.copy(alpha = 0.2f), shape = CircleShape) {
                        Text("14.8k", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm)
                    }
                }},
                colors = FilterChipDefaults.filterChipColors(containerColor = PrimaryContainer, labelColor = Color.Black, selectedContainerColor = PrimaryContainer, selectedLabelColor = Color.Black)
            )
        }
        listOf(
            Triple("YouTube", PrimaryContainer, PlatformType.YOUTUBE),
            Triple("Twitch", SecondaryContainer, PlatformType.TWITCH),
            Triple("Kick", NeonAmber, PlatformType.KICK)
        ).forEach { (name, color, type) ->
            item {
                FilterChip(
                    selected = activeFilter == type,
                    onClick = { onFilterSelect(type) },
                    label = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                        Text(name)
                        Text("8.2k", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                    }},
                    colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceLow, labelColor = OnSurfaceMuted, selectedContainerColor = SurfaceHigh, selectedLabelColor = OnSurface)
                )
            }
        }
    }
}

@Composable
private fun PinnedSuperChatCard() {
    Surface(
        color = NeonAmber.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(top = 2.dp)) {
                        Surface(color = NeonAmber, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AttachMoney, null, Modifier.size(20.dp), Color.Black)
                            }
                        }
                        Surface(color = PrimaryContainer, shape = CircleShape, modifier = Modifier.size(14.dp).offset(x = 2.dp, y = 2.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("YT", style = PulseCastType.labelTelemetrySm, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("@CyberGamer99", style = PulseCastType.headlineSm, color = NeonAmber, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        // Badge on next line as requested
                        Surface(color = NeonAmber, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("$50.00", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetryMd, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "PINNED • 02:45 REMAINING", 
                            style = PulseCastType.labelTelemetrySm, 
                            color = OnSurfaceMuted,
                            lineHeight = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
                // Send FX Button - Ensuring visibility
                Surface(
                    onClick = { },
                    color = NeonAmber,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp), 
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Celebration, null, Modifier.size(16.dp), Color(0xFF332B00))
                        Text("Send FX", style = PulseCastType.buttonText, fontSize = 11.sp, color = Color(0xFF332B00))
                    }
                }
            }
            Surface(color = Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = "\"Let's go clutch this 1v4 round! GG PulseCast crew!\"",
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    style = PulseCastType.bodyMd,
                    color = OnSurface
                )
            }
        }
    }
}

@Composable
private fun ModeratorShieldsHUD() {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Tune, null, Modifier.size(16.dp), OnSurfaceMuted)
                    Text("LIVE ROOM SHIELDS", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, letterSpacing = 1.sp)
                }
                Text("Host Mode", style = PulseCastType.labelTelemetrySm, color = CyberCyan)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShieldToggle("Slow (5s)", Icons.Default.HourglassBottom, true, CyberCyan, Modifier.weight(1f))
                    ShieldToggle("Subs Only", Icons.Default.Stars, false, OnSurfaceMuted, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShieldToggle("Emote Only", Icons.Default.Mood, false, OnSurfaceMuted, Modifier.weight(1f))
                    ShieldToggle("Shield Bot", Icons.Default.Security, true, PrimaryContainer, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ShieldToggle(label: String, icon: ImageVector, active: Boolean, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = if (active) SurfaceHigh else SurfaceMid,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { }
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, Modifier.size(18.dp), if (active) color else OnSurfaceMuted)
                Text(label, style = PulseCastType.buttonText, color = if (active) OnSurface else OnSurfaceMuted, fontSize = 12.sp)
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (active) color else Color.Transparent).border(1.dp, if (active) Color.Transparent else OnSurfaceMuted.copy(alpha = 0.3f), CircleShape))
        }
    }
}

fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}

val PlatformType.displayColor: Color
    get() = when (this) {
        PlatformType.TWITCH -> SecondaryContainer
        PlatformType.YOUTUBE -> PrimaryContainer
        PlatformType.KICK -> NeonAmber
        PlatformType.CUSTOM -> Color.White
    }

val PlatformType.displayName: String
    get() = when (this) {
        PlatformType.TWITCH -> "Twitch"
        PlatformType.YOUTUBE -> "YouTube"
        PlatformType.KICK -> "Kick"
        PlatformType.CUSTOM -> "Custom"
    }

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val platformColor = message.platform.displayColor
    val platformName = message.platform.displayName
    
    Surface(
        color = SurfaceLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = SurfaceHigh, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(message.sender.take(2).uppercase(), style = PulseCastType.labelTelemetrySm, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(color = platformColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(platformName, modifier = Modifier.padding(horizontal = 4.dp), style = PulseCastType.labelTelemetrySm, color = platformColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    if (message.badges.isNotEmpty()) {
                        Surface(color = SurfaceHigh, shape = RoundedCornerShape(4.dp)) {
                            Text(message.badges.first().name, modifier = Modifier.padding(horizontal = 4.dp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 9.sp)
                        }
                    }
                    Text(
                        text = "@${message.sender}", 
                        style = PulseCastType.buttonText, 
                        color = OnSurface, 
                        fontSize = 11.sp, 
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.weight(1f)) // Push time to end
                    Text(formatTime(message.timestamp), style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted, fontSize = 10.sp)
                }
                Text(message.content, style = PulseCastType.bodyMd, color = OnSurface)
            }
        }
    }
}

@Composable
private fun SpamLinkFilteredCard(message: ChatMessage, onApprove: () -> Unit, onBan: () -> Unit) {
    Surface(
        color = ElectricRuby.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ElectricRuby.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Flag, null, Modifier.size(18.dp), ElectricRuby)
                    Text("SPAM LINK FILTERED", style = PulseCastType.labelTelemetrySm, color = ElectricRuby, fontWeight = FontWeight.Bold)
                }
                Text("Auto-Held", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("@${message.sender}:", style = PulseCastType.buttonText, color = OnSurfaceMuted)
                Text(
                    text = message.content, 
                    style = PulseCastType.bodySm.copy(lineHeight = 14.sp), 
                    color = OnSurface, 
                    fontWeight = FontWeight.Medium,
                    maxLines = 4, // Ensure fully visible
                    overflow = TextOverflow.Visible
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onApprove) { Text("Approve", color = OnSurface, style = PulseCastType.buttonText) }
                Button(
                    onClick = onBan,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricRuby, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Block, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ban User", style = PulseCastType.buttonText, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickCommandMacros() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Icons.Default.Gavel to "!rules", Icons.Default.Memory to "!specs", Icons.Default.Forum to "!discord", Icons.Default.VolunteerActivism to "!donate").forEach { (icon, label) ->
            item {
                Surface(color = SurfaceHigh, shape = CircleShape, modifier = Modifier.clickable { }) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(icon, null, Modifier.size(14.dp), SecondaryFixedDim)
                        Text(label, style = PulseCastType.labelTelemetryMd, color = SecondaryFixedDim)
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastInputDock(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Surface(
        color = BgBase.copy(alpha = 0.98f),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(color = SurfaceLow, shape = CircleShape) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* TTS */ },
                    modifier = Modifier.size(36.dp)
                ) { Icon(Icons.Default.RecordVoiceOver, null, Modifier.size(20.dp), OnSurface) }
                
                IconButton(
                    onClick = { /* Emotes */ },
                    modifier = Modifier.size(36.dp)
                ) { Icon(Icons.Default.Mood, null, Modifier.size(20.dp), OnSurface) }
                
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = PulseCastType.bodyMd.copy(color = OnSurface),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (text.isEmpty()) {
                                Text("Broadcast to all 3 platform chats...", style = PulseCastType.bodyMd, color = OnSurfaceMuted)
                            }
                            innerTextField()
                        }
                    }
                )
                
                Surface(
                    color = PrimaryContainer, 
                    shape = CircleShape, 
                    modifier = Modifier.size(36.dp).clickable { 
                        if (text.isNotBlank()) {
                            onSend(text)
                            text = ""
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp), Color.Black)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun LiveChatDrawerPreview() {
    PulseCastTheme {
        LiveChatDrawer(onClose = {})
    }
}
