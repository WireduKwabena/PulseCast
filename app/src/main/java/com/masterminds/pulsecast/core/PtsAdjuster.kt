package com.masterminds.pulsecast.core

/**
 * Tracks accumulated paused duration and normalizes raw Surface/MediaCodec
 * timestamps (which start at system uptime ~15+ hours) down to 0-based time.
 * This guarantees the output MP4 container header (mvhd atom) records the
 * exact real duration (e.g. 00:55) in Android Gallery, Photos, VLC, and YouTube.
 */
class PtsAdjuster {
    @Volatile private var basePtsUs: Long = -1L
    @Volatile private var totalPausedDurationUs: Long = 0L
    @Volatile private var pausedAtUs: Long? = null

    @Synchronized
    fun adjust(rawTimestampUs: Long): Long {
        if (basePtsUs == -1L) {
            basePtsUs = rawTimestampUs
        }
        val relativePts = rawTimestampUs - basePtsUs
        return (relativePts - totalPausedDurationUs).coerceAtLeast(0L)
    }

    @Synchronized
    fun onPause(nowUs: Long) {
        if (pausedAtUs == null) {
            pausedAtUs = nowUs
        }
    }

    @Synchronized
    fun onResume(nowUs: Long) {
        val pausedAt = pausedAtUs
        if (pausedAt != null) {
            totalPausedDurationUs += (nowUs - pausedAt)
            pausedAtUs = null
        }
    }

    @Synchronized
    fun reset() {
        basePtsUs = -1L
        totalPausedDurationUs = 0L
        pausedAtUs = null
    }

    fun isPaused(): Boolean = pausedAtUs != null
}
