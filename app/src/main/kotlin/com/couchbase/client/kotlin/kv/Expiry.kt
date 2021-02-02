package com.couchbase.client.kotlin.kv

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

public sealed class Expiry {
    internal abstract fun encode(): Long

    internal object None : Expiry() {
        override fun encode(): Long = 0;
        override fun toString(): String = "None"
    }

    internal data class Absolute internal constructor(val instant: Instant) : Expiry() {
        override fun encode(): Long = instant.epochSecond
    }

    internal data class Relative internal constructor(val duration: Duration) : Expiry() {
        override fun encode(): Long {
            val seconds: Long = duration.seconds
            if (seconds < RELATIVE_EXPIRY_CUTOFF_SECONDS) return seconds
            return currentTimeSeconds() + seconds
        }

        private fun currentTimeSeconds() = MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    public companion object {
        private val RELATIVE_EXPIRY_CUTOFF_SECONDS = DAYS.toSeconds(30).toInt()

        public fun none(): Expiry = None
        public fun absolute(instant: Instant): Expiry = Absolute(instant)
        public fun relative(duration: Duration): Expiry = Relative(duration)
    }
}
