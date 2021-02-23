package com.couchbase.client.kotlin.codec

public interface Transcoder {

    public fun <T> encode(input: T, type: TypeRef<T>): Content {
        return if (input is Content) input // already encoded!
        else doEncode(input, type)
    }

    /**
     * Encodes the given input into the wire representation based on the data format.
     *
     * @param input the input object to encode.
     * @return the encoded wire representation of the payload.
     */
    public fun <T> doEncode(input: T, type: TypeRef<T>): Content

    /**
     * Decodes the wire representation into the entity based on the data format.
     *
     * @param target the target type to decode.
     * @param input the wire representation to decode.
     * @param flags the flags on the wire
     * @return the decoded entity.
     */
    public fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T
}

