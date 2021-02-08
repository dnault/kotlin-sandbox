package com.couchbase.client.kotlin.query

public data class QueryDiagnostics(
    val metrics: Boolean = false,
    val profile: QueryProfile = QueryProfile.OFF,
) {
    public companion object {
        public val DEFAULT: QueryDiagnostics = QueryDiagnostics()
    }

    internal fun inject(queryJson: MutableMap<String, Any?>): Unit {
        if (profile !== QueryProfile.OFF) {
            queryJson["profile"] = profile.toString()
        }

        if (!metrics) {
            queryJson["metrics"] = false
        }
    }

}
