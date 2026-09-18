package com.kwabena.screenrecorder.core

import com.masterminds.pulsecast.core.RecordingEvent
import com.masterminds.pulsecast.core.RecordingState
import com.masterminds.pulsecast.core.RecordingStateMachine
import kotlin.math.abs
import kotlin.system.exitProcess

private var failures = 0

private fun check(name: String, condition: Boolean) {
    if (condition) {
        println("[PASS] $name")
    } else {
        println("[FAIL] $name")
        failures++
    }
}

private fun expectThrows(name: String, block: () -> Unit) {
    try {
        block()
        println("[FAIL] $name (expected an exception, none thrown)")
        failures++
    } catch (e: Exception) {
        println("[PASS] $name (threw ${e.javaClass.simpleName})")
    }
}

fun main() {
    println("--- RecordingStateMachine ---")
    run {
        val sm = RecordingStateMachine()
        check("starts IDLE", sm.current() == RecordingState.IDLE)

        sm.transition(RecordingEvent.Start)
        check("Start -> PERMISSION_REQUESTED", sm.current() == RecordingState.PERMISSION_REQUESTED)

        sm.transition(RecordingEvent.PermissionGranted)
        check("PermissionGranted -> RECORDING", sm.current() == RecordingState.RECORDING)
        check("canPause() true while RECORDING", sm.canPause())
        check("canStop() true while RECORDING", sm.canStop())

        sm.transition(RecordingEvent.Pause)
        check("Pause -> PAUSED", sm.current() == RecordingState.PAUSED)
        check("canResume() true while PAUSED", sm.canResume())
        check("canPause() false while PAUSED", !sm.canPause())

        sm.transition(RecordingEvent.Resume)
        check("Resume -> RECORDING again", sm.current() == RecordingState.RECORDING)

        sm.transition(RecordingEvent.Stop)
        check("Stop -> STOPPING", sm.current() == RecordingState.STOPPING)
        check("isActive() false while STOPPING", !sm.isActive())

        sm.transition(RecordingEvent.Finished)
        check("Finished -> IDLE", sm.current() == RecordingState.IDLE)

        // The exact bug class this state machine exists to prevent:
        expectThrows("double-pause (RECORDING -> Pause -> Pause) rejected") {
            val sm2 = RecordingStateMachine(RecordingState.RECORDING)
            sm2.transition(RecordingEvent.Pause)
            sm2.transition(RecordingEvent.Pause)
        }
        expectThrows("stop while already IDLE rejected") {
            RecordingStateMachine(RecordingState.IDLE).transition(RecordingEvent.Stop)
        }
        expectThrows("resume while RECORDING (not paused) rejected") {
            RecordingStateMachine(RecordingState.RECORDING).transition(RecordingEvent.Resume)
        }
    }

    println("\n--- PutsAdjuster ---")
    run {
        val pts = PtsAdjuster()
        check("adjust() is a no-op before any pause", pts.adjust(5_000_000L) == 5_000_000L)

        // Recording runs 0s -> 10s, pause for 30s, resume, frame arrives at
        // raw encoder time 40s (10s of real content + 30s paused gap).
        // Corrected output timestamp should be 10s, NOT 40s — the whole
        // point of this class.
        pts.onPause(10_000_000L)
        check("isPaused() true after onPause", pts.isPaused())
        pts.onResume(40_000_000L)
        check("isPaused() false after onResume", !pts.isPaused())
        check(
            "30s pause gap correctly subtracted from output timeline",
            pts.adjust(40_000_000L) == 10_000_000L
        )

        expectThrows("double onPause() rejected") {
            val p = PtsAdjuster()
            p.onPause(1000L)
            p.onPause(2000L)
        }
        expectThrows("onResume() without a prior onPause() rejected") {
            PtsAdjuster().onResume(1000L)
        }
    }

    println("\n--- Resolution / BitrateCalculator / ResolutionScaler ---")
    run {
        expectThrows("odd width rejected") { Resolution(1921, 1080) }
        expectThrows("odd height rejected") { Resolution(1920, 1081) }
        expectThrows("zero/negative rejected") { Resolution(0, 1080) }

        val hd = Resolution(1920, 1080)
        val bitrate = BitrateCalculator.calculate(hd, frameRate = 30, quality = Quality.MEDIUM)
        // 1920*1080*30*0.10 ≈ 6,220,800 bps ≈ 6.2 Mbps — sane for 1080p30
        check(
            "1080p30 MEDIUM bitrate is in a sane real-world range (4-9 Mbps)",
            bitrate in 4_000_000..9_000_000
        )

        val higherQualityBitrate = BitrateCalculator.calculate(hd, 30, Quality.HIGH)
        check("HIGH quality yields a higher bitrate than MEDIUM", higherQualityBitrate > bitrate)

        // A typical tall phone screen, e.g. 1440x3120 (Pixel-class device)
        val scaledDown = ResolutionScaler.scaleToTier(1440, 3120, Quality.MEDIUM)
        check("scaling down keeps dimensions even", scaledDown.width % 2 == 0 && scaledDown.height % 2 == 0)
        check("scaling down respects the long-edge target (1080 for MEDIUM)", maxOf(scaledDown.width, scaledDown.height) <= 1080)
        val expectedRatio = 1440.0 / 3120.0
        val actualRatio = scaledDown.width.toDouble() / scaledDown.height.toDouble()
        check("aspect ratio preserved within rounding", abs(expectedRatio - actualRatio) < 0.01)

        val notScaled = ResolutionScaler.scaleToTier(640, 480, Quality.HIGH)
        check(
            "a screen already smaller than the target tier isn't scaled up",
            notScaled.width == 640 && notScaled.height == 480
        )
    }

    println("\n--- FilterPreset ---")
    run {
        for (preset in FilterPreset.entries) {
            check(
                "${preset.label}: brightness within media3's valid [-1,1] range",
                preset.brightnessAdjustment in FilterPreset.BRIGHTNESS_CONTRAST_MIN..FilterPreset.BRIGHTNESS_CONTRAST_MAX
            )
            check(
                "${preset.label}: contrast within media3's valid [-1,1] range",
                preset.contrastAdjustment in FilterPreset.BRIGHTNESS_CONTRAST_MIN..FilterPreset.BRIGHTNESS_CONTRAST_MAX
            )
            check(
                "${preset.label}: saturation within media3's valid [-100,100] range",
                preset.saturationAdjustment in FilterPreset.SATURATION_MIN..FilterPreset.SATURATION_MAX
            )
        }

        check("NONE preset is a genuine no-op (all adjustments zero, not grayscale)",
            FilterPreset.NONE.hueRotationDegrees == 0f &&
            FilterPreset.NONE.saturationAdjustment == 0f &&
            FilterPreset.NONE.brightnessAdjustment == 0f &&
            FilterPreset.NONE.contrastAdjustment == 0f &&
            !FilterPreset.NONE.grayscale
        )

        check("GRAYSCALE preset actually sets the grayscale flag", FilterPreset.GRAYSCALE.grayscale)
        check("only GRAYSCALE sets the grayscale flag", FilterPreset.entries.toTypedArray().count { it.grayscale } == 1)

        check("WARM and COOL rotate hue in opposite directions",
            FilterPreset.WARM.hueRotationDegrees == -FilterPreset.COOL.hueRotationDegrees &&
            FilterPreset.WARM.hueRotationDegrees != 0f
        )

        val labels = FilterPreset.entries.map { it.label }
        check("every preset has a unique label", labels.size == labels.toSet().size)
    }

    println("\n" + if (failures == 0) "ALL TESTS PASSED" else "$failures TEST(S) FAILED")
    if (failures > 0) exitProcess(1)
}
