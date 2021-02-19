package com.couchbase.client.kotlin.query

public class QueryWarning(
    public val code: Int,
    public val message: String,
) {
    override fun toString(): String {
        return "QueryWarning(code=$code, message='$message')"
    }
}
