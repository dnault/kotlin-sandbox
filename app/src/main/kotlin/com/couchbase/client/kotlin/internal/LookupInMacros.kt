package com.couchbase.client.kotlin.internal

internal class LookupInMacro {
    internal companion object {
        internal const val DOCUMENT = "\$document"

        internal const val EXPIRY_TIME = "\$document.exptime"

        internal const val CAS = "\$document.CAS"

        internal const val SEQ_NO = "\$document.seqno"

        internal const val LAST_MODIFIED = "\$document.last_modified"

        internal const val IS_DELETED = "\$document.deleted"

        internal const val VALUE_SIZE_BYTES = "\$document.value_bytes"

        internal const val REV_ID = "\$document.revid"

        internal const val FLAGS = "\$document.flags"
    }
}

