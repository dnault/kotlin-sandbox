package com.couchbase.client.kotlin.query

import com.couchbase.client.core.annotation.Stability


/**
 * Query profiling information received from the server query engine.
 *
 * @since 3.0.0
 */
@Stability.Volatile
public enum class QueryProfile {

    /**
     * No profiling information is added to the query response.
     */
    OFF {
        override fun toString(): String = "off"
    },

    /**
     * The query response includes a profile section with stats and details about various phases of the query plan and
     * execution.
     *
     *
     * Three phase times will be included in the system:active_requests and system:completed_requests monitoring
     * keyspaces.
     */
    PHASES {
        override fun toString(): String = "phases"

    },

    /**
     * Besides the phase times, the profile section of the query response document will include a full query plan with
     * timing and information about the number of processed documents at each phase.
     *
     *
     * This information will be included in the system:active_requests and system:completed_requests keyspaces.
     */
    TIMINGS {
        override fun toString(): String = "timings"
    }
}
