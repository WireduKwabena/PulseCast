package com.masterminds.pulsecast

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
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
import com.masterminds.pulsecast.service.LiveHUDOverlayService
import com.masterminds.pulsecast.service.ScreenRecordService
import com.masterminds.pulsecast.ui.CaptureViewModel
import com.masterminds.pulsecast.ui.navigation.PulseCastNavigation
import com.masterminds.pulsecast.ui.theme.PulseCastTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var projectionManager: MediaProjectionManager
    private val captureViewModel: CaptureViewModel by viewModels()

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) startRecordingService(result.resultCode, result.data!!)
        else Toast.makeText(this, "Screen recording permission denied.", Toast.LENGTH_SHORT).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(MediaProjectionManager::class.java)
        setContent {
            val isRecording by captureViewModel.isRecording.collectAsState()
            var countdown by rememberSaveable { mutableIntStateOf(0) }
            LaunchedEffect(countdown) {
                if (countdown > 0) {
                    delay(1_000)
                    countdown--
                    if (countdown == 0) {
                        captureViewModel.toggleRecording()
                        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
                    }
                }
            }
            PulseCastTheme {
                Box(Modifier.fillMaxSize()) {
                    PulseCastNavigation(isRecording = isRecording, onRecord = {
                        if (isRecording) {
                            stopRecordingService()
                            captureViewModel.toggleRecording()
                        } else countdown = 3
                    })
                    if (countdown > 0) CountdownOverlay(countdown)
                }
            }
        }
    }

    private fun startRecordingService(resultCode: Int, data: Intent) {
        ContextCompat.startForegroundService(this, Intent(this, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_START
            putExtra(ScreenRecordService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenRecordService.EXTRA_RESULT_DATA, data)
        })
        startService(Intent(this, LiveHUDOverlayService::class.java))
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
