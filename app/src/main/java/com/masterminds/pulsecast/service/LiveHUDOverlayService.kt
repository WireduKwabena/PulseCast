package com.masterminds.pulsecast.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.masterminds.pulsecast.MainActivity
import com.masterminds.pulsecast.core.CaptureSessionStore
import com.masterminds.pulsecast.ui.theme.*
import kotlinx.coroutines.delay

class LiveHUDOverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        const val ACTION_MIC_MUTED = "com.masterminds.pulsecast.ACTION_MIC_MUTED"
        const val ACTION_MIC_UNMUTED = "com.masterminds.pulsecast.ACTION_MIC_UNMUTED"
    }

    private var composeView: ComposeView? = null
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams

    private var isMicMuted by mutableStateOf(false)
    var isCapturePaused by mutableStateOf(false)
    var isFaceCamActive by mutableStateOf(false)

    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showFloatingOrb()
    }

    private fun showFloatingOrb() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 300
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LiveHUDOverlayService)
            setViewTreeViewModelStoreOwner(this@LiveHUDOverlayService)
            setViewTreeSavedStateRegistryOwner(this@LiveHUDOverlayService)

            setContent {
                PulseCastTheme {
                    FloatingOrbContent(
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(this, params)
                        },
                        onStartRecording = {
                            val intent = Intent(this@LiveHUDOverlayService, MainActivity::class.java).apply {
                                action = MainActivity.ACTION_START_RECORDING_FROM_ORB
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            startActivity(intent)
                        },
                        onTogglePause = {
                            isCapturePaused = !isCapturePaused
                            val action = if (isCapturePaused) ScreenRecordService.ACTION_PAUSE else ScreenRecordService.ACTION_RESUME
                            sendServiceAction(action)
                        },
                        onToggleMic = {
                            isMicMuted = !isMicMuted
                            val action = if (isMicMuted) ACTION_MIC_MUTED else ACTION_MIC_UNMUTED
                            sendServiceAction(action)
                        },
                        onToggleFaceCam = {
                            if (ContextCompat.checkSelfPermission(this@LiveHUDOverlayService, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(this@LiveHUDOverlayService, "Camera permission is required for Facecam PIP", Toast.LENGTH_LONG).show()
                                return@FloatingOrbContent
                            }
                            isFaceCamActive = !isFaceCamActive
                            if (isFaceCamActive) {
                                val intent = Intent(this@LiveHUDOverlayService, FaceCamOverlayService::class.java)
                                ContextCompat.startForegroundService(this@LiveHUDOverlayService, intent)
                            } else {
                                stopService(Intent(this@LiveHUDOverlayService, FaceCamOverlayService::class.java))
                            }
                        },
                        onStopRecording = {
                            sendServiceAction(ScreenRecordService.ACTION_STOP)
                            stopService(Intent(this@LiveHUDOverlayService, FaceCamOverlayService::class.java))
                        }
                    )
                }
            }
        }

        windowManager.addView(composeView, params)
    }

    @Composable
    private fun FloatingOrbContent(
        onDrag: (Float, Float) -> Unit,
        onStartRecording: () -> Unit,
        onTogglePause: () -> Unit,
        onToggleMic: () -> Unit,
        onToggleFaceCam: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        var orbAlpha by remember { mutableFloatStateOf(0.35f) } // Faded low opacity by default

        val isRecording by CaptureSessionStore.isRecording.collectAsState()
        val durationSec by CaptureSessionStore.recordingDurationSeconds.collectAsState()
        val timerText = CaptureSessionStore.getFormattedDuration(durationSec)
        val context = LocalContext.current

        // Smooth Opacity Decay Coroutine: Fades from 1.0f back down to 0.35f after 3.5s of inactivity
        LaunchedEffect(orbAlpha, isExpanded) {
            if (orbAlpha > 0.35f && !isExpanded) {
                delay(3500)
                orbAlpha = 0.35f
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Main Floating Orb
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .alpha(if (isExpanded) 1.0f else orbAlpha)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { orbAlpha = 1.0f }
                            ) { change, drag ->
                                change.consume()
                                orbAlpha = 1.0f
                                onDrag(drag.x, drag.y)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    orbAlpha = 1.0f
                                    isExpanded = !isExpanded
                                }
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricRuby, NeonAmber)))
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF07090E))
                                .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isCapturePaused) NeonAmber else ElectricRuby)
                                    .shadow(8.dp, spotColor = ElectricRuby),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                }

                // Live Duration Badge below Orb (Visible when recording or active)
                if (isRecording) {
                    Surface(
                        color = Color(0xFF090E19).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.alpha(if (isExpanded) 1.0f else orbAlpha)
                    ) {
                        Text(
                            text = timerText,
                            style = PulseCastType.labelTelemetrySm,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Expanded Quick Controls Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(Color(0xFF090E19).copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording) {
                        // Start Recording Direct CTA
                        Surface(
                            color = PrimaryContainer,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    isExpanded = false
                                    onStartRecording()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.RadioButtonChecked, contentDescription = "Start Recording", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                    } else {
                        // Pause/Resume
                        OrbControlIconButton(
                            icon = if (isCapturePaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            tint = NeonAmber,
                            onClick = {
                                orbAlpha = 1.0f
                                onTogglePause()
                            }
                        )

                        // Stop Recording
                        OrbControlIconButton(
                            icon = Icons.Default.Stop,
                            tint = ElectricRuby,
                            onClick = {
                                isExpanded = false
                                onStopRecording()
                            }
                        )
                    }

                    // Mic Mute
                    OrbControlIconButton(
                        icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        tint = if (isMicMuted) ElectricRuby else SecondaryFixedDim,
                        onClick = {
                            orbAlpha = 1.0f
                            onToggleMic()
                        }
                    )

                    // Facecam PIP Toggle
                    OrbControlIconButton(
                        icon = Icons.Default.Face,
                        tint = if (isFaceCamActive) PrimaryContainer else OnSurfaceMuted,
                        onClick = {
                            orbAlpha = 1.0f
                            onToggleFaceCam()
                        }
                    )

                    // Screenshot
                    OrbControlIconButton(
                        icon = Icons.Default.CameraAlt,
                        tint = CyberCyan,
                        onClick = {
                            orbAlpha = 1.0f
                            Toast.makeText(context, "Screenshot Captured & Saved to Vault", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // SFX Soundboard
                    OrbControlIconButton(
                        icon = Icons.Default.MusicNote,
                        tint = NeonAmber,
                        onClick = {
                            orbAlpha = 1.0f
                            Toast.makeText(context, "SFX Triggered: Airhorn / GG / Clutch", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun OrbControlIconButton(icon: ImageVector, tint: Color, onClick: () -> Unit) {
        Surface(
            color = SurfaceHigh,
            shape = CircleShape,
            modifier = Modifier.size(36.dp).clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
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
