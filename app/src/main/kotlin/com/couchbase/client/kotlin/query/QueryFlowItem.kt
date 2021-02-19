package com.couchbase.client.kotlin.query

import com.couchbase.client.core.deps.com.fasterxml.jackson.core.type.TypeReference
import com.couchbase.client.core.error.ErrorCodeAndMessage
import com.couchbase.client.core.json.Mapper
import com.couchbase.client.core.msg.query.QueryChunkHeader
import com.couchbase.client.core.msg.query.QueryChunkTrailer
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.typeRef
import java.util.*

public sealed class QueryFlowItem

public class QueryRow(
    public val content: ByteArray,
    public val defaultSerializer: JsonSerializer,
) : QueryFlowItem() {

    public inline fun <reified T> contentAs(serializer: JsonSerializer = defaultSerializer): T? {
        return serializer.deserialize(content, typeRef())
    }
}

public class QueryError(
    public val cause: Throwable,
) : QueryFlowItem() {

    override fun toString(): String {
        return "QueryError(cause=$cause)"
    }
}

public class QueryMeta(
    private val header: QueryChunkHeader,
    private val trailer: QueryChunkTrailer,
) : QueryFlowItem() {

    public val requestId: String
        get() = header.requestId()

    public val clientContextId: String
        get() = header.clientContextId().orElse("")

    public val status: QueryStatus
        get() = QueryStatus.from(trailer.status())

    public val signature: Map<String, Any?>?
        get() = header.signature().parse().orElse(null)

    public val profile: Map<String, Any?>?
        get() = trailer.profile().parse().orElse(null)

    public val metrics: QueryMetrics?
        get() = trailer.metrics().parse().map { QueryMetrics(it) }.orElse(null)

    public val warnings: List<QueryWarning>
        get() = trailer.warnings().map { warnings ->
            ErrorCodeAndMessage.fromJsonArray(warnings)
                .map { QueryWarning(it.code(), it.message()) }
        }.orElse(emptyList())

    override fun toString(): String {
        return "QueryMetadata(header=$header, trailer=$trailer)"
    }
}

private val MAP_TYPE_REF = object : TypeReference<Map<String, Any?>>() {}

private fun Optional<ByteArray>.parse(): Optional<Map<String, Any?>> {
    return map { Mapper.decodeInto(it, MAP_TYPE_REF) }
}
