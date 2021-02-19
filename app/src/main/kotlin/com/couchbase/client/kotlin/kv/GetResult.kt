package com.couchbase.client.kotlin.kv

import com.couchbase.client.kotlin.codec.Transcoder
import com.couchbase.client.kotlin.codec.typeRef
import java.time.Instant

public class GetResult private constructor(
    public val id: String,
    public val cas: Long,
    public val flags: Int,
    public val content: ByteArray,
    public val isExpiryKnown: Boolean,
    public val defaultTranscoder: Transcoder,
    expiry: Instant?,
) {
    internal companion object {
        fun withKnownExpiry(
            id: String,
            cas: Long,
            flags: Int,
            content: ByteArray,
            defaultTranscoder: Transcoder,
            expiry: Instant?,
        ) =
            GetResult(id, cas, flags, content, true, defaultTranscoder, expiry)

        fun withUnknownExpiry(id: String, cas: Long, flags: Int, content: ByteArray, defaultTranscoder: Transcoder) =
            GetResult(id, cas, flags, content, false, defaultTranscoder, null)
    }
//
//    public val e : Optional<Expiry>
//        get() {
//            if (!isExpiryKnown) {
//                return Optional.empty()
//            }
//
//            return if (expiry == null) Expiry.none().toOptional()
//            else Expiry.absolute(expiry!!).toOptional()
//        }


    public val expiry: Instant? = expiry
        get() = if (!isExpiryKnown) throw IllegalStateException(
            "Expiry is not available because `get` was called without `withExpiry=true`.")
        else field

    public inline fun <reified T> contentAs(transcoder: Transcoder = defaultTranscoder): T? {
        return transcoder.decode(content, typeRef(), flags)
    }

}
