package com.couchbase.client.kotlin.query

import com.couchbase.client.core.msg.kv.MutationToken
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

public enum class QueryScanConsistency {
    /**
     * The indexer will return whatever state it has to the query engine at the time of query.
     *
     * This is the default (for single-statement requests). No timestamp vector is used in the index scan. This is also
     * the fastest mode, because we avoid the cost of obtaining the vector, and we also avoid any wait for the index to
     * catch up to the vector.
     */
    NOT_BOUNDED {
        override fun toString(): String = "not_bounded"
    },

    /**
     * The indexer will wait until all mutations have been processed at the time of request before returning to the
     * query engine.
     *
     * This implements strong consistency per request. Before processing the request, a current vector is obtained. The
     * vector is used as a lower bound for the statements in the request. If there are DML statements in the request,
     * RYOW ("read your own write") is also applied within the request.
     */
    REQUEST_PLUS {
        override fun toString(): String = "request_plus"
    }
}
