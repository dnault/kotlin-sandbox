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
import com.couchbase.client.core.error.CouchbaseException
import com.couchbase.client.core.error.DefaultErrorUtil
import com.couchbase.client.core.error.InvalidArgumentException
import com.couchbase.client.core.error.context.ReducedKeyValueErrorContext
import com.couchbase.client.core.io.CollectionIdentifier
import com.couchbase.client.core.msg.Request
import com.couchbase.client.core.msg.Response
import com.couchbase.client.core.msg.kv.*
import com.couchbase.client.core.projections.ProjectionsApplier
import com.couchbase.client.core.service.kv.Observe
import com.couchbase.client.core.service.kv.ObserveContext
import com.couchbase.client.core.util.Validators
import com.couchbase.client.kotlin.codec.*
import com.couchbase.client.kotlin.internal.LookupInMacro
import com.couchbase.client.kotlin.kv.Durability
import com.couchbase.client.kotlin.kv.Durability.Synchronous
import com.couchbase.client.kotlin.kv.Expiry
import com.couchbase.client.kotlin.kv.GetResult
import com.couchbase.client.kotlin.kv.MutationResult
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.util.*
import kotlin.system.measureNanoTime

public class Collection internal constructor(
    public val name: String,
    public val scopeName: String,
    public val bucketName: String,
    private val core: Core,
) {
    // todo get from environment
    public val defaultTranscoder: Transcoder = JsonTranscoder(
        JacksonJsonSerializer(jacksonObjectMapper()))

    private val env: CoreEnvironment
        get() = core.context().environment()

    private val collectionIdentifier = CollectionIdentifier(bucketName, Optional.of(scopeName), Optional.of(name))

    private fun RequestOptions.actualKvTimeout() = timeout ?: env.timeoutConfig().kvTimeout()
    private fun RequestOptions.actualRetryStrategy() = retryStrategy ?: env.retryStrategy()
    private fun RequestOptions.actualSpan(name: String) = env.requestTracer().requestSpan(name, parentSpan)

    private suspend fun observe(
        request: Request<*>,
        id: String,
        durability: Durability.Polling,
        cas: Long,
        mutationToken: Optional<MutationToken>,
        remove: Boolean = false,
    ) {
        val ctx = ObserveContext(
            core.context(),
            durability.persistTo.coreHandle,
            durability.replicateTo.coreHandle,
            mutationToken,
            cas,
            collectionIdentifier,
            id,
            remove,
            request.timeout(),
            request.requestSpan()
        )

        Observe.poll(ctx).toFuture().await()
    }

    public suspend inline fun <reified T> upsert(
        id: String,
        content: T,
        options: RequestOptions = RequestOptions.DEFAULT,
        transcoder: Transcoder = defaultTranscoder,
        durability: Durability = Durability.inMemoryOnActive(),
        expiry: Expiry = Expiry.none(),
    ): MutationResult {
        return upsertWithReifiedType(id, content, typeRef(), options, transcoder, durability, expiry)
    }

    public suspend fun <T> upsertWithReifiedType(
        id: String,
        content: T,
        contentType: TypeRef<T>,
        options: RequestOptions = RequestOptions.DEFAULT,
        transcoder: Transcoder,
        durability: Durability = Durability.inMemoryOnActive(),
        expiry: Expiry = Expiry.none(),
    ): MutationResult {
        val request = upsertRequest(id, content, contentType, options, transcoder, durability, expiry)
        try {
            val response = exec(request, options)

            if (durability is Durability.Polling) {
                observe(request, id, durability, response.cas(), response.mutationToken())
            }

            return MutationResult(response.cas(), response.mutationToken().orElse(null))

        } finally {
            request.endSpan()
        }
    }

    private fun <T> upsertRequest(
        id: String,
        content: T,
        contentType: TypeRef<T>,
        options: RequestOptions,
        transcoder: Transcoder,
        durability: Durability,
        expiry: Expiry,
    ): UpsertRequest {
        Validators.notNullOrEmpty(id, "Id") { ReducedKeyValueErrorContext.create(id, collectionIdentifier) }
        Validators.notNull(content, "Content", { ReducedKeyValueErrorContext.create(id, collectionIdentifier) })
        val timeout = options.actualKvTimeout()
        val retryStrategy = options.actualRetryStrategy()
        val span = options.actualSpan(TracingIdentifiers.SPAN_REQUEST_KV_UPSERT)
        val encodeSpan = env.requestTracer().requestSpan(TracingIdentifiers.SPAN_REQUEST_ENCODING, span)

        var encodedContent: Content
        val encodingNanos = measureNanoTime {
            try {
                encodedContent = transcoder.encode(content, contentType)
            } finally {
                encodeSpan.end()
            }
        }

        val syncDurability = if (durability is Synchronous) durability.level else null
        val request = UpsertRequest(id, encodedContent.bytes, expiry.encode(), encodedContent.flags,
            timeout, core.context(), collectionIdentifier, retryStrategy, syncDurability.toOptional(), span)
        request.context().encodeLatency(encodingNanos)
        return request
    }

    public suspend fun get(
        id: String,
        options: RequestOptions = RequestOptions.DEFAULT,
        withExpiry: Boolean = false,
        projections: List<String> = emptyList(),
    ): GetResult {

        if (!withExpiry && projections.isEmpty()) {
            val request = GetRequest(
                id,
                options.actualKvTimeout(),
                core.context(),
                collectionIdentifier,
                options.actualRetryStrategy(),
                options.actualSpan(TracingIdentifiers.SPAN_REQUEST_KV_GET),
            )
            try {
                exec(request, options).apply {
                    return GetResult.withUnknownExpiry(id, cas(), flags(), content(), defaultTranscoder)
                }
            } finally {
                request.endSpan()
            }
        }

        val request = createSubdocGetRequest(id, withExpiry, projections, options)
        try {
            exec(request, options).apply {
                return parseSubdocGet(id, this)
            }
        } finally {
            request.endSpan()
        }
    }

    private fun createSubdocGetRequest(
        id: String,
        withExpiry: Boolean = false,
        projections: List<String>,
        options: RequestOptions,
    ): SubdocGetRequest {
        validateProjections(id, projections, withExpiry)
        val commands = ArrayList<SubdocGetRequest.Command>(16)

        if (projections.isEmpty()) {
            // fetch whole document
            commands.add(SubdocGetRequest.Command(SubdocCommandType.GET_DOC, "", false, commands.size))
        } else for (projection in projections) {
            commands.add(SubdocGetRequest.Command(SubdocCommandType.GET, projection, false, commands.size))
        }

        if (withExpiry) {
            // xattrs must go first
            commands.add(0, SubdocGetRequest.Command(
                SubdocCommandType.GET,
                LookupInMacro.EXPIRY_TIME,
                true,
                commands.size
            ))

            // If we have projections, there is no need to fetch the flags
            // since only JSON is supported that implies the flags.
            // This will also "force" the transcoder on the read side to be
            // JSON aware since the flags are going to be hard-set to the
            // JSON compat flags.
            if (projections.isEmpty()) {
                commands.add(1, SubdocGetRequest.Command(
                    SubdocCommandType.GET,
                    LookupInMacro.FLAGS,
                    true,
                    commands.size
                ))
            }
        }

        return SubdocGetRequest(
            options.actualKvTimeout(),
            core.context(),
            collectionIdentifier,
            options.actualRetryStrategy(),
            id,
            0x00,
            commands,
            options.actualSpan(TracingIdentifiers.SPAN_REQUEST_KV_LOOKUP_IN)
        )
    }

    private fun validateProjections(id: String, projections: List<String>, withExpiry: Boolean) {
        try {
            if (projections.any { it.isEmpty() }) {
                throw InvalidArgumentException.fromMessage("Empty string is not a valid projection.")
            }

            if (withExpiry) {
                if (projections.size > 15) {
                    throw InvalidArgumentException.fromMessage("Only a maximum of 16 fields can be " +
                            "projected per request due to a server limitation (includes the expiration macro as one field).")
                }
            } else {
                if (projections.size > 16) {
                    throw InvalidArgumentException.fromMessage("Only a maximum of 16 fields can be " +
                            "projected per request due to a server limitation.")
                }
            }

        } catch (t: Throwable) {
            throw InvalidArgumentException("Argument validation failed",
                t,
                ReducedKeyValueErrorContext.create(id, collectionIdentifier))
        }
    }

    private fun parseSubdocGet(id: String, response: SubdocGetResponse): GetResult {
        val cas = response.cas()
        var exptime: ByteArray? = null
        var content: ByteArray? = null
        var flags: ByteArray? = null
        for (value in response.values()) {
            if (value != null) {
                if (LookupInMacro.EXPIRY_TIME.equals(value.path())) {
                    exptime = value.value()
                } else if (LookupInMacro.FLAGS.equals(value.path())) {
                    flags = value.value()
                } else if (value.path().isEmpty()) {
                    content = value.value()
                }
            }
        }
        val convertedFlags =
            if (flags == null) CodecFlags.JSON_COMPAT_FLAGS else String(flags, UTF_8).toInt()
        if (content == null) {
            content = try {
                ProjectionsApplier.reconstructDocument(response)
            } catch (e: Exception) {
                throw CouchbaseException("Unexpected Exception while decoding Sub-Document get", e)
            }
        }

        val expiration = parseExpiry(exptime)
        return GetResult.withKnownExpiry(id, cas, convertedFlags, content!!, defaultTranscoder, expiration)
    }

    private fun parseExpiry(expiryBytes: ByteArray?): Instant? {
        if (expiryBytes == null) return null
        val epochSecond = String(expiryBytes, UTF_8).toLong()
        return if (epochSecond == 0L) null else Instant.ofEpochSecond(epochSecond)
    }

    private suspend fun <R : Response> exec(
        request: KeyValueRequest<R>,
        options: RequestOptions,
    ): R {
        val response = core.exec(request, options)
        if (response.status().success()) {
            return response
        }

        if (response is SubdocGetResponse) {
            response.error().ifPresent { throw it }
        }

        throw DefaultErrorUtil.keyValueStatusToException(request, response)
    }

    override fun toString(): String {
        return "Collection(name='$name', scopeName='$scopeName', bucketName='$bucketName')"
    }
}

internal suspend fun <R : Response> Core.exec(request: Request<R>, options: RequestOptions): R {
    request.context().clientContext(options.clientContext)
    send(request)
    return request.response().await()
}

internal fun <R : Response> Request<R>.endSpan() = context().logicallyComplete()
