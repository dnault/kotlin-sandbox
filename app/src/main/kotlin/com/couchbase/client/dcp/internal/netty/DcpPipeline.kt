package com.couchbase.client.dcp.internal.netty

import awaitSuspend
import com.couchbase.client.dcp.internal.BODY_LENGTH_OFFSET
import com.couchbase.client.dcp.internal.DcpRequest
import com.couchbase.client.dcp.HostAndPort
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.oio.OioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.oio.OioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.AttributeKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

internal class DcpPipeline : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel) {
        ch.pipeline().apply {
            addLast(LengthFieldBasedFrameDecoder(Int.MAX_VALUE, BODY_LENGTH_OFFSET, 4, 12, 0, false))
            addLast(LoggingHandler(javaClass, LogLevel.INFO))
            addLast(RequestDispatcherHandler())
        }

//    if (environment.sslEnabled()) {
//      pipeline.addLast(new SslHandler(sslEngineFactory.get()));
//    }


//    if (environment.sslEnabled()) {
//      pipeline.addLast(new SslHandler(sslEngineFactory.get()));
////    }
//        pipeline.addLast(
//            LengthFieldBasedFrameDecoder(Int.MAX_VALUE, BODY_LENGTH_OFFSET, 4, 12, 0, false)
//        )
//
//        pipeline.addLast(RequestDispatcherHandler())

        //if (log.isTraceEnabled()) {

        //if (log.isTraceEnabled()) {
//        pipeline.addLast(LoggingHandler(LogLevel.INFO))
        //}


    }
}

/**
 * The original host and port used to created the channel.
 * Absolutely certain not to have been the victim of a reverse DNS lookup.
 *
 *
 * (It's not clear whether Channel.remoteAddress() has the same guarantee.
 * This seems like something that could vary by channel implementation.)
 */
private val HOST_AND_PORT = AttributeKey.valueOf<HostAndPort>("hostAndPort")


public fun main(): Unit = runBlocking {
    val globalEventLoopGroup = NioEventLoopGroup()


    val eventLoopGroup = globalEventLoopGroup;
    try {

        val address = HostAndPort("127.0.0.1", 11210)
        val socketConnectTimeout = Duration.ofSeconds(10)
        val bootstrap = Bootstrap()
            .handler(DcpPipeline())
            .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, socketConnectTimeout.toMillis().toInt())
            .remoteAddress(address.host, address.port)
            .attr(HOST_AND_PORT, address) // stash it away separately for safety (paranoia?)
            .group(eventLoopGroup)
            .channel(channelForEventLoopGroup(eventLoopGroup))

//
//    val f: CompletableFuture<Any>? = null
//    f.await()

            val ch = bootstrap.connect().awaitSuspend()
            logger.info { "connected! $ch" }

        withContext(Dispatchers.Unconfined) {
            val dispatcher = ch.pipeline().get(RequestDispatcherHandler::class.java)
            val versionResponse = dispatcher.sendRequest(DcpRequest.version()).awaitSuspend()

            versionResponse.use {
                logger.info("server version: ${it.packet().rawValue.toString(UTF_8)}")
            }

            val r2 = dispatcher.sendRequest(DcpRequest.version()).awaitSuspend()
            r2.use {
                logger.info("Again server version: ${it.packet().rawValue.toString(UTF_8)}")
            }
        }



        //ch.pipeline().writeAndFlush(DcpRequest.version().toByteBuf(123))


//
//    suspendCancellableCoroutine<Channel> { cont ->
//        val future = bootstrap.connect()
//        cont.cancelFutureOnCancellation(future)
//        future.addListener {
//            val channel = future.channel()
//            if (future.isSuccess) {
//                cont.resume(channel) { channel.close() }
//            } else {
//                cont.resumeWithException(future.cause())
//            }
//        }
//    }


        /*
    connectFuture.addListener(ChannelFutureListener {
        if (!it.isSuccess) {
            it.cause().printStackTrace();
        } else {
            val ch = it.channel()
            print("connected! ${ch}")
            val dispatcher = ch.pipeline().get(RequestDispatcherHandler::class.java)

            try {
                ch.eventLoop().execute {
                    dispatcher.sendRequest(DcpRequest.version())
                    dispatcher.sendRequest(DcpRequest.version())
                }
            } catch (t: Throwable) {
                // todo fail the promise or whatever
                t.printStackTrace()
            }
        }
    })


    */
    } finally {
        println("shutting down")
        eventLoopGroup.shutdownGracefully()
        eventLoopGroup.awaitTermination(10, TimeUnit.SECONDS)
        println("done")
    }
}

public fun channelForEventLoopGroup(group: EventLoopGroup): Class<out Channel> {
    if (group is EpollEventLoopGroup) {
        return EpollSocketChannel::class.java
    } else if (group is OioEventLoopGroup) {
        return OioSocketChannel::class.java
    } else if (group is KQueueEventLoopGroup) {
        return KQueueSocketChannel::class.java
    } else if (group is NioEventLoopGroup) {
        return NioSocketChannel::class.java
    }
    throw IllegalArgumentException("Unrecognized event loop: " + group.javaClass)
}
