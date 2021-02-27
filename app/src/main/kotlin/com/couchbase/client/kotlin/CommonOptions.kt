package com.couchbase.client.kotlin

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import java.time.Duration

/**
 * Request options that apply most operations.
 */
public class CommonOptions(
    public val timeout: Duration? = null,
    public val parentSpan: RequestSpan? = null,
    public val retryStrategy: RetryStrategy? = null,
    public val clientContext: Map<String, Any?>? = null,
) {
    public companion object {
        public val Default: CommonOptions = CommonOptions()
    }
}
