package com.couchbase.client.kotlin.query

import com.couchbase.client.core.msg.query.QueryResponse
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.TypeRef
import com.couchbase.client.kotlin.codec.typeRef
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle

public class QueryResult(
    private val response: QueryResponse,
    private val defaultSerializer: JsonSerializer,
) {

    public fun unifiedFlow() : Flow<QueryFlowItem> {

        return flowOf<Flow<QueryFlowItem>>(
            rowsAsBytes().map { QueryRow(it, defaultSerializer) },
            response.trailer()
                .map { QueryMeta(response.header(), it) }.asFlow()
        ).flattenConcat()

//        return rowsAsBytes().map { QueryRow(it, defaultSerializer) }
//            .onCompletion {
//                val meta : QueryMeta = response.trailer()
//                    .map { QueryMeta(response.header(), it) }.
////                emit(response.trailer()
////                .map { QueryMeta(response.header(), it) }
////                .asFlow()) }
//            }

    }


    public fun rowsAsBytes(): Flow<ByteArray> {
        return response.rows().asFlow().map { it.data() }
    }

    public inline fun <reified T> rowsAs(serializer: JsonSerializer? = null): Flow<T?> {
        return rowsAs(typeRef<T>(), serializer)
    }

    public fun <T> rowsAs(type: TypeRef<T>, serializer: JsonSerializer? = null): Flow<T?> {
        val actualSerializer = serializer ?: defaultSerializer
        return response.rows().asFlow().map {
            actualSerializer.deserialize(it.data(), type)
        }
    }

    public suspend fun metaData(): QueryMeta {
        return response.trailer()
            .map { QueryMeta(response.header(), it) }
            .awaitSingle()
    }

}
