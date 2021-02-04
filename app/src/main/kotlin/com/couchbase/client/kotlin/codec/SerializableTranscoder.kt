package com.couchbase.client.kotlin.codec

import com.couchbase.client.core.error.InvalidArgumentException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

public object SerializableTranscoder : Transcoder {
    override fun <T> doEncode(input: T?, type: TypeRef<T>): Content {
        return try {
            val bos = ByteArrayOutputStream()
            ObjectOutputStream(bos).use {
                it.writeObject(input)
                Content.serializedJavaObject(bos.toByteArray())
            }
        } catch (ex: Exception) {
            throw InvalidArgumentException.fromMessage("Could not encode (serialize) the given object!", ex)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(input: ByteArray, type: TypeRef<T>, flags: Int): T? {
        return try {
            ObjectInputStream(ByteArrayInputStream(input)).use { it.readObject() as T }
        } catch (ex: java.lang.Exception) {
            throw InvalidArgumentException.fromMessage("Could not decode (deserialize) the given object!", ex)
        }
    }
}
