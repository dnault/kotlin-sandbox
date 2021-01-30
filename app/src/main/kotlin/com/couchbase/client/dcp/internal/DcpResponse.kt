package com.couchbase.client.dcp.internal

import io.netty.buffer.ByteBuf
import java.io.Closeable

internal class DcpResponse(val buffer: ByteBuf) : Closeable {
    fun humanize() = packet().humanize()
    fun packet() = DcpPacket(buffer)
    override fun close() {
        buffer.release()
    }
}
