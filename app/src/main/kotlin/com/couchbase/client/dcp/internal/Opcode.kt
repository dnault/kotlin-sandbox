package com.couchbase.client.dcp.internal

class Opcode {
    companion object {
        const val DCP_NOOP = 0x5c
        const val VERSION = 0x0b

        fun getName(opcode: Int): String = "?"
    }
}
