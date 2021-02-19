package com.couchbase.client.kotlin.query

import com.couchbase.client.core.util.Golang
import java.time.Duration

public class QueryMetrics(
    public val map: Map<String, Any?>,
) {
    public val elapsedTime: Duration
        get() = getDuration("elapsedTime")

    public val executionTime: Duration
        get() = getDuration("executionTime")

    public val sortCount: Long
        get() = getLong("sortCount")

    public val resultCount: Long
        get() = getLong("resultCount")

    public val resultSize: Long
        get() = getLong("resultSize")

    public val mutationCount: Long
        get() = getLong("mutationCount")

    public val errorCount: Long
        get() = getLong("errorCount")

    public val warningCount: Long
        get() = getLong("warningCount")

    private fun getDuration(key: String): Duration = Golang.parseDuration(map[key] as String? ?: "0")

    private fun getLong(key: String): Long = (map[key] as Number? ?: 0).toLong()

    override fun toString(): String {
        return "QueryMetrics(map=$map)"
    }
}
