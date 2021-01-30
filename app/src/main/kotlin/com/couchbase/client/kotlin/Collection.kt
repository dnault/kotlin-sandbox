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
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.core.error.DefaultErrorUtil
import com.couchbase.client.core.io.CollectionIdentifier
import com.couchbase.client.core.msg.kv.GetRequest
import com.couchbase.client.kotlin.kv.GetResult
import kotlinx.coroutines.future.await
import java.util.*

public class Collection(
    name: String,
    scopeName: String,
    bucketName: String,
    private val core: Core,
) {
    private val env: CoreEnvironment
        get() = core.context().environment()

    private val collectionIdentifier = CollectionIdentifier(bucketName, Optional.of(scopeName), Optional.of(name))

    private fun RequestOptions.actualKvTimeout() = timeout ?: env.timeoutConfig().kvTimeout()
    private fun RequestOptions.actualRetryStrategy() = retryStrategy ?: env.retryStrategy()
    private fun RequestOptions.actualSpan(name: String) = env.requestTracer().requestSpan(name, parentSpan)

    public suspend fun get(
        id: String,
        withExpiry: Boolean = false,
        projections: List<String> = emptyList(),
        options: RequestOptions = RequestOptions.DEFAULT,
    ): GetResult {

        if (withExpiry || projections.isNotEmpty()) TODO("implement subdoc")

        val request = GetRequest(
            id,
            options.actualKvTimeout(),
            core.context(),
            collectionIdentifier,
            options.actualRetryStrategy(),
            options.actualSpan(TracingIdentifiers.SPAN_REQUEST_KV_GET),
        )
        request.context().clientContext(options.clientContext)

        core.send(request)
        try {
            val response = request.response().await()
            if (response.status().success()) {
                return GetResult(response.cas(), response.flags(), Optional.empty(), response.content())
            }
            throw DefaultErrorUtil.keyValueStatusToException(request, response)

        } finally {
            request.context().logicallyComplete()
        }
    }

}

