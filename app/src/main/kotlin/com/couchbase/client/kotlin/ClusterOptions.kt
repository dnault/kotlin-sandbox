package com.couchbase.client.kotlin

import com.couchbase.client.core.env.Authenticator

data class ClusterOptions(val authenticator: Authenticator) {
}