package com.couchbase.client.kotlin.query

import com.couchbase.client.core.msg.kv.MutationToken
import java.lang.Long

/**
 * Like:
 * ```
 * {
 *   "bucketName" : {
 *     "partitionId" : [ sequenceNumber, "partitionUuid" ]
 *   }
 *   ...
 * }
 * ```
 */
internal fun Collection<MutationToken>.formatForQuery(): Map<String, Any> {
    // {bucket name -> { vbucket -> [sequence number, vbucketUUID]}
    val result: MutableMap<String, MutableMap<String, List<Any>>> = HashMap()

    for (token in withoutRedundant(this)) {
        val bucket = result.getOrPut(token.bucketName(), { HashMap() })
        bucket[token.partitionID().toString()] =
            listOf(token.sequenceNumber(), token.partitionUUID().toString())
    }
    return result
}

/**
 * If any tokens in the input list have the same bucket name and partition ID,
 * only the one with the highest sequence number will be present in the returned list.
 */
private fun withoutRedundant(tokens: Collection<MutationToken>): Collection<MutationToken> {
    val map = HashMap<BucketAndPartition, MutationToken>()

    for (token in tokens) {
        val key = token.bucketAndPartition()
        val existing = map[key]
        // todo complain if existing has a different partition UUID?
        if (existing == null || Long.compareUnsigned(token.sequenceNumber(), existing.sequenceNumber()) > 0) {
            map[key] = token
        }
    }

    return map.values
}

private fun MutationToken.bucketAndPartition() = BucketAndPartition(bucketName(), partitionID())
private data class BucketAndPartition(val bucket: String, val partition: Short)
