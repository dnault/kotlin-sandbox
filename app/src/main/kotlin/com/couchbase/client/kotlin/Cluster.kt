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
import com.couchbase.client.kotlin.query.QueryResult
import kotlinx.coroutines.future.await

public class Cluster internal constructor(
    environment: CoreEnvironment,
    authenticator: Authenticator,
    seedNodes: Set<SeedNode>,
) {

    private val core: Core = Core.create(environment, authenticator, seedNodes)

    init {
        core.initGlobalConfig()
    }

    public companion object {

        public fun connect(connectionString: String, username: String, password: String): Cluster {
            return connect(connectionString, ClusterOptions(PasswordAuthenticator.create(username, password)))
        }

        public fun connect(connectionString: String, options: ClusterOptions): Cluster {
            val env = CoreEnvironment.create()
            val seedNodes = ConnectionStringUtil.seedNodesFromConnectionString(
                connectionString,
                env.ioConfig().dnsSrvEnabled(),
                env.securityConfig().tlsEnabled(),
                env.eventBus()
            )
            return Cluster(env, options.authenticator, seedNodes)
        }
    }

    private fun RequestOptions.actualQueryTimeout() = timeout ?: core.context().environment().timeoutConfig().queryTimeout()
    private fun RequestOptions.actualRetryStrategy() = retryStrategy ?: core.context().environment().retryStrategy()

    public fun bucket(name: String): Bucket {
        core.openBucket(name)
        return Bucket(name, core)
    }

    public suspend fun query(
        statement: String,
        readonly: Boolean = false,
        options: RequestOptions = RequestOptions.DEFAULT
    ): QueryResult {
        TODO("what about these?")
        val query = null;
        val contextId = null;
        val queryContext = null;

        val request = QueryRequest(
            options.actualQueryTimeout(),
            core.context(),
            options.actualRetryStrategy(),
            null,
            statement,
            query,
            readonly,
            contextId,
            options.parentSpan,
            queryContext
        )
        core.send(request)
        val response = request.response().await()

        return QueryResult()
    }

}
