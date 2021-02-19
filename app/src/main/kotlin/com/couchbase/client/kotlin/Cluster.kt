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
import com.couchbase.client.core.cnc.TracingIdentifiers
import com.couchbase.client.core.diagnostics.ClusterState
import com.couchbase.client.core.diagnostics.WaitUntilReadyHelper
import com.couchbase.client.core.env.Authenticator
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.core.env.PasswordAuthenticator
import com.couchbase.client.core.env.SeedNode
import com.couchbase.client.core.msg.query.QueryRequest
import com.couchbase.client.core.service.ServiceType
import com.couchbase.client.core.util.ConnectionStringUtil
import com.couchbase.client.core.util.Golang
import com.couchbase.client.kotlin.codec.JacksonJsonSerializer
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.typeRef
import com.couchbase.client.kotlin.query.*
import com.couchbase.client.kotlin.query.QueryScanConsistency.NotBounded
import com.fasterxml.jackson.module.kotlin.jsonMapper
import kotlinx.coroutines.future.await
import kt.sandbox.toStringUtf8
import java.time.Duration
import java.util.*

public class Cluster internal constructor(
    environment: CoreEnvironment,
    private val authenticator: Authenticator,
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

    private fun RequestOptions.actualSpan(name: String) =
        core.context().environment().requestTracer().requestSpan(name, parentSpan)


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


    public suspend fun query(
        statement: String,
        options: RequestOptions = RequestOptions.DEFAULT,
        parameters: QueryParameters = QueryParameters.None,
        readonly: Boolean = false,
        adhoc: Boolean = true,

        serializer: JsonSerializer? = null,
        raw: Map<String, Any?> = emptyMap(),

        consistency: QueryScanConsistency = NotBounded,
        diagnostics: QueryDiagnostics = QueryDiagnostics.DEFAULT,
        tuning: QueryTuning = QueryTuning.DEFAULT,

        clientContextId: String? = UUID.randomUUID().toString(),

        ): QueryResult {

        if (!adhoc) TODO("adhoc not implemented")

        val timeout = options.actualQueryTimeout()

        // use interface type so Moshi doesn't freak out
        val queryJson: MutableMap<String, Any?> = HashMap<String, Any?>()

        queryJson["statement"] = statement
        queryJson["timeout"] = Golang.encodeDurationToMs(timeout)
        clientContextId?.let { queryJson["client_context_id"] = it }
        if (readonly) {
            queryJson["readonly"] = true
        }

        consistency.inject(queryJson)
        diagnostics.inject(queryJson)
        tuning.inject(queryJson)
        parameters.inject(queryJson)

        queryJson.putAll(raw)

        val actualSerializer = JacksonJsonSerializer(jsonMapper())
//        val actualSerializer = KotlinxJsonSerializer()
//        val actualSerializer = MoshiJsonSerializer(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())

        // use serializer from environment
        //  val type : TypeRef<Map<String, *>> = typeRef()

        val queryBytes = actualSerializer.serialize(queryJson, typeRef())

        println(queryBytes.toStringUtf8())

        val bucketName: String? = null;
        val scopeName: String? = null;
        val request = QueryRequest(
            timeout,
            core.context(),
            options.actualRetryStrategy(),
            authenticator,
            statement,
            queryBytes,
            readonly,
            clientContextId,
            options.actualSpan(TracingIdentifiers.SPAN_REQUEST_QUERY),
            bucketName,
            scopeName,
        )
        request.context().clientContext(options.clientContext)

        core.send(request)

        println("awaiting query response")
        val response = request.response().await()
        println("got query response")

        val defaultSerializer = JacksonJsonSerializer(jsonMapper())

        return QueryResult(response, serializer ?: defaultSerializer)
    }

}
