package com.couchbase.client.kotlin.kv

import com.couchbase.client.kotlin.CommonOptions
import java.time.Duration

data class GetOptions(override val timeoutMillis: Long? = null) : CommonOptions