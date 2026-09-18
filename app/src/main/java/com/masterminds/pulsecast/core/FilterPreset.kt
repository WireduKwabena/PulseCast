package com.masterminds.pulsecast.core

/**
 * Filter preset parameters — pure data, zero media3/Android dependency.
 * FaceCamFilterEffects (Android-specific) turns these into real media3
 * Effect objects (HslAdjustment, RgbFilter, Contrast, Brightness),
 * applied to the CameraX face-cam preview via Media3Effect.
 *
 * Deliberately built entirely from confirmed, documented media3-effect
 * classes rather than a hand-rolled 4x4 color matrix — RgbMatrix's exact
 * array layout convention (row- vs column-major) isn't something I could
 * verify without a live device, so composing only fully-documented
 * classes (HslAdjustment, Brightness, Contrast, RgbFilter) sidesteps that
 * uncertainty entirely rather than guessing at it.
 *
 * One real remaining uncertainty: hue rotation *direction*.
 * HslAdjustment.adjustHue()'s sign convention for "which way is warmer"
 * isn't something I could confirm without visually testing on a device —
 * warm uses -15°, cool uses +15° here as a best-effort guess. If they
 * look swapped once you can actually see the preview, just flip both
 * signs in the enum below.
 */
enum class FilterPreset(
    val label: String,
    val hueRotationDegrees: Float,
    val saturationAdjustment: Float,
    val brightnessAdjustment: Float,
    val contrastAdjustment: Float,
    val grayscale: Boolean,
) {
    NONE("None", 0f, 0f, 0f, 0f, false),
    WARM("Warm", -15f, 0f, 0.05f, 0f, false),
    COOL("Cool", 15f, 0f, 0f, 0.05f, false),
    VIVID("Vivid", 0f, 40f, 0f, 0.15f, false),
    VINTAGE("Vintage", 0f, -30f, 0.08f, -0.1f, false),
    GRAYSCALE("B&W", 0f, 0f, 0f, 0f, true);

    companion object {
        // media3's documented valid ranges — Brightness/Contrast: [-1, 1],
        // HslAdjustment.adjustSaturation: [-100, 100].
        const val BRIGHTNESS_CONTRAST_MIN = -1f
        const val BRIGHTNESS_CONTRAST_MAX = 1f
        const val SATURATION_MIN = -100f
        const val SATURATION_MAX = 100f
    }
}
