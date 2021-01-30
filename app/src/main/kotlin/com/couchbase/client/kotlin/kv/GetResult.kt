package com.couchbase.client.kotlin.kv

import java.util.*

public data class GetResult(val cas: Long, val flags: Int, val expiry: Optional<Long>, val content: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetResult

        if (cas != other.cas) return false
        if (flags != other.flags) return false
        if (expiry != other.expiry) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cas.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + expiry.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
