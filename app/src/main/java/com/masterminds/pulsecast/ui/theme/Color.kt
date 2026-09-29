package com.masterminds.pulsecast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Broadcast Obsidian" palette — sourced from DESIGN.md's prose "Colors"
 * section. This palette is optimized for OLED dark mode.
 */

// Surface Layers
val BgBase = Color(0xFF0A0D14)
val SurfaceLow = Color(0xFF121722)
val SurfaceMid = Color(0xFF1A2130)
val SurfaceHigh = Color(0xFF242D40)
val StrokeSubtle = Color(0x14FFFFFF) // rgba(255,255,255,0.08)
val StrokeHighlight = Color(0x1FFFFFFF) // rgba(255,255,255,0.12)

// Signal Spectrum
val ElectricRuby = Color(0xFFFF2D55)
val CyberCyan = Color(0xFF00E5FF)
val NeonAmber = Color(0xFFFFB300)
val SignalGreen = Color(0xFF00E676)

// Text
val OnSurface = Color(0xFFFFFFFF)
val OnSurfaceMuted = Color(0xFF8A96AA)

// Glow colors
val RubyGlow = Color(0x73FF2D55)
val CyanGlow = Color(0x7300E5FF)

// Material 3 Mappings
val Primary = ElectricRuby
val OnPrimary = Color(0xFF680019) // Dark red/black per DESIGN.md
val PrimaryContainer = Color(0xFFFF5167)
val Secondary = CyberCyan
val OnSecondary = BgBase
val SecondaryContainer = Color(0xFF00E3FD)
val SecondaryFixedDim = Color(0xFF00DAF3)
val Tertiary = NeonAmber
val OnTertiary = BgBase
val SurfaceVariant = SurfaceMid
val OnSurfaceVariant = OnSurfaceMuted
val Error = ElectricRuby
val OnError = BgBase
val ErrorContainer = Color(0xFF93000A)
val Outline = Color(0xFFAD8888)
val OutlineVariant = Color(0xFF5D3F40)
