package com.couchbase.client.kotlin.codec

import com.couchbase.client.core.error.DecodingFailureException
import com.couchbase.client.core.error.InvalidArgumentException
import kt.sandbox.toStringUtf8

public object RawJsonTranscoder : Transcoder {
    override fun <T> doEncode(input: T?, type: TypeRef<T>): Content {
        return when (input) {
            is ByteArray -> Content.json(input)
            is String -> Content.json(input)
            else -> throw InvalidArgumentException.fromMessage(
                "Only ByteArray and String types are supported for the RawJsonTranscoder!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T? {
        return when (type.type) {
            ByteArray::class.java -> input as T?
            String::class.java -> input.toStringUtf8() as T?
            else -> throw DecodingFailureException(
                "RawJsonTranscoder can only decode into either ByteArray or String!")
        }
    }
}
