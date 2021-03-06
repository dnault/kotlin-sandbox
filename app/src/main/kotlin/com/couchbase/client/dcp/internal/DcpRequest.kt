package com.couchbase.client.dcp.internal

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.util.ReferenceCountUtil
import mu.KotlinLogging
import java.nio.charset.StandardCharsets.UTF_8

private val logger = KotlinLogging.logger {}

public class DcpRequest(
    private val opcode: Int,
    private val partition: Int = 0,
    private val extras: ByteBuf.() -> Unit = { },
    private val content: ByteBuf.() -> Unit = { },
) {
    override fun toString(): String = "${javaClass.name}(opcode=${opcode},partition=${partition})"

    public companion object {
        public fun version(): DcpRequest = DcpRequest(opcode = Opcode.VERSION)
    }

    public fun toByteBuf(opaque: Int, allocator: ByteBufAllocator = UnpooledByteBufAllocator.DEFAULT): ByteBuf {
        val buf = allocator.buffer()

        try {
            return buf.apply {
                writeByte(MAGIC_REQ)
                writeByte(opcode)

                // Set the rest of the header to zeroes, to be filled in below
                writeZero(HEADER_SIZE - writerIndex())

                setShort(VBUCKET_OFFSET, partition)
                setInt(OPAQUE_OFFSET, opaque)

                extras.invoke(this)

                val extrasLength = (writerIndex() - HEADER_SIZE)
                require(extrasLength <= 0xff) { "Request extras too long: $extrasLength bytes" }
                setByte(EXTRAS_LENGTH_OFFSET, extrasLength)

                // ASSUME NO KEY

                content.invoke(this)
                setInt(BODY_LENGTH_OFFSET, writerIndex() - HEADER_SIZE)
            }

        } catch (t: Throwable) {
            ReferenceCountUtil.safeRelease(buf)
            throw t
        }
    }
}

private fun ByteBuf.writeString(value: String): ByteBuf = writeBytes(value.toByteArray(UTF_8))

public fun main() {

    val extra = "so extra"

    val request = DcpRequest(
        opcode = Opcode.DCP_NOOP,
        partition = 42,
        extras = { writeInt(extra.length) },
//        extras = { writeZero(0xff+1) },
        content = { writeString("xyzzy") },
    )

    println(request)

    val message = DcpPacket(request.toByteBuf(opaque = Integer.MIN_VALUE + 1))

    println(ByteBufUtil.prettyHexDump(message.buffer))
    println(message.humanize())


}
