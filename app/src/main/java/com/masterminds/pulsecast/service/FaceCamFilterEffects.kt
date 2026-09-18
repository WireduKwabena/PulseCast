package com.masterminds.pulsecast.service

import androidx.media3.common.Effect
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbFilter
import com.masterminds.pulsecast.core.FilterPreset

/**
 * Turns a pure FilterPreset into a real list of media3 Effect objects,
 * built entirely from confirmed, documented media3-effect classes:
 *
 *  - HslAdjustment.Builder().adjustHue()/.adjustSaturation() — confirmed
 *    via https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/effect/HslAdjustment.Builder.html
 *  - Brightness(value), Contrast(value) — confirmed via
 *    https://developer.android.com/reference/kotlin/androidx/media3/effect/Brightness
 *    and .../Contrast
 *  - RgbFilter.createGrayscaleFilter() — confirmed via
 *    https://developer.android.com/reference/kotlin/androidx/media3/effect/RgbFilter
 *
 * Deliberately NOT a hand-rolled RgbMatrix — see FilterPreset's doc
 * comment for why (the array layout convention wasn't something I could
 * verify without a live device).
 */
object FaceCamFilterEffects {

    fun buildEffects(preset: FilterPreset): List<Effect> {
        if (preset == FilterPreset.NONE) return emptyList()

        val effects = mutableListOf<Effect>()

        if (preset.grayscale) {
            effects.add(RgbFilter.createGrayscaleFilter())
        }

        if (preset.hueRotationDegrees != 0f || preset.saturationAdjustment != 0f) {
            effects.add(
                HslAdjustment.Builder()
                    .adjustHue(preset.hueRotationDegrees)
                    .adjustSaturation(preset.saturationAdjustment)
                    .build()
            )
        }

        if (preset.brightnessAdjustment != 0f) {
            effects.add(Brightness(preset.brightnessAdjustment))
        }

        if (preset.contrastAdjustment != 0f) {
            effects.add(Contrast(preset.contrastAdjustment))
        }

        return effects
    }
}
