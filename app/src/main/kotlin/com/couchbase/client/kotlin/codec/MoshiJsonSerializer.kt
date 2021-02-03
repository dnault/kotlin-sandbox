package com.couchbase.client.kotlin.codec

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import java.nio.charset.StandardCharsets

public class MoshiSerializer(private val mapper: Moshi) : JsonSerializer {
    override fun serialize(value: Any?): ByteArray {
        if (value == null) {
            return "null".toByteArray(StandardCharsets.UTF_8)
        }

        // So Moshi doesn't complain about not being able to serialize
        // singletonList and friends.
        val normalizedClass = preferCollectionInterface(value.javaClass)

        val buffer = Buffer()
        mapper.adapter<Any>(normalizedClass).toJson(buffer, value)
        return buffer.readByteArray()
    }

    private fun preferCollectionInterface(clazz: Class<*>): Class<*> {
        return when {
            List::class.java.isAssignableFrom(clazz) -> List::class.java
            Map::class.java.isAssignableFrom(clazz) -> Map::class.java
            Set::class.java.isAssignableFrom(clazz) -> Set::class.java
            else -> clazz
        }
    }

    override fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T? {
        val adapter: JsonAdapter<T> = mapper.adapter(typeRef.type)
        val reader = JsonReader.of(Buffer().write(json))
        return adapter.fromJson(reader)
    }
}
