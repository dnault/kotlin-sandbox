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

import com.couchbase.client.core.env.Authenticator
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.core.env.PasswordAuthenticator
import com.couchbase.client.core.env.SeedNode
import com.couchbase.client.core.util.ConnectionStringUtil
import com.couchbase.client.kotlin.kv.GetOptions
import com.couchbase.client.kotlin.kv.GetResult
import com.couchbase.client.kotlin.query.QueryOptions
import com.couchbase.client.kotlin.query.QueryResult
import kotlinx.coroutines.runBlocking

class Cluster(environment: CoreEnvironment, authenticator: Authenticator, seedNodes: Set<SeedNode>) {

    val async = AsyncCluster(environment, authenticator, seedNodes)

    companion object {

        fun connect(connectionString: String, username: String, password: String): Cluster {
            return connect(connectionString, ClusterOptions(PasswordAuthenticator.create(username, password)))
        }


        fun connect(connectionString: String, options: ClusterOptions): Cluster  {
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

    fun bucket(name: String): Bucket {
        return Bucket(async.bucket(name))
    }

    fun query(statement: String, options: QueryOptions = QueryOptions()): QueryResult {
        return runBlocking { async.query(statement, options) }
    }

}
