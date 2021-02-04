package com.couchbase.client.kotlin.codec

import com.couchbase.client.core.error.DecodingFailureException
import com.couchbase.client.core.error.InvalidArgumentException

public object RawBinaryTranscoder : Transcoder {
    override fun <T> doEncode(input: T?, type: TypeRef<T>): Content {
        return when (input) {
            is ByteArray -> Content.binary(input)
            else -> throw InvalidArgumentException.fromMessage(
                "Only ByteArray is supported for the RawBinaryTranscoder!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T? {
        return when (type.type) {
            ByteArray::class.java -> input as T?
            else -> throw DecodingFailureException(
                "RawBinaryTranscoder can only decode into ByteArray!")
        }
    }
}
