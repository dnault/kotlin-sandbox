package com.couchbase.client.dcp.internal

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil


const val HEADER_SIZE = 24
const val KEY_LENGTH_OFFSET = 2
const val EXTRAS_LENGTH_OFFSET = 4
const val DATA_TYPE_OFFSET = 5
const val VBUCKET_OFFSET = 6
const val BODY_LENGTH_OFFSET = 8
const val OPAQUE_OFFSET = 12
const val CAS_OFFSET = 16

const val MAGIC_REQ = 0x80
const val MAGIC_RES = 0x81

const val MAGIC_REQ_FLEX = 0x08
const val MAGIC_RES_FLEX = 0x18

// Duplex mode, server-initiated request and client response
const val MAGIC_SERVER_REQ = 0x82
const val MAGIC_SERVER_RES = 0x83

/**
 * Dumps the given ByteBuf in the "wire format".
 *
 *
 * Note that the response is undefined if a buffer with a different
 * content than the KV protocol is passed in.
 *
 * @return the String ready to be printed/logged.
 */
fun ByteBuf.humanize(): String {
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
        ${if (isAnyRequest) "VBucket" else "Status "}        (6,7)    ${getUnsignedShort(VBUCKET_OFFSET).toHex(2)}
        Total Body     (8-11)   ${totalBodyLength.toHex(4)}
        Opaque         (12-15)  ${opaque.toHex(4)}
        CAS            (16-23)  ${cas.toHex(8)}
        """.trimIndent()
    ).append("\n")

    sb.append(formatOptionalBuffer("Extras", extras))
    sb.append(formatOptionalBuffer("Key", key))
    sb.append(formatOptionalBuffer("Content", rawContent))

    return sb.toString()
}

private fun formatOptionalBuffer(name: String, buffer: ByteBuf): String {
    return when {
        buffer.isReadable -> "+ ${name}:\n" +
                "${ByteBufUtil.prettyHexDump(buffer)}\n"
        else -> "No ${name}\n"
    }
}

private fun Number.toHex(bytes: Int): String = "0x%0${bytes * 2}x".format(this)

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
    return "${opcode.toHex(1)} (${getOpcodeName(opcode)})"
}

fun getOpcodeName(opcode: Int): String = "?"

/**
 * Returns the message content in its original form (possibly compressed).
 *
 * The returned buffer shares its reference count with the given buffer.
 */
val ByteBuf.rawContent: ByteBuf
    get() = slice(HEADER_SIZE + keyLength + extrasLength, contentLength)

val ByteBuf.extras: ByteBuf
    get() = slice(HEADER_SIZE, extrasLength)

val ByteBuf.key: ByteBuf
    get() = slice(HEADER_SIZE + extrasLength, keyLength)

val ByteBuf.extrasLength: Int
    get() = getByte(EXTRAS_LENGTH_OFFSET).toInt()

val ByteBuf.keyLength: Int
    get() = getShort(KEY_LENGTH_OFFSET).toInt()

val ByteBuf.totalBodyLength: Int
    get() = getInt(BODY_LENGTH_OFFSET)

val ByteBuf.contentLength: Int
    get() = totalBodyLength - keyLength - extrasLength

val ByteBuf.opcode: Int
    get() = getUnsignedByte(1).toInt()

val ByteBuf.magic: Int
    get() = getUnsignedByte(0).toInt()

val ByteBuf.dataType: Int
    get() = getUnsignedByte(DATA_TYPE_OFFSET).toInt()

val ByteBuf.opaque: Int
    get() = getInt(OPAQUE_OFFSET)

val ByteBuf.cas: Long
    get() = getLong(CAS_OFFSET)

val ByteBuf.vbucket: Int
    get() = if (isAnyRequest) getUnsignedShort(VBUCKET_OFFSET) else throw RuntimeException("Can't get vbucket; not a request")

val ByteBuf.statusCode: Int
    get() = if (!isAnyRequest) getUnsignedShort(VBUCKET_OFFSET) else throw RuntimeException("Can't get vbucket; not a request")

val ByteBuf.isAnyRequest: Boolean
    get() = magic == MAGIC_REQ || magic == MAGIC_REQ_FLEX


