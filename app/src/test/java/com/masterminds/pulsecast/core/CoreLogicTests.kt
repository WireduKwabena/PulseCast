package com.masterminds.pulsecast.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingStateMachineTest {
    @Test
    fun recordingCanPauseResumeAndStop() {
        val machine = RecordingStateMachine()
        assertEquals(RecordingState.IDLE, machine.current())

        machine.transition(RecordingEvent.Start)
        machine.transition(RecordingEvent.PermissionGranted)
        assertEquals(RecordingState.RECORDING, machine.current())

        machine.transition(RecordingEvent.Pause)
        assertEquals(RecordingState.PAUSED, machine.current())
        machine.transition(RecordingEvent.Resume)
        machine.transition(RecordingEvent.Stop)
        machine.transition(RecordingEvent.Finished)

        assertEquals(RecordingState.IDLE, machine.current())
    }

    @Test(expected = InvalidTransitionException::class)
    fun cannotResumeWhenNotPaused() {
        RecordingStateMachine().transition(RecordingEvent.Resume)
    }

    @Test
    fun deniedPermissionReturnsToIdle() {
        val machine = RecordingStateMachine()
        machine.transition(RecordingEvent.Start)
        machine.transition(RecordingEvent.PermissionDenied)
        assertEquals(RecordingState.IDLE, machine.current())
        assertFalse(machine.isActive())
    }
}

class PtsAdjusterTest {
    @Test
    fun removesPauseDurationFromOutputTimestamps() {
        val adjuster = PtsAdjuster()
        adjuster.onPause(10_000_000L)
        adjuster.onResume(40_000_000L)
        assertEquals(10_000_000L, adjuster.adjust(40_000_000L))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsResumeWithoutPause() {
        PtsAdjuster().onResume(1_000L)
    }
}

class RecordingProfileMathTest {
    @Test
    fun resolutionMustBePositiveAndEven() {
        assertEquals(Resolution(1920, 1080), Resolution(1920, 1080))
        assertIllegalArgument { Resolution(1921, 1080) }
        assertIllegalArgument { Resolution(1920, 1081) }
    }

    @Test
    fun scalerPreservesAspectAndDoesNotUpscale() {
        val scaled = ResolutionScaler.scaleToTier(1440, 3120, Quality.MEDIUM)
        assertTrue(maxOf(scaled.width, scaled.height) <= 1080)
        assertEquals(0, scaled.width % 2)
        assertEquals(0, scaled.height % 2)
        assertEquals(640, ResolutionScaler.scaleToTier(640, 480, Quality.HIGH).width)
    }

    @Test
    fun bitrateIncreasesWithQualityAndFrameRate() {
        val resolution = Resolution(1920, 1080)
        val baseline = BitrateCalculator.calculate(resolution, 30, Quality.MEDIUM)
        assertTrue(BitrateCalculator.calculate(resolution, 60, Quality.MEDIUM) > baseline)
        assertTrue(BitrateCalculator.calculate(resolution, 30, Quality.HIGH) > baseline)
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected validation failure.
        }
    }
}
