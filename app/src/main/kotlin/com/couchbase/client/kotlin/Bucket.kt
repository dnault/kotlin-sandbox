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
import com.couchbase.client.core.io.CollectionIdentifier
import com.couchbase.client.core.service.ServiceType
import kotlinx.coroutines.future.await
import java.time.Duration

public class Bucket internal constructor(
    public val name: String,
    internal val core: Core,
) {

    public fun defaultCollection(): Collection {
        return Collection(CollectionIdentifier.DEFAULT_COLLECTION, CollectionIdentifier.DEFAULT_SCOPE, name, core)
    }

    public suspend fun waitUntilReady(
        timeout: Duration,
        serviceTypes: Set<ServiceType> = emptySet(),
        desiredState: ClusterState = ClusterState.ONLINE,
    ): Bucket {
        WaitUntilReadyHelper.waitUntilReady(core, serviceTypes, timeout, desiredState, name.toOptional()).await()
        return this
    }
}

internal fun <T> T?.toOptional() = java.util.Optional.ofNullable(this)
