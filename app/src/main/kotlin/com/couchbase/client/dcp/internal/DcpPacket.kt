package com.couchbase.client.dcp.internal

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil

internal const val HEADER_SIZE = 24
internal const val KEY_LENGTH_OFFSET = 2
internal const val EXTRAS_LENGTH_OFFSET = 4
internal const val DATA_TYPE_OFFSET = 5
internal const val VBUCKET_OFFSET = 6
internal const val BODY_LENGTH_OFFSET = 8
internal const val OPAQUE_OFFSET = 12
internal const val CAS_OFFSET = 16

internal const val MAGIC_REQ = 0x80
internal const val MAGIC_RES = 0x81

internal const val MAGIC_REQ_FLEX = 0x08
internal const val MAGIC_RES_FLEX = 0x18

// Duplex mode, server-initiated request and client response
internal const val MAGIC_SERVER_REQ = 0x82
internal const val MAGIC_SERVER_RES = 0x83

internal inline class DcpPacket(val buffer: ByteBuf) {
    val extras: ByteBuf
        get() = buffer.slice(HEADER_SIZE, extrasLength)

    val key: ByteBuf
        get() = buffer.slice(HEADER_SIZE + extrasLength, keyLength)

    val rawValue: ByteBuf
        get() = buffer.slice(HEADER_SIZE + keyLength + extrasLength, valueLength)

    private val extrasLength: Int
        get() = buffer.getByte(EXTRAS_LENGTH_OFFSET).toInt()

    private val keyLength: Int
        get() = buffer.getShort(KEY_LENGTH_OFFSET).toInt()

    private val totalBodyLength: Int
        get() = buffer.getInt(BODY_LENGTH_OFFSET)

    private val valueLength: Int
        get() = totalBodyLength - keyLength - extrasLength

    val opcode: Int
        get() = buffer.getUnsignedByte(1).toInt()

    val magic: Int
        get() = buffer.getUnsignedByte(0).toInt()

    val dataType: Int
        get() = buffer.getUnsignedByte(DATA_TYPE_OFFSET).toInt()

    val opaque: Int
        get() = buffer.getInt(OPAQUE_OFFSET)

    val cas: Long
        get() = buffer.getLong(CAS_OFFSET)

    val vbucket: Int
        get() = if (isAnyRequest) buffer.getUnsignedShort(VBUCKET_OFFSET) else throw RuntimeException("Can't get vbucket; not a request")

    val statusCode: Int
        get() = if (!isAnyRequest) buffer.getUnsignedShort(VBUCKET_OFFSET) else throw RuntimeException("Can't get vbucket; not a request")

    val isAnyRequest: Boolean
        get() = magic == MAGIC_REQ || magic == MAGIC_REQ_FLEX


    fun humanize(): String {
        val sb = StringBuilder()
        sb.append(
            """
        Field          (offset) (value)
        -----------------------------------
        Magic          (0)      ${formatMagic(magic)}
        Opcode         (1)      ${formatOpcode(opcode)}
        Key Length     (2,3)    ${keyLength.toHex(2)}
        Extras Length  (4)      ${extrasLength.toHex(1)}
        Data Type      (5)      ${dataType.toHex(1)}
        ${if (isAnyRequest) "VBucket" else "Status "}        (6,7)    ${
                buffer.getUnsignedShort(VBUCKET_OFFSET).toHex(2)
            }
        Total Body     (8-11)   ${totalBodyLength.toHex(4)}
        Opaque         (12-15)  ${opaque.toHex(4)}
        CAS            (16-23)  ${cas.toHex(8)}
        """.trimIndent()
        ).append("\n")

        sb.append(formatOptionalBuffer("Extras", extras))
        sb.append(formatOptionalBuffer("Key", key))
        sb.append(formatOptionalBuffer("Value", rawValue))

        return sb.toString()
    }
}

private fun formatOptionalBuffer(name: String, buffer: ByteBuf): String {
    return when {
        buffer.isReadable -> "+ ${name}:\n" +
                "${ByteBufUtil.prettyHexDump(buffer)}\n"
        else -> "No ${name}\n"
    }
}

private fun formatMagic(magic: Int): String {
    val name: String = when (magic) {
        MAGIC_REQ -> "REQUEST"
        MAGIC_RES -> "RESPONSE"
        MAGIC_REQ_FLEX -> "REQUEST-FLEX"
        MAGIC_RES_FLEX -> "RESPONSE-FLEX"
        MAGIC_SERVER_REQ -> "SERVER REQUEST"
        MAGIC_SERVER_RES -> "SERVER RESPONSE"
        else -> "?"
    }
    return "${magic.toHex(1)} (${name})"
}

private fun formatOpcode(opcode: Int): String {
    return "${opcode.toHex(1)} (${Opcode.getName(opcode)})"
}

private fun Number.toHex(bytes: Int): String = "0x%0${bytes * 2}x".format(this)
