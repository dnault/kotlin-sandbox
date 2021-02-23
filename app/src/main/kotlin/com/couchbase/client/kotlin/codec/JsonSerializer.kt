package com.couchbase.client.kotlin.codec

public interface JsonSerializer {

    public fun <T> serialize(value: T, typeRef: TypeRef<T>): ByteArray

    public fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T

}
