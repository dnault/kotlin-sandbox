package com.couchbase.client.kotlin.kv

import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.typeRef
import java.time.Instant

public class GetResult private constructor(
    public val id: String,
    public val cas: Long,
    public val flags: Int,
    public val content: ByteArray,
    public val isExpiryKnown: Boolean,
    expiry: Instant?,
) {
    internal companion object {
        fun withKnownExpiry(id: String, cas: Long, flags: Int, content: ByteArray, expiry: Instant?) =
            GetResult(id, cas, flags, content, true, expiry)

        fun withUnknownExpiry(id: String, cas: Long, flags: Int, content: ByteArray) =
            GetResult(id, cas, flags, content, false, null)
    }

    public val expiry: Instant? = expiry
        get() = if (!isExpiryKnown) throw IllegalStateException(
            "Expiry is not available because `get` was called without `withExpiry=true`.")
        else field

    public inline fun <reified T> contentAs(serializer: JsonSerializer): T? {
        return serializer.deserialize(content, typeRef())
    }

}
