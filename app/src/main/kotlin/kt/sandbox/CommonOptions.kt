package kt.sandbox

import com.couchbase.client.core.cnc.RequestSpan
import com.couchbase.client.core.retry.RetryStrategy
import java.time.Duration

class CommonOptions private constructor(
    val timeout: Duration?,
    val retryStrategy: RetryStrategy?,
    val clientContext: Map<String, Any>?,
    val parentSpan: RequestSpan?,
) {
    companion object {
        val DEFAULT = CommonOptions {}
    }

    fun copy(initializer: Builder.() -> Unit): CommonOptions {
        return Builder(this).apply(initializer).build()
    }

    override fun toString(): String {
        return "CommonOptions(timeout=$timeout, retryStrategy=$retryStrategy, clientContext=$clientContext, parentSpan=$parentSpan)"
    }

    class Builder() {
        var timeout: Duration? = null
        var retryStrategy: RetryStrategy? = null
        var clientContext: Map<String, Any>? = null
        var parentSpan: RequestSpan? = null

        internal constructor(opt: CommonOptions) : this() {
            this.timeout = opt.timeout
            this.retryStrategy = opt.retryStrategy
            this.clientContext = opt.clientContext
            this.parentSpan = opt.parentSpan
        }

        internal fun build(): CommonOptions = CommonOptions(timeout, retryStrategy, clientContext, parentSpan)
    }
}


fun CommonOptions(initializer: CommonOptions.Builder.() -> Unit): CommonOptions {
    return CommonOptions.Builder().apply(initializer).build()
}
