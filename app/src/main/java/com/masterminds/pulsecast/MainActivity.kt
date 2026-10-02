package com.masterminds.pulsecast

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.core.CaptureSessionStore
import com.masterminds.pulsecast.service.LiveHUDOverlayService
import com.masterminds.pulsecast.service.ScreenRecordService
import com.masterminds.pulsecast.streaming.BroadcastDestinationStore
import com.masterminds.pulsecast.ui.CaptureViewModel
import com.masterminds.pulsecast.ui.navigation.PulseCastNavigation
import com.masterminds.pulsecast.ui.theme.PulseCastTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var projectionManager: MediaProjectionManager
    private val captureViewModel: CaptureViewModel by viewModels()
    private var pendingRecordingAction: (() -> Unit)? = null
    private var pendingBroadcastLaunch = false

    private val microphonePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            continueAfterMicrophonePermission()
        } else {
            pendingBroadcastLaunch = false
            pendingRecordingAction = null
            Toast.makeText(this, "Microphone permission is required for the selected audio source.", Toast.LENGTH_LONG).show()
        }
    }

    private val legacyStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingRecordingAction?.invoke()
            pendingRecordingAction = null
        } else {
            pendingBroadcastLaunch = false
            pendingRecordingAction = null
            Toast.makeText(this, "Storage permission is required to save recordings on Android 9 and below.", Toast.LENGTH_LONG).show()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            continueAfterOverlayPermission()
        } else {
            pendingBroadcastLaunch = false
            pendingRecordingAction = null
            Toast.makeText(this, "Allow display over other apps to show recording controls during capture.", Toast.LENGTH_LONG).show()
        }
    }

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) startRecordingService(result.resultCode, result.data!!)
        else {
            pendingBroadcastLaunch = false
            captureViewModel.setRecording(false)
            Toast.makeText(this, "Screen recording permission denied.", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        const val ACTION_START_RECORDING_FROM_ORB = "com.masterminds.pulsecast.ACTION_START_RECORDING_FROM_ORB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(MediaProjectionManager::class.java)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            startService(Intent(this, LiveHUDOverlayService::class.java))
        }
        handleIntent(intent)
        setContent {
            val isRecording by captureViewModel.isRecording.collectAsState()
            val isBroadcasting by CaptureSessionStore.isBroadcasting.collectAsState()
            var countdown by rememberSaveable { mutableIntStateOf(0) }
            LaunchedEffect(countdown) {
                if (countdown > 0) {
                    delay(1_000)
                    countdown--
                    if (countdown == 0) {
                        captureViewModel.setRecording(false)
                        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
                    }
                }
            }
            PulseCastTheme {
                Box(Modifier.fillMaxSize()) {
                    PulseCastNavigation(
                        isRecording = isRecording,
                        onRecord = {
                            if (isRecording) {
                                stopRecordingService()
                            } else requestRecordingStart { countdown = 3 }
                        },
                        isBroadcasting = isBroadcasting,
                        onStartBroadcast = {
                            val destinationLoadError = BroadcastDestinationStore.initializationError.value
                            if (destinationLoadError != null) {
                                Toast.makeText(this@MainActivity, destinationLoadError, Toast.LENGTH_LONG).show()
                                false
                            } else if (!BroadcastDestinationStore.isInitialized.value) {
                                Toast.makeText(this@MainActivity, "Loading saved ingest destination…", Toast.LENGTH_SHORT).show()
                                false
                            } else if (BroadcastDestinationStore.destinations.value.isEmpty()) {
                                Toast.makeText(this@MainActivity, "Arm at least one RTMP destination before going live.", Toast.LENGTH_LONG).show()
                                false
                            } else {
                                pendingBroadcastLaunch = true
                                requestRecordingStart { screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent()) }
                                true
                            }
                        },
                        onStopCapture = { stopRecordingService() },
                        captureViewModel = captureViewModel
                    )
                    if (countdown > 0) CountdownOverlay(countdown)
                }
            }
        }
        captureViewModel.refreshStorage(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_START_RECORDING_FROM_ORB) {
            requestRecordingStart {
                screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
            }
        }
    }

    private fun startRecordingService(resultCode: Int, data: Intent) {
        val resolution = captureViewModel.resolution.value
        val fps = captureViewModel.fps.value
        val audioMode = captureViewModel.audioMode.value
        val destinations = if (pendingBroadcastLaunch) BroadcastDestinationStore.destinations.value else emptyList()

        try {
            ContextCompat.startForegroundService(this, Intent(this, ScreenRecordService::class.java).apply {
                action = ScreenRecordService.ACTION_START
                putExtra(ScreenRecordService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenRecordService.EXTRA_RESULT_DATA, data)
                putExtra(ScreenRecordService.EXTRA_RESOLUTION, resolution)
                putExtra(ScreenRecordService.EXTRA_FPS, fps)
                putExtra(ScreenRecordService.EXTRA_AUDIO_MODE, audioMode.name)
                putExtra(ScreenRecordService.EXTRA_STREAM_LABELS, destinations.map { it.label }.toTypedArray())
                putExtra(ScreenRecordService.EXTRA_STREAM_IDS, destinations.map { it.id }.toTypedArray())
                putExtra(ScreenRecordService.EXTRA_STREAM_URLS, destinations.map { it.rtmpUrl }.toTypedArray())
                putExtra(ScreenRecordService.EXTRA_STREAM_BITRATE_KBPS, destinations.singleOrNull()?.bitrateKbps ?: 0)
            })
            pendingBroadcastLaunch = false
            startService(Intent(this, LiveHUDOverlayService::class.java))
            moveTaskToBack(true)
        } catch (error: RuntimeException) {
            pendingBroadcastLaunch = false
            captureViewModel.setRecording(false)
            Toast.makeText(this, "Could not start capture: ${error.localizedMessage ?: "service unavailable"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestRecordingStart(onReady: () -> Unit) {
        pendingRecordingAction = onReady
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            overlayPermissionLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
            return
        }
        continueAfterOverlayPermission()
    }

    private fun continueAfterOverlayPermission() {
        val audioMode = captureViewModel.audioMode.value
        val needsMicrophone = audioMode == AudioMode.MIC_ONLY || audioMode == AudioMode.INTERNAL_AND_MIC
        if (needsMicrophone && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            continueAfterMicrophonePermission()
        }
    }

    private fun continueAfterMicrophonePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            legacyStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        pendingRecordingAction?.invoke()
        pendingRecordingAction = null
    }

    private fun stopRecordingService() {
        startService(Intent(this, ScreenRecordService::class.java).setAction(ScreenRecordService.ACTION_STOP))
        stopService(Intent(this, LiveHUDOverlayService::class.java))
    }

}

@Composable
private fun CountdownOverlay(value: Int) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .72f)), contentAlignment = Alignment.Center) {
        Text(value.toString(), style = MaterialTheme.typography.displayLarge, color = Color(0xFFFF2D55), fontWeight = FontWeight.Black, fontSize = 120.sp)
    }
}
