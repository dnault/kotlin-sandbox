package com.couchbase.client.kotlin.kv

import com.couchbase.client.kotlin.codec.Transcoder
import com.couchbase.client.kotlin.codec.typeRef

public class GetResult private constructor(
    public val id: String,
    public val cas: Long,
    public val flags: Int,
    public val content: ByteArray,
    public val defaultTranscoder: Transcoder,

    /**
     * A null value means the expiry is unknown because the
     * `withExpiry` argument was `false` when getting the document.
     *
     * If the expiry is known, it will be either [Expiry.None]
     * or [Expiry.Absolute].
     */
    public val expiry: Expiry?,
) {
    internal companion object {
        fun withKnownExpiry(
            id: String,
            cas: Long,
            flags: Int,
            content: ByteArray,
            defaultTranscoder: Transcoder,
            expiry: Expiry,
        ) =
            GetResult(id, cas, flags, content, defaultTranscoder, expiry)

        fun withUnknownExpiry(id: String, cas: Long, flags: Int, content: ByteArray, defaultTranscoder: Transcoder) =
            GetResult(id, cas, flags, content, defaultTranscoder, null)
    }

    public inline fun <reified T> contentAs(transcoder: Transcoder = defaultTranscoder): T {
        return transcoder.decode(content, typeRef(), flags)
    }

}
