import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.atomic.AtomicBoolean


private val sseLogger: Logger = LoggerFactory.getLogger("sse-coroutines")

/**
 * Launches a coroutine that is cancelled when the [SseEmitter] is completed or cancelled.
 *
 * Used for sending Server-Sent Events from coroutines.
 *
 * @param block The coroutine block to execute.
 */
fun launchServerSentEvent(
    block: suspend CoroutineScope.(SseEmitter) -> Unit
): SseEmitter {
    return SseEmitter().also { emitter ->
        emitterScope(emitter).launch {
            try {
                block(emitter)
            } catch (e: Throwable) {
                sseLogger.error("Error in SSE coroutine", e)
            }
        }
    }
}

/**
 * Creates a [CoroutineScope] that is cancelled when the [SseEmitter] is completed or cancelled.
 *
 * Used for sending Server-Sent Events from coroutines.
 *
 * @param emitter The [SseEmitter] to track.
 */
fun emitterScope(emitter: SseEmitter): CoroutineScope {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val isComplete = AtomicBoolean(false)

    val cancelScope: () -> Unit = {
        if (isComplete.compareAndSet(false, true)){
            sseLogger.info("Cancelling SSE scope")
            scope.cancel()
        } else {
            sseLogger.warn("SSE scope already cancelled")
        }
    }

    sseLogger.info("Creating SSE scope")

    emitter.onCompletion(cancelScope)
    emitter.onTimeout(cancelScope)
    emitter.onError{
        sseLogger.warn("Error in SSE emitter", it)
        cancelScope()
    }

    return scope
}
