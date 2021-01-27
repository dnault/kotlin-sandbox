package com.couchbase.client.dcp.internal.netty

import com.couchbase.client.dcp.NotConnectedException
import com.couchbase.client.dcp.internal.DcpResponse
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.util.concurrent.Promise
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}


class SimpleRequestDispatcherHandler : ChannelDuplexHandler() {
    // TODO add some contextual information about the request
    // so failures can be matched with the requests
    data class OutstandingRequest(val opaque: Int, val promise: Promise<DcpResponse>)

    /**
     * A counter for assigning an ID to each request. There should never be
     * two outstanding requests with the same ID on the same channel.
     *
     * Must only be accessed/modified by the event loop thread.
     */
    private var nextOpaque = 0

    /**
     * Holds the promises issued by [.sendRequest] that have not yet
     * been fulfilled by [.channelRead] or failed by [.channelInactive].
     *
     * Must only be accessed/modified by the event loop thread.
     */
    private val outstandingRequests: Queue<OutstandingRequest> = ArrayDeque()

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val connectionClosed: Exception = NotConnectedException("Channel became inactive while awaiting response.")
        for ((_, promise) in outstandingRequests) {
            try {
                promise.setFailure(connectionClosed)
            } catch (t: Throwable) {
                logger.error(t) { "Failed to set promise failure" }
            }
        }
        outstandingRequests.clear()
        super.channelInactive(ctx)
    }
}
