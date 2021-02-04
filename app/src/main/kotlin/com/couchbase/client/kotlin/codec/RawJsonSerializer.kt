package com.couchbase.client.kotlin.codec

public class RawJsonSerializer : Transcoder {
    override fun <T> encode(input: T?, type: TypeRef<T>): Content {
        TODO("Not yet implemented")
    }

    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T? {
        TODO("Not yet implemented")
    }
}
