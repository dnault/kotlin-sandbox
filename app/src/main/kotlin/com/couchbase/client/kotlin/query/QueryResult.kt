package com.couchbase.client.kotlin.query

import com.couchbase.client.core.msg.query.QueryResponse
import com.couchbase.client.kotlin.codec.JsonSerializer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import java.util.concurrent.atomic.AtomicBoolean

public class QueryResult(
    private val response: QueryResponse,
    private val defaultSerializer: JsonSerializer,
) {
    private var consumed = AtomicBoolean()

    private fun consume() {
        if (!consumed.compareAndSet(false, true))
            throw IllegalStateException("Query results may only be consumed once.")
    }

    private fun flow(): Flow<QueryFlowItem> {
        consume()

        return flow {
            emitAll(response.rows().asFlow()
                .map { QueryRow(it.data(), defaultSerializer) })
            emitAll(response.trailer().asFlow().map { QueryMetaData(response.header(), it) })
        }
            .onStart { }
    }
}
