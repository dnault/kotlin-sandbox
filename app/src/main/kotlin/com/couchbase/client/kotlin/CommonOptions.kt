package com.couchbase.client.kotlin

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import java.time.Duration

interface CommonOptions {
    val parentSpan: RequestSpan?
    val timeout: Duration?
    val retryStrategy: RetryStrategy?
    val clientContext: Map<String, Any>?
}
