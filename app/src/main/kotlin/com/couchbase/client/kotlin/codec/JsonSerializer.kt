package com.couchbase.client.kotlin.codec

public interface JsonSerializer {
    public fun serialize(value: Any?): ByteArray

    public fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T?
}
