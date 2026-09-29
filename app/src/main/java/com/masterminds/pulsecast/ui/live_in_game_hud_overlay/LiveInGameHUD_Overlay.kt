package com.masterminds.pulsecast.ui.live_in_game_hud_overlay

import android.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

import com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer.LiveChatDrawer

@Composable
fun LiveInGameHUDOverlayScreen(
    modifier: Modifier = Modifier,
    onStopRecording: () -> Unit = {}
) {
    var showChat by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. BACKGROUND VIEWPORT: Full-bleed gameplay
        Image(
            painter = painterResource(id = R.drawable.ic_menu_gallery),
            contentDescription = "Gameplay Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Legibility Gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
        )

        // 2. TOP HUD: Telemetry Badge & Facecam PIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            TelemetryBadge()
            FacecamPIP()
        }

        // 3. RIGHT EDGE FLOATING RADIAL MENU
        FloatingRadialMenu(onStop = onStopRecording, onShowChat = { showChat = true })

        // 4. BOTTOM-LEFT STREAM CHAT OVERLAY
        ChatOverlay(modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 80.dp))

        // 5. BOTTOM HUD ANNOTATION TOOLBAR
        AnnotationToolbar(modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 16.dp))
        
        // Internal Modal for Chat
        if (showChat) {
            LiveChatDrawer(onClose = { showChat = false })
        }
    }
}

@Composable
private fun TelemetryBadge() {
    Surface(
        color = Color(0xFF0B0F19).copy(alpha = 0.78f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), // Even tighter padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // REC & Timer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(end = 2.dp)
            ) {
                PulseStatusDot(color = ElectricRuby)
                Text(
                    text = "REC 00:14:28",
                    style = PulseCastType.labelTelemetrySm.copy(fontSize = 9.sp), // Sleek font size
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider
            Box(Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.12f)))

            // Res & Bitrate
            Column {
                Text("1080p60", style = PulseCastType.labelTelemetrySm, color = CyberCyan, fontWeight = FontWeight.SemiBold, fontSize = 8.sp)
                Text("18.4 Mbps", style = PulseCastType.labelTelemetrySm, color = Color.LightGray, fontSize = 8.sp)
            }

            // Divider
            Box(Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.12f)))

            // Audio Meter
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Default.Mic, null, Modifier.size(9.dp), SignalGreen)
                Row(modifier = Modifier.height(10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    Box(Modifier.size(2.dp, 5.dp).clip(CircleShape).background(SignalGreen))
                    Box(Modifier.size(2.dp, 8.dp).clip(CircleShape).background(SignalGreen).alpha(0.6f))
                    Box(Modifier.size(2.dp, 6.dp).clip(CircleShape).background(NeonAmber))
                }
            }
        }
    }
}

@Composable
private fun FacecamPIP() {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 6.dp)) {
        Surface(
            modifier = Modifier.size(width = 92.dp, height = 66.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, ElectricRuby),
            color = Color(0xFF07090E),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, Modifier.size(32.dp), Color.White.copy(alpha = 0.2f))
                // CAM 1 Watermark
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(SignalGreen))
                        Text("CAM 1", style = PulseCastType.labelTelemetrySm, color = SignalGreen, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // ON-AIR Badge - Overlapping the bottom center
        Surface(
            color = Color.Black.copy(alpha = 0.95f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.offset(y = 8.dp) // Halfway out of the main PIP
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(SignalGreen))
                Text("ON-AIR", style = PulseCastType.labelTelemetrySm, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FloatingRadialMenu(onStop: () -> Unit, onShowChat: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(end = 12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Action Arc (Linear column for mobile, arc in design)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                RadialActionItem(Icons.Default.Pause, NeonAmber)
                RadialActionItem(Icons.Default.Edit, CyberCyan)
                RadialActionItem(Icons.Default.Videocam, Color(0xFFC084FC))
                RadialActionItem(Icons.Default.ChatBubble, Color(0xFFF472B6), badge = "24", onClick = onShowChat)
                RadialActionItem(null, ElectricRuby, isStop = true, onClick = onStop)
            }

            // Main Pulse Ball
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(ElectricRuby, Color(0xFFF43F5E), NeonAmber, ElectricRuby)
                        )
                    )
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF07090E)).border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(ElectricRuby),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    }
                }
            }
        }
    }
}

@Composable
private fun RadialActionItem(
    icon: ImageVector?, 
    color: Color, 
    badge: String? = null,
    isStop: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = Color(0xFF07090E).copy(alpha = 0.82f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
            shadowElevation = 4.dp,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isStop) {
                    Box(Modifier.size(14.dp).clip(RoundedCornerShape(2.dp)).background(color))
                } else if (icon != null) {
                    Icon(icon, null, Modifier.size(16.dp), color)
                }
            }
        }
        if (badge != null) {
            Surface(
                color = Color(0xFFEC4899),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = PulseCastType.labelTelemetrySm,
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChatOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(260.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Super Chat
        Surface(
            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = NeonAmber,
                    shape = CircleShape,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$", style = PulseCastType.labelTelemetrySm, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                    }
                }
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("@Kira99", style = PulseCastType.labelTelemetrySm, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("$10.00", style = PulseCastType.labelTelemetrySm, color = Color(0xFFFCD34D), fontSize = 9.sp, modifier = Modifier.background(NeonAmber.copy(alpha = 0.2f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp))
                    }
                    Text("CLUTCH THIS! 👑", style = PulseCastType.bodySm, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                }
            }
        }

        // Standard Messages
        ChatBubble("@NeonGamer:", "that flick shot was insane!! 🔥", CyberCyan)
        ChatBubble("@ViperX:", "dual stream crisp 60fps on mobile lets goo", Color(0xFFC084FC))
    }
}

@Composable
private fun ChatBubble(user: String, message: String, color: Color) {
    Surface(
        color = Color(0xFF0B0F19).copy(alpha = 0.78f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(user, style = PulseCastType.labelTelemetrySm, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(message, style = PulseCastType.labelTelemetrySm, color = Color(0xFFE2E8F0), fontSize = 10.sp)
        }
    }
}

@Composable
private fun AnnotationToolbar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tools
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Surface(
                    color = CyberCyan,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp), Color.Black)
                    }
                }
                listOf(Icons.AutoMirrored.Filled.ArrowForward, Icons.Default.CropSquare).forEach { icon ->
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, Modifier.size(16.dp), Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Divider
            Box(Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

            // Color Palette
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                ColorDot(ElectricRuby)
                ColorDot(CyberCyan, isActive = true)
                ColorDot(NeonAmber)
                ColorDot(Color.White)
            }

            // Divider
            Box(Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))

            // Utility
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                listOf(Icons.AutoMirrored.Filled.Undo, Icons.Default.Delete, Icons.Default.KeyboardArrowDown).forEach { icon ->
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, Modifier.size(14.dp), Color(0xFFCBD5E1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, isActive: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (isActive) 20.dp else 16.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (isActive) Modifier.border(2.dp, Color.White, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {}
}

@Preview(widthDp = 430, heightDp = 932)
@Composable
fun InGameHUDPreview() {
    PulseCastTheme {
        LiveInGameHUDOverlayScreen()
    }
}
