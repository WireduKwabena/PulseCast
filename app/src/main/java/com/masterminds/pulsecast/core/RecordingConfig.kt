package com.masterminds.pulsecast.core

enum class Quality { LOW, MEDIUM, HIGH, ORIGINAL }

data class Resolution(val width: Int, val height: Int) {
    init {
        require(width > 0 && height > 0) { "Resolution must be positive: ${width}x$height" }
        // Several OEM hardware AVC encoders crash or silently fail on odd
        // width/height — this is a real, documented device-specific bug,
        // not theoretical. Enforcing even dimensions here means it's
        // structurally impossible to construct a Resolution that triggers it.
        require(width % 2 == 0 && height % 2 == 0) {
            "Encoder requires even width/height, got ${width}x$height"
        }
    }
}

object BitrateCalculator {
    // Bits-per-pixel factor per quality tier, same rough ballpark as
    // YouTube's own published upload bitrate recommendations for H.264.
    private fun bitsPerPixel(quality: Quality): Double = when (quality) {
        Quality.LOW -> 0.07
        Quality.MEDIUM -> 0.10
        Quality.HIGH -> 0.14
        Quality.ORIGINAL -> 0.20
    }

    fun calculate(resolution: Resolution, frameRate: Int, quality: Quality): Int {
        require(frameRate > 0) { "Frame rate must be positive" }
        val pixelsPerSecond = resolution.width.toLong() * resolution.height.toLong() * frameRate
        return (pixelsPerSecond * bitsPerPixel(quality)).toInt()
    }
}

object ResolutionScaler {
    /**
     * Scales the device's actual screen size down to a target quality tier
     * while preserving aspect ratio, always landing on even dimensions.
     */
    fun scaleToTier(actualWidth: Int, actualHeight: Int, quality: Quality): Resolution {
        val targetLongEdge = when (quality) {
            Quality.LOW -> 720
            Quality.MEDIUM -> 1080
            Quality.HIGH -> 1440
            Quality.ORIGINAL -> maxOf(actualWidth, actualHeight)
        }
        val longEdge = maxOf(actualWidth, actualHeight)
        if (longEdge <= targetLongEdge) {
            return Resolution(evenify(actualWidth), evenify(actualHeight))
        }
        val scale = targetLongEdge.toDouble() / longEdge
        return Resolution(evenify((actualWidth * scale).toInt()), evenify((actualHeight * scale).toInt()))
    }

    private fun evenify(value: Int): Int = if (value % 2 == 0) value else value - 1
}
