package com.masterminds.pulsecast.core

/**
 * Tracks accumulated paused duration so encoder frame timestamps can be
 * corrected before muxing. Without this, pausing for 30 seconds bakes a
 * 30-second frozen gap into the output file's timeline — one of the most
 * common visible bugs in recorders that claim to "support" pause/resume.
 *
 * Pure logic, no MediaCodec/Android dependency — directly unit-testable
 * with fake timestamps.
 */
class PtsAdjuster {
    private var totalPausedDurationUs: Long = 0
    private var pausedAtUs: Long? = null

    fun onPause(nowUs: Long) {
        check(pausedAtUs == null) { "onPause() called while already paused" }
        pausedAtUs = nowUs
    }

    fun onResume(nowUs: Long) {
        val pausedAt = checkNotNull(pausedAtUs) { "onResume() called while not paused" }
        totalPausedDurationUs += (nowUs - pausedAt)
        pausedAtUs = null
    }

    /** Maps a raw encoder timestamp onto the corrected, gap-free output timeline. */
    fun adjust(rawTimestampUs: Long): Long = rawTimestampUs - totalPausedDurationUs

    fun isPaused(): Boolean = pausedAtUs != null
}
