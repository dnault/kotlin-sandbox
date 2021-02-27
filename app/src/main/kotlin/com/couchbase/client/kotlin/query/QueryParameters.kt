package com.couchbase.client.kotlin.query

public sealed class QueryParameters {

    internal abstract fun inject(queryJson: MutableMap<String, Any?>)

    public object None : QueryParameters() {
        override fun inject(queryJson: MutableMap<String, Any?>) {
        }
    }

    public class Named(private val values: Map<String, Any?>) : QueryParameters() {
        public constructor(vararg pairs: Pair<String, Any?>) : this(pairs.toMap())

        override fun inject(queryJson: MutableMap<String, Any?>): Unit =
            values.forEach { (key, value) ->
                queryJson[key.addPrefixIfAbsent("$")] = value
            }

        private fun String.addPrefixIfAbsent(prefix: String) =
            if (startsWith(prefix)) this else prefix + this
    }

    public class Positional(private val values: List<Any?>) : QueryParameters() {
        public constructor(vararg values: Any?) : this(values.toList())

        override fun inject(queryJson: MutableMap<String, Any?>): Unit {
            if (values.isNotEmpty()) {
                queryJson["args"] = values
            }
        }
    }

    public companion object {
        public fun positional(values: List<Any?>) : QueryParameters = Positional(values)
        public fun named(values: Map<String, Any?>) : QueryParameters = Named(values)
    }
}
