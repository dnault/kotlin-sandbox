/*
 * Copyright (c) 2020 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.kotlin

import com.couchbase.client.core.Core
import com.couchbase.client.core.env.Authenticator
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.core.env.PasswordAuthenticator
import com.couchbase.client.core.env.SeedNode
import com.couchbase.client.core.msg.query.QueryRequest
import com.couchbase.client.core.util.ConnectionStringUtil
import com.couchbase.client.kotlin.query.QueryOptions
import com.couchbase.client.kotlin.query.QueryResult
import kotlinx.coroutines.future.await

class AsyncCluster internal constructor(
    environment: CoreEnvironment,
    authenticator: Authenticator,
    seedNodes: Set<SeedNode>,
) {

    private val core: Core = Core.create(environment, authenticator, seedNodes)

    private val defaultQueryOptions = QueryOptions()

    init {
        core.initGlobalConfig()
    }

    companion object {

        fun connect(connectionString: String, username: String, password: String): AsyncCluster {
            return connect(connectionString, ClusterOptions(PasswordAuthenticator.create(username, password)))
        }

        fun connect(connectionString: String, options: ClusterOptions): AsyncCluster {
            val env = CoreEnvironment.create()
            val seedNodes = ConnectionStringUtil.seedNodesFromConnectionString(
                connectionString,
                env.ioConfig().dnsSrvEnabled(),
                env.securityConfig().tlsEnabled(),
                env.eventBus()
            )
            return AsyncCluster(env, options.authenticator, seedNodes)
        }
    }

    fun bucket(name: String): AsyncBucket {
        core.openBucket(name)
        return AsyncBucket(name, core)
    }

    suspend fun query(statement: String, options: QueryOptions = defaultQueryOptions): QueryResult {
        val timeout = options.timeout ?: core.context().environment().timeoutConfig().queryTimeout()
        val query = null;
        val idempotent = options.readonly;
        val contextId = null;
        val span = null;

        val request = QueryRequest(
            timeout, core.context(), core.context().environment().retryStrategy(),
            null, statement, query, idempotent, contextId, span, null
        )
        core.send(request)
        val response = request.response().await()

        return QueryResult()
    }

}
