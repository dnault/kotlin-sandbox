package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.MutationToken
import com.couchbase.client.core.msg.kv.MutationTokenAggregator

/**
 * Aggregation of one or more [MutationToken]s for specifying
 * the consistency requirements of N1QL or FTS queries.
 *
 * Thread-safe.
 */
public class MutationState private constructor(
    private val tokens: MutationTokenAggregator,
) : Iterable<MutationToken> {

    /**
     * Creates an empty mutation state.
     */
    public constructor() : this(MutationTokenAggregator())

    /**
     * Creates a mutation state representing the given tokens.
     */
    public constructor(tokens: Iterable<MutationToken>) : this() {
        tokens.forEach { add(it) }
    }

    /**
     * Adds the given token to this state.
     */
    public fun add(token: MutationToken): Unit = tokens.add(token)

    /**
     * Exports the this mutation state into a universal format,
     * which can be used either to serialize it into a N1QL query
     * or to send it over the network to a different application/SDK.
     */
    public fun export(): Map<String, Any?> = tokens.export()

    internal fun exportForSearch(): Map<String, Any?> = tokens.exportForSearch()

    override fun toString(): String = tokens.toString()

    public companion object {
        /**
         * Parses the serialized form returned by [export].
         */
        public fun from(exported: Map<String, Any?>): MutationState =
            MutationState(MutationTokenAggregator.from(exported))
    }

    override fun iterator(): Iterator<MutationToken> = tokens.iterator()
}

