package kt.sandbox

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.util.function.Consumer

object DcpRequestBuilder {
    fun build(opcode: Int, extrasWriter: Consumer<ByteBuf>): ByteBuf {
        val buf = Unpooled.buffer()
        buf.writeInt(opcode)
        extrasWriter.accept(buf)
        return buf
    }
}