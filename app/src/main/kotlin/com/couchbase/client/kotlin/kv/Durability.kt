package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.DurabilityLevel
import com.couchbase.client.core.msg.kv.DurabilityLevel.*
import com.couchbase.client.core.service.kv.Observe.ObservePersistTo
import com.couchbase.client.core.service.kv.Observe.ObserveReplicateTo

public sealed class Durability {

    public object InMemoryOnActive : Durability() {
        override fun toString(): String = "InMemoryOnActive"
    }

    public class Synchronous internal constructor(
        public val level: DurabilityLevel,
    ) : Durability() {
        override fun toString(): String = "Synchronous($level)"
    }

    public class Polling internal constructor(
        public val replicateTo: ReplicateTo,
        public val persistTo: PersistTo,
    ) : Durability() {
        override fun toString(): String = "Polling(replicateTo=$replicateTo, persistTo=$persistTo)"
    }

    public companion object {
        /**
         * No special durability requirements.
         *
         * The mutation is considered a success once the mutation is saved
         * in RAM of the node hosting the active partition (vBucket) for the data.
         */
        public fun inMemoryOnActive(): Durability = InMemoryOnActive

        /**
         * The client will poll the server until the specified durability
         * requirements are observed.
         *
         * This strategy is supported by all Couchbase Server versions.
         * When using Couchbase Server 6.5 or later, prefer the other
         * durability options.
         *
         * Note: polling([ReplicateTo.NONE], [PersistTo.NONE])
         * behaves just like [inMemoryOnActive].
         */
        public fun polling(replicateTo: ReplicateTo, persistTo: PersistTo = PersistTo.NONE): Durability =
            Polling(replicateTo, persistTo)

        /**
         * The mutation must be replicated to (that is, held in the memory
         * allocated to the bucket on) a majority of the Data Service nodes.
         *
         * Requires Couchbase Server 6.5 or later.
         */
        public fun majority(): Durability = Synchronous(MAJORITY)

        /**
         * The mutation must be persisted to a majority of the Data Service nodes.
         *
         * Requires Couchbase Server 6.5 or later.
         */
        public fun persistToMajority(): Durability = Synchronous(PERSIST_TO_MAJORITY)

        /**
         * The mutation must be replicated to a majority of the Data Service nodes.
         *
         * Additionally, it must be persisted (that is, written and synchronised to disk)
         * on the node hosting the active partition (vBucket) for the data.
         *
         * Requires Couchbase Server 6.5 or later.
         */
        public fun majorityAndPersistToActive(): Durability = Synchronous(MAJORITY_AND_PERSIST_TO_ACTIVE)
    }
}

public enum class ReplicateTo(internal val coreHandle: ObserveReplicateTo) {
    NONE(ObserveReplicateTo.NONE),
    ONE(ObserveReplicateTo.ONE),
    TWO(ObserveReplicateTo.TWO),
    THREE(ObserveReplicateTo.THREE);
}

public enum class PersistTo(internal val coreHandle: ObservePersistTo) {
    NONE(ObservePersistTo.NONE),
    ACTIVE(ObservePersistTo.ACTIVE),
    ONE(ObservePersistTo.ONE),
    TWO(ObservePersistTo.TWO),
    THREE(ObservePersistTo.THREE),
    FOUR(ObservePersistTo.FOUR);
}
