package com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Compact overlay entry point used by [LiveHUDOverlayService]. */
@Composable
fun LiveChatDrawer(onClose: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Live chat")
            Button(onClick = onClose) { Text("Close") }
        }
    }
}

