package com.masterminds.pulsecast.ui.ui_library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masterminds.pulsecast.ui.theme.CyberCyan
import com.masterminds.pulsecast.ui.theme.ElectricRuby
import com.masterminds.pulsecast.ui.theme.SurfaceLow
/*
* Reusable AppBar Variants for all usecases.
* */
@Composable
fun PulseCastAppBar(
    onHome: () -> Unit,
    onVault: () -> Unit,
    onDiagnostics: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = SurfaceLow, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.heightIn(min = 64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).clickable(onClick = onHome),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = "PulseCast home", tint = ElectricRuby)
                Spacer(Modifier.width(8.dp))
                Text("PulseCast Pro", style = MaterialTheme.typography.titleMedium)
            }
            AssistChip(
                onClick = onVault,
                label = { Text("128 GB Free") },
                leadingIcon = { Icon(Icons.Default.Storage, null, Modifier.size(16.dp), CyberCyan) }
            )
            IconButton(onClick = onDiagnostics) {
                Icon(Icons.Default.QueryStats, "Open diagnostics", tint = CyberCyan)
            }
            IconButton(onClick = onProfile) {
                Surface(shape = CircleShape, color = ElectricRuby) {
                    Icon(Icons.Default.Person, "Open creator profile", Modifier.padding(6.dp), Color.White)
                }
            }
        }
    }
}
