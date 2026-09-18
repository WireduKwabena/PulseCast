package com.masterminds.pulsecast.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.media3.effect.Media3Effect
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.masterminds.pulsecast.core.FilterPreset
import com.masterminds.pulsecast.ui.theme.PulseCastTheme

class FaceCamOverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "facecam"
        private const val NOTIFICATION_ID = 2
        private const val TAG = "FaceCamOverlayService"
    }

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var media3Effect: Media3Effect? = null
    private var currentFilter by mutableStateOf(FilterPreset.NONE)

    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, buildNotification(), foregroundServiceType())
        showBubble()
    }

    private fun foregroundServiceType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        } else {
            0
        }

    private fun showBubble() {
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
            gravity = Gravity.BOTTOM or Gravity.START
            x = 32
            y = 200
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FaceCamOverlayService)
            setViewTreeViewModelStoreOwner(this@FaceCamOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FaceCamOverlayService)
            
            setContent {
                PulseCastTheme {
                    FaceCamBubble(
                        onClose = { stopSelf() },
                        onFilter = { cycleFilter() },
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y -= dy.toInt()
                            windowManager.updateViewLayout(this, params)
                        }
                    )
                }
            }
        }

        windowManager.addView(composeView, params)
    }

    @Composable
    private fun FaceCamBubble(
        onClose: () -> Unit,
        onFilter: () -> Unit,
        onDrag: (Float, Float) -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF00E5FF), CircleShape),
                color = Color.Black
            ) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            bindCamera(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onFilter,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }

    private fun cycleFilter() {
        val presets = FilterPreset.values()
        currentFilter = presets[(currentFilter.ordinal + 1) % presets.size]
        media3Effect?.setEffects(FaceCamFilterEffects.buildEffects(currentFilter))
        Toast.makeText(this, "Filter: ${currentFilter.label}", Toast.LENGTH_SHORT).show()
    }

    private fun bindCamera(previewView: PreviewView) {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val effect = Media3Effect(
                    this,
                    CameraEffect.PREVIEW,
                    ContextCompat.getMainExecutor(this)
                ) { error -> Log.e(TAG, "Media3Effect error", error) }
                effect.setEffects(FaceCamFilterEffects.buildEffects(currentFilter))
                media3Effect = effect

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addEffect(effect)
                    .build()

                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, useCaseGroup)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind front camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Face cam",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Face cam active")
            .setSmallIcon(R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        media3Effect?.close()
        cameraProvider?.unbindAll()
        composeView?.let { windowManager.removeView(it) }
        _viewModelStore.clear()
        super.onDestroy()
    }
}
