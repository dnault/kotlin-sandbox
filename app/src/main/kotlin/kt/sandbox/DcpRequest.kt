package kt.sandbox

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.util.ReferenceCountUtil
import java.nio.charset.StandardCharsets.UTF_8

class DcpRequest(
        private val opcode: Int,
        private val partition: Int,
        private val extras: (ByteBuf) -> Unit = { },
        private val content: (ByteBuf) -> Unit = { },
) {

    override fun toString(): String = "${javaClass.name}(opcode=${opcode},partition=${partition})"

    fun toByteBuf(opaque: Int, allocator: ByteBufAllocator = UnpooledByteBufAllocator.DEFAULT): ByteBuf {
        val buf = allocator.buffer();

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
                if (extrasLength > 0xff) {
                    throw RuntimeException("Request extras too long")
                }
                setByte(EXTRAS_LENGTH_OFFSET, extrasLength)

                // ASSUME NO KEY

                content.invoke(this)
                setInt(BODY_LENGTH_OFFSET, writerIndex() - HEADER_SIZE)
            }

        } catch (t: Throwable) {
            ReferenceCountUtil.safeRelease(buf)
            throw t;
        }
    }
}


fun ByteBuf.writeString(value: String): ByteBuf = writeBytes(value.toByteArray(UTF_8))

fun main() {
    val request = DcpRequest(
            opcode = 0x5c,
            partition = 42,
            extras = { it.writeString("so extra") },
            content = { it.writeString("xyzzy") },
    )

    println(request)

    println(ByteBufUtil.prettyHexDump(request.toByteBuf(opaque = 456)))
}