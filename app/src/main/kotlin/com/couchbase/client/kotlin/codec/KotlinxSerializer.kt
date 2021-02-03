package com.couchbase.client.kotlin.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kt.sandbox.Project
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets.UTF_8

@ExperimentalSerializationApi
public class KotlinxSerializer : JsonSerializer {
    override fun serialize(value: Any?): ByteArray {
        // todo make the value a generic, reify it, and pass it in?
        if (value == null) return "null".toByteArray()
        val serializer = Json.serializersModule.serializer(value.javaClass)
        return Json.encodeToString(serializer, value).toByteArray()
    }

    override fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T? {
        val s = json.toString(UTF_8)
        if (s == "null") return null
        val serializer = Json.serializersModule.serializer(typeRef.type)
        @Suppress("UNCHECKED_CAST")
        return Json.decodeFromString(serializer, s) as T
    }
}


public fun main() {
    println(KotlinxSerializer().serialize(Project("doo", "sas", listOf("x"))).toStringUtf8())
}

internal fun ByteArray.toStringUtf8() = toString(UTF_8)
