package kt.sandbox

import io.netty.buffer.ByteBuf


const val MAGIC_REQ = 0x80
const val HEADER_SIZE = 24
const val KEY_LENGTH_OFFSET = 2
const val EXTRAS_LENGTH_OFFSET = 4
const val DATA_TYPE_OFFSET = 5
const val VBUCKET_OFFSET = 6
const val BODY_LENGTH_OFFSET = 8
const val OPAQUE_OFFSET = 12
const val CAS_OFFSET = 16

fun humanize(message: ByteBuf): String {



    return ""
}


