package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.CodecFlags

public class ContentType(public val flags: Int) {

    override fun toString(): String = "${javaClass.simpleName}(flags=${flags.toHex()})"

    public companion object {
        public fun ofFlags(flags: Int): ContentType = ContentType(flags)

        public val JSON: ContentType = ofFlags(CodecFlags.JSON_COMPAT_FLAGS)
        public val BINARY: ContentType = ofFlags(CodecFlags.BINARY_COMPAT_FLAGS)
        public val STRING: ContentType = ofFlags(CodecFlags.STRING_COMPAT_FLAGS)
        public val SERIALIZED_JAVA: ContentType = ofFlags(CodecFlags.SERIALIZED_COMPAT_FLAGS)
    }
}

private fun Int.toHex(bytes: Int = 4): String = "0x%0${bytes * 2}x".format(this)
private fun Long.toHex(bytes: Int = 8): String = "0x%0${bytes * 2}x".format(this)


