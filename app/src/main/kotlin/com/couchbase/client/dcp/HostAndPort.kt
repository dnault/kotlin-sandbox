/*
 * Copyright 2021 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.dcp

import java.net.InetSocketAddress
import java.util.*

public class HostAndPort(host: String, public val port: Int) {
    private val isIpv6Literal: Boolean = host.contains(":")
    public val host: String = if (isIpv6Literal) canonicalizeIpv6Literal(host) else host

    public operator fun component1(): String = host
    public operator fun component2(): Int = port

    public fun copy(host: String = this.host, port: Int = this.port): HostAndPort = HostAndPort(host, port)

    public fun format(): String = "${formatHost()}:$port"

    public fun formatHost(): String = if (isIpv6Literal) "[$host]" else host

    override fun toString(): String = format()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HostAndPort

        if (port != other.port) return false
        if (host != other.host) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(host, port)

    private fun canonicalizeIpv6Literal(ipv6Literal: String): String {
        // This "resolves" the address, but because it's an IPv6 literal no DNS lookup is required
        return InetSocketAddress("[$ipv6Literal]", 0).hostString
    }
}
