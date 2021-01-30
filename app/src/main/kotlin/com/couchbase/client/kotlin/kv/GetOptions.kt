package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import com.couchbase.client.kotlin.CommonOptions
import java.time.Duration

data class GetOptions(
    val withExpiry: Boolean = false,
    val projections: List<String>? = null,

    override val timeout: Duration? = null,
    override val parentSpan: RequestSpan? = null,
    override val retryStrategy: RetryStrategy? = null,
    override val clientContext: Map<String, Any>? = null,
) : CommonOptions {

    companion object {
        val DEFAULT = GetOptions()
    }
}


