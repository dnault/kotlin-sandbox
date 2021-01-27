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
import java.time.Duration
import java.util.*

class AsyncCollection(name: String, scopeName: String, bucketName: String, private val core: Core) {

    private val collectionIdentifier = CollectionIdentifier(bucketName, Optional.of(scopeName), Optional.of(name))
    private val defaultGetOptions = GetOptions()

    suspend fun get(id: String, options: GetOptions = defaultGetOptions): GetResult {
        val timeout = if (options.timeoutMillis == null) {
            core.context().environment().timeoutConfig().kvTimeout()
        } else {
            Duration.ofMillis(options.timeoutMillis)
        }
        val request = GetRequest(id, timeout, core.context(), collectionIdentifier, core.context().environment().retryStrategy(), null)
        core.send(request)
        val response = request.response().await()

        if (response.status().success()) {
            return GetResult(response.cas(), response.flags(), Optional.empty(), response.content())
        } else {
            throw DefaultErrorUtil.keyValueStatusToException(request, response)
        }
    }

}