package com.couchbase.client.kotlin.codec

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper

public class JacksonJsonSerializer(private val mapper: ObjectMapper) : JsonSerializer {
    override fun <T> serialize(value: T?, typeRef: TypeRef<T>): ByteArray = mapper.writeValueAsBytes(value)

    override fun <T> deserialize(json: ByteArray, typeRef: TypeRef<T>): T? {
        val type: JavaType = mapper.typeFactory.constructType(typeRef.type)
        return mapper.readValue(json, type)
    }
}


