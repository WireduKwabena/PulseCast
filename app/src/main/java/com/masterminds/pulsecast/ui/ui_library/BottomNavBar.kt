package com.masterminds.pulsecast.ui.ui_library

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.ui.theme.*

@Composable
fun PulseCastBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onQuickOrb: () -> Unit,
    routes: List<BottomNavRoute>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(80.dp), // Height 64 + peak 16
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base bar
        Surface(
            color = SurfaceLow.copy(alpha = 0.9f),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First two items
                routes.take(2).forEach { route ->
                    BottomNavItem(
                        selected = currentRoute == route.route,
                        onClick = { onNavigate(route.route) },
                        icon = route.icon,
                        label = route.label
                    )
                }

                // Spacer for center FAB
                Spacer(Modifier.width(64.dp))

                // Last two items
                routes.drop(2).forEach { route ->
                    BottomNavItem(
                        selected = currentRoute == route.route,
                        onClick = { onNavigate(route.route) },
                        icon = route.icon,
                        label = route.label
                    )
                }
            }
        }

        // Animated Center FAB
        CenterQuickOrb(
            onClick = onQuickOrb,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp)
        )
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) ElectricRuby else OnSurfaceMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = PulseCastType.bodySm,
            color = if (selected) ElectricRuby else OnSurfaceMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun CenterQuickOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opacity"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer Ping Animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .border(2.dp, ElectricRuby.copy(alpha = opacity), CircleShape)
        )

        // Main Button Surface - Using onClick here for better reliability
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = ElectricRuby,
            shadowElevation = 12.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricRuby,
                                Color(0xFFFF5167)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Quick Orb",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

data class BottomNavRoute(
    val route: String,
    val label: String,
    val icon: ImageVector
)
