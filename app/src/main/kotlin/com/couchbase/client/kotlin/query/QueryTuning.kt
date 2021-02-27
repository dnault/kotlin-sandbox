package com.couchbase.client.kotlin.query

public class QueryTuning(
    public val flexIndex: Boolean = false,
    public val maxParallelism: Int? = null,
    public val scanCap: Int? = null,
    public val pipelineBatch: Int? = null,
    public val pipelineCap: Int? = null,
) {

    public companion object {
        public val DEFAULT: QueryTuning = QueryTuning()
    }

    override fun toString(): String {
        return "QueryTuning(flexIndex=$flexIndex, maxParallelism=$maxParallelism, scanCap=$scanCap, pipelineBatch=$pipelineBatch, pipelineCap=$pipelineCap)"
    }

    internal fun inject(queryJson: MutableMap<String, Any?>): Unit {
        maxParallelism?.let { queryJson["max_parallelism"] = it.toString() }
        pipelineCap?.let { queryJson["pipeline_cap"] = it.toString() }
        pipelineBatch?.let { queryJson["pipeline_batch"] = it.toString() }
        scanCap?.let { queryJson["scan_cap"] = it.toString() }
        if (flexIndex) queryJson["use_fts"] = true

    }

}
