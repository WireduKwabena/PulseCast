package com.masterminds.pulsecast.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer.LiveChatDrawer
import com.masterminds.pulsecast.ui.theme.*
import kotlin.math.roundToInt

class LiveHUDOverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private var composeView: ComposeView? = null
    private lateinit var windowManager: WindowManager

    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showHUD()
    }

    private fun showHUD() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LiveHUDOverlayService)
            setViewTreeViewModelStoreOwner(this@LiveHUDOverlayService)
            setViewTreeSavedStateRegistryOwner(this@LiveHUDOverlayService)

            setContent {
                PulseCastTheme {
                    LiveHUDContent()
                }
            }
        }

        windowManager.addView(composeView, params)
    }

    @Composable
    private fun LiveHUDContent() {
        var isRadialOpen by remember { mutableStateOf(false) }
        var isChatOpen by remember { mutableStateOf(false) }
        var isBrushActive by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HUD: Telemetry Badge & Facecam PIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Compact Telemetry Pill
                CompactTelemetryPill()
                
                // Right: Draggable Facecam PIP
                LivePIP()
            }

            // 2. BOTTOM-LEFT: Stream Chat Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 100.dp, start = 12.dp)
                    .width(260.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuperChatBanner()
                ChatBubbleHUD("@NeonGamer:", "that flick shot was insane!! 🔥", CyberCyan)
                ChatBubbleHUD("@ViperX:", "dual stream crisp 60fps on mobile", Color(0xFF9C27B0))
            }

            // 3. RIGHT EDGE: Floating Radial Menu
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isRadialOpen) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(end = 60.dp)
                    ) {
                        HUDActionCircle("Pause", Icons.Default.Pause, NeonAmber) { sendServiceAction(ScreenRecordService.ACTION_PAUSE) }
                        HUDActionCircle("Brush", Icons.Default.Brush, CyberCyan) { 
                            isBrushActive = !isBrushActive
                            isRadialOpen = false 
                        }
                        HUDActionCircle("Cam", Icons.Default.Face, Secondary) { /* toggle */ }
                        HUDActionCircle("Stop", Icons.Default.Stop, ElectricRuby) { 
                            sendServiceAction(ScreenRecordService.ACTION_STOP)
                            stopSelf()
                        }
                    }
                }

                // Signature PulseCast Ball
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricRuby, NeonAmber)))
                        .padding(2.5.dp)
                        .clickable { isRadialOpen = !isRadialOpen },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF07090E)).border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(ElectricRuby).shadow(10.dp, spotColor = ElectricRuby), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
            }

            // 4. BOTTOM HUD: Annotation Toolbar
            AnimatedVisibility(
                visible = isBrushActive,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp, start = 12.dp, end = 12.dp)
            ) {
                BrushToolbarHUD(onClose = { isBrushActive = false })
            }

            // Chat Drawer (Slide from right)
            AnimatedVisibility(
                visible = isChatOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.fillMaxHeight().width(320.dp).align(Alignment.CenterEnd)
            ) {
                LiveChatDrawer(onClose = { isChatOpen = false })
            }
        }
    }

    @Composable
    private fun CompactTelemetryPill() {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.78f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // REC & Timer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val infiniteTransition = rememberInfiniteTransition(label = "rec")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
                )
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricRuby).graphicsLayer { this.alpha = alpha })
                Text("REC 00:14:28", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Box(modifier = Modifier.height(14.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))
            // Stats
            Column {
                Text("1080p60", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("18.4 Mbps", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Box(modifier = Modifier.height(14.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))
            // Audio VU
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(4.dp, 8.dp).background(SignalGreen))
                Box(modifier = Modifier.size(4.dp, 12.dp).background(SignalGreen))
                Box(modifier = Modifier.size(4.dp, 10.dp).background(NeonAmber))
            }
        }
    }

    @Composable
    private fun LivePIP() {
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp, 66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07090E))
                    .border(2.dp, ElectricRuby, RoundedCornerShape(12.dp))
                    .shadow(16.dp, spotColor = ElectricRuby.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                // Mock Facecam
                Icon(Icons.Default.Person, null, tint = ElectricRuby.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
                
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalGreen))
                        Text("CAM 1", style = MaterialTheme.typography.labelSmall, color = SignalGreen, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // Mic Status Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 8.dp)
                    .background(Color.Black.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SignalGreen))
                    Text("ON-AIR", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    @Composable
    private fun SuperChatBanner() {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NeonAmber.copy(alpha = 0.15f))
                .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(NeonAmber), contentAlignment = Alignment.Center) {
                Text("$", color = Color(0xFF07090E), fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("@Kira99", style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("$10.00", style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.background(NeonAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                }
                Text("CLUTCH THIS! 👑", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            }
        }
    }

    @Composable
    private fun ChatBubbleHUD(user: String, text: String, color: Color) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.78f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(user, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 10.sp)
        }
    }

    @Composable
    private fun HUDActionCircle(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp)).border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            IconButton(onClick = onClick, modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.82f)).border(1.dp, color.copy(alpha = 0.6f), CircleShape)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
    }

    @Composable
    private fun BrushToolbarHUD(onClose: () -> Unit) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {}, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(CyberCyan)) {
                    Icon(Icons.Default.Brush, null, tint = Color(0xFF07090E), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.TrendingFlat, null, tint = Color.White.copy(alpha = 0.8f))
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Rectangle, null, tint = Color.White.copy(alpha = 0.8f))
                }
            }
            Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))
            Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(ElectricRuby))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(CyberCyan).border(2.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(NeonAmber))
            }
            Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))
            IconButton(onClick = onClose, modifier = Modifier.padding(start = 4.dp).size(28.dp).clip(RoundedCornerShape(8.dp)).background(ElectricRuby.copy(alpha = 0.2f))) {
                Icon(Icons.Default.ExpandMore, null, tint = ElectricRuby, modifier = Modifier.size(16.dp))
            }
        }
    }

    private fun sendServiceAction(action: String) {
        startService(Intent(this, ScreenRecordService::class.java).setAction(action))
    }

    override fun onDestroy() {
        composeView?.let { windowManager.removeView(it) }
        _viewModelStore.clear()
        super.onDestroy()
    }
}
