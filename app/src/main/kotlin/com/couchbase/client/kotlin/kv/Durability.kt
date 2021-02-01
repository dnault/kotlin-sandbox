package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.DurabilityLevel
import com.couchbase.client.core.msg.kv.DurabilityLevel.*
import com.couchbase.client.core.service.kv.Observe.ObservePersistTo
import com.couchbase.client.core.service.kv.Observe.ObserveReplicateTo

public sealed class Durability {
    /**
     * Synchronous durability, also known as enhanced durability.
     */
    public class Synchronous internal constructor(
        public val level: DurabilityLevel,
    ) : Durability() {
        override fun toString(): String = "Synchronous($level)"
    }

    /**
     * Old-style durability implemented with polling.
     */
    public class Polling internal constructor(
        public val persistTo: PersistTo,
        public val replicateTo: ReplicateTo,
    ) : Durability() {
        override fun toString(): String = "Polling(persistTo=$persistTo, replicateTo=$replicateTo)"
    }

    public companion object {
        /**
         * The client will poll the server until the specified durability requirements have been observed.
         */
        public fun polling(persistTo: PersistTo, replicateTo: ReplicateTo): Durability =
            Polling(persistTo, replicateTo)

        /**
         * The mutation must be replicated to (that is, held in the memory
         * allocated to the bucket on) a majority of the Data Service nodes.
         */
        public fun majority(): Synchronous = Synchronous(MAJORITY)

        /**
         * The mutation must be persisted to a majority of the Data Service nodes.
         *
         * Accordingly, it will be written to disk on those nodes.
         */
        public fun persistToMajority(): Synchronous = Synchronous(PERSIST_TO_MAJORITY)

        /**
         * The mutation must be replicated to a majority of the Data Service nodes.
         *
         * Additionally, it must be persisted (that is, written and synchronised to disk)
         * on the node hosting the active partition (vBucket) for the data.
         */
        public fun majorityAndPersistToActive(): Synchronous = Synchronous(MAJORITY_AND_PERSIST_TO_ACTIVE)
    }
}

public enum class PersistTo(internal val coreHandle: ObservePersistTo) {
    NONE(ObservePersistTo.NONE),
    ACTIVE(ObservePersistTo.ACTIVE),
    ONE(ObservePersistTo.ONE),
    TWO(ObservePersistTo.TWO),
    THREE(ObservePersistTo.THREE),
    FOUR(ObservePersistTo.FOUR);
}

public enum class ReplicateTo(internal val coreHandle: ObserveReplicateTo) {
    NONE(ObserveReplicateTo.NONE),
    ONE(ObserveReplicateTo.ONE),
    TWO(ObserveReplicateTo.TWO),
    THREE(ObserveReplicateTo.THREE);
}
