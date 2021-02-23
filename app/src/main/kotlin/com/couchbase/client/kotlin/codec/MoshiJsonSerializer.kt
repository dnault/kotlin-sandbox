package com.couchbase.client.kotlin.codec

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer

public class MoshiJsonSerializer(private val mapper: Moshi) : JsonSerializer {
    override fun <T> serialize(value: T, typeRef: TypeRef<T>): ByteArray {
        if (value == null) return "null".toByteArray()

        val buffer = Buffer()
        mapper.adapter<T>(typeRef.type).toJson(buffer, value)
        return buffer.readByteArray()
    }

    override fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T {
        val adapter: JsonAdapter<T> = mapper.adapter(typeRef.type)
        val reader = JsonReader.of(Buffer().write(json))
        val result = adapter.fromJson(reader) as T

        if (!typeRef.nullable && result == null) {
            throw NullPointerException("Can't deserialize null value into non-nullable type $typeRef")
        }

        return result
    }
}
