package com.masterminds.pulsecast.ui.capturehud


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.masterminds.pulsecast.ui.theme.*
import kotlinx.coroutines.delay

/**
 * CaptureHub: The primary recording dashboard for PulseCast Studio.
 *
 * Refactored to align with the Broadcast Obsidian design system.
 * Implements high-precision telemetry, dual-state recording triggers,
 * and adaptive studio configuration grids.
 */

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun CaptureHubPreview() {
    PulseCastTheme {
        CaptureHubScreen(
            isRecording = false,
            onRecordClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun CaptureHubRecordingPreview() {
    PulseCastTheme {
        CaptureHubScreen(
            isRecording = true,
            onRecordClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureHubScreen(
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    onNavigateToBroadcast: () -> Unit = {},
    onNavigateToMixer: () -> Unit = {},
    onNavigateToOverlays: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToEditor: () -> Unit = {}
){
    var countdownValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(countdownValue) {
        if (countdownValue > 0) {
            delay(1000)
            countdownValue--
            if (countdownValue == 0) {
                onRecordClick()
            }
        }
    }


}
