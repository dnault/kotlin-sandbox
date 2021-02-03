package com.couchbase.client.kotlin.codec

public interface JsonSerializer {

    public fun <T> serialize(value: T?, typeRef: TypeRef<T>): ByteArray

    public fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T?

}

public inline fun <reified T> JsonSerializer.serialize(value: T?) : ByteArray {
    return serialize(value, typeRef())
}
