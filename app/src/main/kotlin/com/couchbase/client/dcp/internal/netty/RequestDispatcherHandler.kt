package com.couchbase.client.dcp.internal.netty

import com.couchbase.client.dcp.NotConnectedException
import com.couchbase.client.dcp.internal.DcpPacket
import com.couchbase.client.dcp.internal.DcpRequest
import com.couchbase.client.dcp.internal.DcpResponse
import com.couchbase.client.dcp.internal.MAGIC_RES
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.ImmediateEventExecutor
import io.netty.util.concurrent.Promise
import mu.KotlinLogging
import java.io.IOException
import java.util.*

private val logger = KotlinLogging.logger {}

internal class RequestDispatcherHandler : ChannelInboundHandlerAdapter() {

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


    /**
     * A reference to the channel context so [.sendRequest] can write
     * messages to the channel.
     *
     *
     * A non-null value indicates the channel is active and accepting requests.
     * The value is initialized when the channel becomes active, and set back
     * to null when the channel becomes inactive.
     *
     *
     * It's volatile so the value set by [.channelActive] in the event loop
     * thread is visible when [.sendRequest] is called from another thread.
     */
    @Volatile
    private var volatileContext: ChannelHandlerContext? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        this.volatileContext = ctx
        nextOpaque = Int.MIN_VALUE
        super.channelActive(ctx)
    }


    /**
     * Fails all the promises in the [.outstandingRequests] queue when the
     * channel becomes inactive.
     *
     *
     * Netty always invokes this method in the event loop thread. To ensure
     * this method never runs concurrently with [.unsafeSendRequest],
     * we only call that method in the event loop thread as well.
     */
    override fun channelInactive(ctx: ChannelHandlerContext) {
        volatileContext = null // Make sure future `sendRequest` calls fail.
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


    fun sendRequest(request: DcpRequest): Future<DcpResponse> {
        // Since this method might be called from outside the event loop thread,
        // it's possible for `channelInactive` to run concurrently and set
        // `volatileContext` to null at any time. Take a snapshot so the value
        // doesn't change out from under us!
        val ctx = volatileContext

        // Regardless of whether we're in the event loop thread, a null context
        // indicates the channel is inactive and not receiving requests.
        if (ctx == null) {
            ReferenceCountUtil.safeRelease(request)
            return ImmediateEventExecutor.INSTANCE.newFailedFuture(
                NotConnectedException("Failed to issue request; channel is not active.")
            )
        }
        val executor = ctx.executor()
        val promise = executor.newPromise<DcpResponse>()

        // Here's where the paths diverge. If we're in the event loop thread,
        // it's impossible for channelInactive to be running at the same time,
        // so we can safely enqueue the request immediately.
        if (executor.inEventLoop()) {
            unsafeSendRequest(ctx, request, promise)
            return promise
        }

        // If we got here, we're not running in the event loop thread.
        // To prevent `channelInactive` from interfering while the request
        // is enqueued, schedule a task to send the request later in the
        // event loop thread.
        //
        // Netty would reschedule the `writeAndFlush` operation to run in the
        // event loop thread anyway, so there shouldn't be much additional
        // overhead in doing the scheduling ourselves.
        //
        // Another reason to reschedule: Netty does not guarantee ordering
        // between writes initiated in the event loop and writes initiated
        // outside the event loop. In order to guarantee the outstanding
        // requests get enqueued in the same order they are written to the
        // channel, it's necessary for all write + enqueue operations to happen
        // in the event loop. See https://github.com/netty/netty/issues/3887
        try {
            executor.submit {
                // The channel may have become inactive after this task
                // was submitted, so check one more time.
                //
                // Since we're now in the event loop thread, there's no risk of
                // channelInactive() changing the value of `volatileContext`.
                // This assignment is just to avoid redundant volatile reads.
                val ctx = volatileContext
                if (ctx == null) {
                    ReferenceCountUtil.safeRelease(request)
                    promise.setFailure(NotConnectedException("Failed to issue request; channel is not active."))
                } else {
                    unsafeSendRequest(ctx, request, promise)
                }
            }
        } catch (t: Throwable) {
            ReferenceCountUtil.safeRelease(request)
            promise.setFailure(t)
        }
        return promise
    }


    /**
     * Writes the request to the channel and records the promise in the
     * outstanding request queue.
     *
     * "Unsafe" because this method must only be called in the event loop thread,
     * to ensure it never runs concurrently with [.channelInactive],
     * and so that outstanding requests are enqueued in the same order
     * they are written to the channel.
     */
    private fun unsafeSendRequest(
        ctx: ChannelHandlerContext,
        request: DcpRequest,
        promise: Promise<DcpResponse>
    ) {
        check(ctx.executor().inEventLoop()) { "Must not be called outside event loop" }
        try {
            //metrics.trackDcpRequest(promise, request)
            val opaque = nextOpaque++
            val buf = request.toByteBuf(opaque)

            // Don't need to be notified if/when the bytes are written,
            // so use void promise to save an allocation.
            // TODO actually, we'd want to know the request failed, wouldn't we?
            ctx.writeAndFlush(buf, ctx.voidPromise())
            outstandingRequests.add(OutstandingRequest(opaque, promise))
        } catch (t: Throwable) {
            promise.setFailure(t)
        }
    }


    /**
     * Reads server responses and uses them to fulfill promises returned by
     * [.sendRequest].
     *
     * Dispatches other incoming messages to either the data or the control feeds.
     */
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val message = msg as ByteBuf
        //  metrics.incrementBytesRead(message.readableBytes())

        // The majority of messages are likely to be stream requests, not responses.
        val magic = message.getUnsignedByte(0).toInt()
        if (magic != MAGIC_RES) {
            TODO("handle request")
            return
        }

        // "The current protocol dictates that the server won't start
        // processing the next command until the current command is completely
        // processed (due to the lack of barriers or any other primitives to
        // enforce execution order). The protocol defines some "quiet commands"
        // which won't send responses in certain cases (success for mutations,
        // not found for gets etc). The client would know that such commands
        // was executed when it encounters the response for the next command
        // requested issued by the client."
        // -- https://github.com/couchbase/kv_engine/blob/master/docs/BinaryProtocol.md#introduction-1

        // The DCP client does not send any "quiet commands", so we assume
        // a 1:1 relationship between requests and responses, and FIFO order.
        val request = outstandingRequests.poll()
        val responseOpaque = DcpPacket(message).opaque

        if (request == null || responseOpaque != request.opaque) {
            // Should never happen so long as all requests are made via sendRequest()
            // and successfully written to the channel.
            try {
                if (request == null) {
                    logger.error { "Received unexpected response\n${DcpPacket(message).humanize()}; closing connection" }
                } else {
                    logger.error { "Unexpected response with opaque ${responseOpaque}} (expected ${request.opaque}); closing connection" }
                    request.promise.setFailure(IOException("Response arrived out of order"))
                }

                ctx.close()
                return
            } finally {
                message.release()
            }
        }
        request.promise.setSuccess(DcpResponse(message))
    }

}
