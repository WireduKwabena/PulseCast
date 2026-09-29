package com.masterminds.pulsecast.ui.ui_library

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterminds.pulsecast.R
import com.masterminds.pulsecast.ui.theme.*

@Composable
fun PulseCastAppBar(
    onHome: () -> Unit,
    onVault: () -> Unit,
    onDiagnostics: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceLow.copy(alpha = 0.85f),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .height(84.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo and Brand Info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onHome),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulseCastLogo(modifier = Modifier.size(32.dp))
                
                Spacer(Modifier.width(12.dp))
                
                Column(verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PulseCast Studio",
                            style = PulseCastType.headlineSm,
                            color = OnSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(Modifier.width(6.dp))
                        ProBadge()
                    }
                    
                    Spacer(Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onVault)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "storage_dot")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = dotAlpha))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "128 GB Free • ~18h 45m",
                            style = PulseCastType.labelTelemetrySm,
                            color = CyberCyan
                        )
                    }
                }
            }

            // Right: Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDiagnostics,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Diagnostics",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                Surface(
                    onClick = onProfile,
                    shape = CircleShape,
                    color = ElectricRuby,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = OnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulseCastLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.pulsecast_back),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.pulsecast_fore),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ProBadge() {
    Surface(
        color = NeonAmber.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier.height(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "PRO",
                style = PulseCastType.labelTelemetrySm,
                color = NeonAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}
