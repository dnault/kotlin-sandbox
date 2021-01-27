package com.couchbase.client.kotlin.query

import com.couchbase.client.kotlin.CommonOptions

data class QueryOptions(override val timeoutMillis: Long? = null, val readonly: Boolean = false) : CommonOptions