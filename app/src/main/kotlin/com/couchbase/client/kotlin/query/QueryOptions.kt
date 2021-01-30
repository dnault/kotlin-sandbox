package com.couchbase.client.kotlin.query

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import com.couchbase.client.kotlin.CommonOptions
import java.time.Duration

data class QueryOptions(
    override val timeout: Duration? = null,
    override val parentSpan: RequestSpan? = null,
    override val retryStrategy: RetryStrategy? = null,
    override val clientContext: Map<String, Any>? = null,

    val readonly: Boolean = false
) : CommonOptions
