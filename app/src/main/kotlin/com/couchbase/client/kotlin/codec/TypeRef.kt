package com.couchbase.client.kotlin.codec

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * A reified type. Conveys generic type information at run time.
 *
 * Create new instances with the [typeRef] factory method:
 *
 * ```
 * val listOfStrings = typeRef<List<String>>()
 * ```
 *
 * Uses the technique described in Neal Gafter's article on
 * [Super Type Tokens](http://gafter.blogspot.com/2006/12/super-type-tokens.html).
 *
 * @param <T> The type to represent
 */
public abstract class TypeRef<T> protected constructor(
    public val nullable: Boolean,
) {
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

public inline fun <reified T> typeRef(): TypeRef<T> {
    return object : TypeRef<T>(nullable = null is T) {}
}
