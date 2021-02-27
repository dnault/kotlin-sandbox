package com.couchbase.client.kotlin.kv

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

public sealed class Expiry {
    internal abstract fun encode(): Long

    public object None : Expiry() {
        override fun encode() = 0L;
        override fun toString(): String = "None"
    }

    public data class Absolute internal constructor(public val instant: Instant) : Expiry() {
        override fun encode() = instant.epochSecond.let {
            // Zero would mean "no expiry". Negative would underflow and expire far in the future.
            if (it <= 0) EXPIRE_IMMEDIATELY else it
        }
    }

    public data class Relative internal constructor(val duration: Duration) : Expiry() {
        override fun encode(): Long {
            val seconds: Long = duration.seconds

            // Zero would mean "no expiry". Negative would underflow and expire far in the future.
            if (seconds <= 0) return EXPIRE_IMMEDIATELY

            // If it's under the threshold, let the server convert it to an absolute time.
            // Otherwise we need to do the conversion on the client.
            return if (seconds < RELATIVE_EXPIRY_CUTOFF_SECONDS) seconds
            else currentTimeSeconds() + seconds
        }

        private fun currentTimeSeconds() = MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    public companion object {
        public fun none(): Expiry = None
        public fun absolute(instant: Instant): Expiry = Absolute(instant)
        public fun relative(duration: Duration): Expiry = Relative(duration)
    }
}

// Durations longer than this must be converted to an absolute
// epoch second before being passed to the server.
private val RELATIVE_EXPIRY_CUTOFF_SECONDS = DAYS.toSeconds(30).toInt()

// An arbitrary value that triggers immediate expiration.
private val EXPIRE_IMMEDIATELY = DAYS.toSeconds(31)
