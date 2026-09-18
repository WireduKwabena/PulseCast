package com.masterminds.pulsecast.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.masterminds.pulsecast.ui.theme.ElectricRuby
import com.masterminds.pulsecast.ui.theme.PulseCastTheme
import com.masterminds.pulsecast.ui.theme.Secondary
import com.masterminds.pulsecast.ui.theme.SurfaceVariant

class OverlayControlsService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private var composeView: ComposeView? = null
    private lateinit var windowManager: WindowManager
    private var isPaused by mutableStateOf(false)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showOverlay() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 100
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayControlsService)
            setViewTreeViewModelStoreOwner(this@OverlayControlsService)
            setViewTreeSavedStateRegistryOwner(this@OverlayControlsService)

            setContent {
                PulseCastTheme {
                    FloatingActionBall(
                        isPaused = isPaused,
                        onPauseResume = {
                            isPaused = !isPaused
                            val action = if (isPaused) ScreenRecordService.ACTION_PAUSE else ScreenRecordService.ACTION_RESUME
                            sendServiceAction(action)
                        },
                        onStop = {
                            sendServiceAction(ScreenRecordService.ACTION_STOP)
                            stopSelf()
                        },
                        onDrag = { dx, dy ->
                            params.x -= dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(this, params)
                        }
                    )
                }
            }
        }

        windowManager.addView(composeView, params)
    }

    @Composable
    private fun FloatingActionBall(
        isPaused: Boolean,
        onPauseResume: () -> Unit,
        onStop: () -> Unit,
        onDrag: (Float, Float) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .padding(8.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .background(SurfaceVariant.copy(alpha = 0.9f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = onPauseResume, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onStop, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Stop", tint = ElectricRuby)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // The Ball
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
                    .border(2.dp, Secondary.copy(alpha = 0.5f), CircleShape)
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.BlurOn, contentDescription = null, tint = Secondary)
                // Tally Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .background(ElectricRuby, CircleShape)
                )
            }
        }
    }

    private fun sendServiceAction(action: String) {
        startService(Intent(this, ScreenRecordService::class.java).setAction(action))
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let { windowManager.removeView(it) }
        _viewModelStore.clear()
        super.onDestroy()
    }
}
