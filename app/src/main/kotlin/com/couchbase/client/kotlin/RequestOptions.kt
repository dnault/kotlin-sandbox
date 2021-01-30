package com.couchbase.client.kotlin

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import java.time.Duration


public data class RequestOptions(
    val timeout: Duration? = null,
    val parentSpan: RequestSpan? = null,
    val retryStrategy: RetryStrategy? = null,
    val clientContext: Map<String, Any>? = null,
) {
    public companion object {
        public val DEFAULT: RequestOptions = RequestOptions()
    }
}
