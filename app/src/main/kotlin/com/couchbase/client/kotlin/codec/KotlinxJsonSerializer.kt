package com.couchbase.client.kotlin.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.StandardCharsets.UTF_8

@ExperimentalSerializationApi
public class KotlinxJsonSerializer : JsonSerializer {
    override fun <T> serialize(value: T, typeRef: TypeRef<T>): ByteArray {
        if (value == null) return "null".toByteArray()
        val serializer = Json.serializersModule.serializer(typeRef.type)
        return Json.encodeToString(serializer, value).toByteArray()
    }

    override fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T {
        val s = json.toString(UTF_8)
        if (s == "null") return null as T
        val serializer = Json.serializersModule.serializer(typeRef.type)
        @Suppress("UNCHECKED_CAST")
        val result = Json.decodeFromString(serializer, s) as T

        if (!typeRef.nullable && result == null) {
            throw NullPointerException("Can't deserialize null value into non-nullable type $typeRef")
        }

        return result
    }
}
