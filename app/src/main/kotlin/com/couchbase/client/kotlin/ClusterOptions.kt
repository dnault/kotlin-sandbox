package com.couchbase.client.kotlin

import com.couchbase.client.core.env.Authenticator

public data class ClusterOptions(val authenticator: Authenticator) {
}
