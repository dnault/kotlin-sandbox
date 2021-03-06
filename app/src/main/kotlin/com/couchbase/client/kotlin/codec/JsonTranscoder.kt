package com.couchbase.client.kotlin.codec

/**
 * Converts the value to and from JSON by delegating to a [JsonSerializer]
 */
public class JsonTranscoder(private val serializer: JsonSerializer) : Transcoder {
    override fun <T> doEncode(input: T, type: TypeRef<T>): Content {
        return Content.json(serializer.serialize(input, type))
    }

    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T {
        return serializer.deserialize(input, type)
    }
}
