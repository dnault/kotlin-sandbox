package com.couchbase.client.kotlin.kv

import com.couchbase.client.core.msg.kv.MutationToken

public class MutationResult internal constructor(
    public val cas: Long,
    public val mutationToken: MutationToken?,
) {
    override fun toString(): String = "MutationResult(cas=$cas, mutationToken=$mutationToken)"
}
