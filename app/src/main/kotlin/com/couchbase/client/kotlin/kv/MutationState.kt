package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.MutationToken
import com.couchbase.client.core.msg.kv.MutationTokenAggregator

public class MutationState private constructor(private val tokens: MutationTokenAggregator) : Iterable<MutationToken> {
    public constructor() : this(MutationTokenAggregator())

    public constructor(tokens: Iterable<MutationToken>) : this() {
        tokens.forEach { add(it) }
    }

    public fun add(token: MutationToken): Unit = tokens.add(token)

    public fun export(): Map<String, Any?> = tokens.export()

    internal fun exportForSearch(): Map<String, Any?> = tokens.exportForSearch()

    override fun toString(): String = tokens.toString()

    public companion object {
        public fun from(exported: Map<String, Any?>): MutationState =
            MutationState(MutationTokenAggregator.from(exported))
    }

    override fun iterator(): Iterator<MutationToken> = tokens.iterator()
}

