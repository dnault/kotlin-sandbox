import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.FutureListener
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun ChannelFuture.awaitSuspend(): Channel {
    return suspendCancellableCoroutine { cont: CancellableContinuation<Channel> ->
        cont.invokeOnCancellation { cancel(true) }
        addListener(ChannelFutureListener {
            when {
                it.isSuccess -> cont.resume(it.channel())
                else -> cont.resumeWithException(it.cause())
            }
        })
    }
}

internal suspend fun <T> Future<T>.awaitSuspend(): T {
    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        cont.invokeOnCancellation { cancel(true) }
        addListener(FutureListener<T> {
            when {
                it.isSuccess -> cont.resume(it.now)
                else -> cont.resumeWithException(it.cause())
            }
        })
    }
}
