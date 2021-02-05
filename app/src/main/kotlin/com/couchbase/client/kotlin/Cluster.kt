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
import com.couchbase.client.core.diagnostics.ClusterState
import com.couchbase.client.core.diagnostics.WaitUntilReadyHelper
import com.couchbase.client.core.env.Authenticator
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.core.env.PasswordAuthenticator
import com.couchbase.client.core.env.SeedNode
import com.couchbase.client.core.msg.kv.MutationToken
import com.couchbase.client.core.msg.query.QueryRequest
import com.couchbase.client.core.service.ServiceType
import com.couchbase.client.core.util.ConnectionStringUtil
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.query.QueryProfile
import com.couchbase.client.kotlin.query.QueryResult
import kotlinx.coroutines.future.await
import java.time.Duration
import java.util.*

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

    private fun RequestOptions.actualQueryTimeout() =
        timeout ?: core.context().environment().timeoutConfig().queryTimeout()

    private fun RequestOptions.actualRetryStrategy() = retryStrategy ?: core.context().environment().retryStrategy()

    public suspend fun waitUntilReady(
        timeout: Duration,
        serviceTypes: Set<ServiceType> = emptySet(),
        desiredState: ClusterState = ClusterState.ONLINE,
    ): Cluster {
        WaitUntilReadyHelper.waitUntilReady(core, serviceTypes, timeout, desiredState, Optional.empty()).await()
        return this
    }

    public fun bucket(name: String): Bucket {
        core.openBucket(name)
        return Bucket(name, core)
    }


    public data class QueryTuning(
        val maxParallelism: Int? = null,
        val scanCap:Int? = null,
        val pipelineBatch: Int? = null,
        val pipelineCap: Int? = null,
    )

    open public class QueryScanConsistency {
        public companion object {
          //  public fun consistentWith(Collection<MutationToken> mutations)
        }
        public object NotBounded : QueryScanConsistency()
        public object RequestPlus : QueryScanConsistency()
    }

    public data class QueryDiagnostics(
        val clientContextId: String? = UUID.randomUUID().toString(),
        val metrics: Boolean = false,
        val profile: QueryProfile = QueryProfile.OFF,
    )


    public suspend fun query(
        statement: String,
        options: RequestOptions = RequestOptions.DEFAULT,
        namedParameters: Map<String, Any?> = emptyMap(),
        positionalParameters: List<Any?> = emptyList(),
        readonly: Boolean = false,
        adhoc: Boolean = true,
        flexIndex: Boolean = false,

        serializer: JsonSerializer? = null,
        raw: Map<String, Any?> = emptyMap(),

        consistency: QueryScanConsistency = QueryScanConsistency.NotBounded,
        diagnostics: QueryDiagnostics? = null,
        tuning: QueryTuning? = null,



        // diagnostics
        clientContextId: String? = UUID.randomUUID().toString(),
        metrics: Boolean = false,
        profile: QueryProfile = QueryProfile.OFF,

        // tuning
        maxParallelism: Int? = null,
        scanCap: Int? = null,
        pipelineBatch: Int? = null,
        pipelineCap: Int? = null,

        // consistency
        scanWait: String? = null,
        scanConsistency: QueryScanConsistency = QueryScanConsistency.NotBounded,
        consistentWith: List<MutationToken>? = null,


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
