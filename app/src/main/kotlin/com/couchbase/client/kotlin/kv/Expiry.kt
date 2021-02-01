package com.couchbase.client.kotlin.kv

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

public sealed class Expiry {
    internal abstract fun encode(): Long

    public class Absolute internal constructor(public val instant: Instant) : Expiry() {
        override fun encode(): Long = instant.epochSecond
        override fun toString(): String = "Absolute(instant=$instant)"
    }

    public class Relative internal constructor(public val duration: Duration) : Expiry() {
        override fun encode(): Long {
            val seconds: Long = duration.seconds
            if (seconds < RELATIVE_EXPIRY_CUTOFF_SECONDS) return seconds
            return currentTimeSeconds() + seconds
        }

        private fun currentTimeSeconds() = MILLISECONDS.toSeconds(System.currentTimeMillis())
        override fun toString(): String = "Relative(duration=$duration)"
    }

    public companion object {
        public fun absolute(instant: Instant): Absolute = Absolute(instant)
        public fun relative(duration: Duration): Relative = Relative(duration)
    }
}

private val RELATIVE_EXPIRY_CUTOFF_SECONDS = DAYS.toSeconds(30).toInt()
