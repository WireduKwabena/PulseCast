package com.masterminds.pulsecast.streaming

/**
 * Exponential backoff for reconnecting a dropped RTMP connection.
 * Deliberately pure math — no network/threading involved — so the
 * escalation curve and give-up threshold are directly testable.
 */
object ReconnectBackoff {
    private const val BASE_DELAY_MS = 1_000L
    private const val MAX_DELAY_MS = 30_000L
    const val MAX_ATTEMPTS = 6

    fun delayForAttempt(attempt: Int): Long {
        require(attempt >= 1) { "attempt must be >= 1, got $attempt" }
        val exponential = BASE_DELAY_MS * (1L shl (attempt - 1).coerceAtMost(10))
        return exponential.coerceAtMost(MAX_DELAY_MS)
    }

    fun shouldGiveUp(attempt: Int): Boolean = attempt > MAX_ATTEMPTS
}
