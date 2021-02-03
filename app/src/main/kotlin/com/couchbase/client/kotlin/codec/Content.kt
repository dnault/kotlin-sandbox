package com.couchbase.client.kotlin.codec

import com.couchbase.client.core.msg.kv.CodecFlags

public class Content(public val bytes: ByteArray, public val flags: Int) {
    public companion object {
        public fun json(value: ByteArray): Content = Content(value, CodecFlags.JSON_COMPAT_FLAGS)
        public fun json(value: String): Content = json(value.toByteArray())

        public fun binary(value: ByteArray): Content = Content(value, CodecFlags.BINARY_COMPAT_FLAGS)

        public fun string(value: String): Content = Content(value.toByteArray(), CodecFlags.STRING_COMPAT_FLAGS)
        public fun serializedJavaObject(value: ByteArray): Content = Content(value, CodecFlags.SERIALIZED_COMPAT_FLAGS)
    }
}
