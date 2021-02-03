package com.couchbase.client.kotlin.codec

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * A reified type. Conveys generic type information at run time.
 *
 * Create an anonymous subclass parameterized with the type you want to represent.
 * For example:
 *
 * ```
 * TypeRef<List<String>> listOfStrings = new TypeRef<List<String>>(){};
 * ```
 *
 * Immutable.
 *
 * Uses the technique described in Neal Gafter's article on
 * [Super Type Tokens](http://gafter.blogspot.com/2006/12/super-type-tokens.html).
 *
 * @param <T> The type to represent
 */
public abstract class TypeRef<T> protected constructor() {
    public val type: Type

    override fun toString(): String = type.typeName

    init {
        val superclass = javaClass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter.")
        }
        type = (superclass as ParameterizedType).actualTypeArguments[0]
    }
}

public inline fun <reified T : Any> typeRef(): TypeRef<T> {
    return object : TypeRef<T>() {}
}

public inline fun <reified T : Any> reify(): Type {
    return object : TypeRef<T>() {}.type
}
