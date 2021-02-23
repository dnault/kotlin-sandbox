package com.couchbase.client.kotlin.codec

import com.couchbase.client.core.error.DecodingFailureException
import com.couchbase.client.core.error.InvalidArgumentException
import kt.sandbox.toStringUtf8

public object RawStringTranscoder : Transcoder {
    override fun <T> doEncode(input: T, type: TypeRef<T>): Content {
        return when (input) {
            is String -> Content.string(input)
            else -> throw InvalidArgumentException.fromMessage(
                "Only String is supported for the RawStringTranscoder!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T {
        return when (type.type) {
            String::class.java -> input.toStringUtf8() as T
            else -> throw DecodingFailureException(
                "RawStringTranscoder can only decode into String!")
        }
    }
}
