package com.couchbase.client.kotlin.query

import java.util.*

public enum class QueryStatus {
    RUNNING,
    SUCCESS,
    ERRORS,
    COMPLETED,
    STOPPED,
    TIMEOUT,
    CLOSED,
    FATAL,
    ABORTED,
    UNKNOWN,
    ;

    public companion object {
        public fun from(wireName: String): QueryStatus {
            return try {
                valueOf(wireName.toUpperCase(Locale.ROOT))
            } catch (ex: Exception) {
                UNKNOWN
            }
        }
    }
}
