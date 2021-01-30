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
import com.couchbase.client.core.error.DefaultErrorUtil
import com.couchbase.client.core.io.CollectionIdentifier
import com.couchbase.client.core.msg.kv.GetRequest
import com.couchbase.client.kotlin.kv.GetOptions
import com.couchbase.client.kotlin.kv.GetResult
import kotlinx.coroutines.future.await
import java.util.*

class AsyncCollection(
    name: String,
    scopeName: String,
    bucketName: String,
    private val core: Core
) {
    private val collectionIdentifier = CollectionIdentifier(bucketName, Optional.of(scopeName), Optional.of(name))

    private fun CommonOptions.actualKvTimeout() = timeout ?: core.context().environment().timeoutConfig().kvTimeout()
    private fun CommonOptions.actualRetryStrategy() = retryStrategy ?: core.context().environment().retryStrategy()

    suspend fun get(id: String, options: GetOptions = GetOptions.DEFAULT): GetResult {
        val request = GetRequest(id,
            options.actualKvTimeout(),
            core.context(),
            collectionIdentifier,
            options.actualRetryStrategy(),
            options.parentSpan
        )
        core.send(request)
        val response = request.response().await()

        if (response.status().success()) {
            return GetResult(response.cas(), response.flags(), Optional.empty(), response.content())
        } else {
            throw DefaultErrorUtil.keyValueStatusToException(request, response)
        }
    }
}
