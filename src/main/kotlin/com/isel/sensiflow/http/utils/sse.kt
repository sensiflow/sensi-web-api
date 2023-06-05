import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.atomic.AtomicBoolean

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
            block(emitter)
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

    emitter.onCompletion {
        if (isComplete.compareAndSet(false, true))
            scope.cancel()
    }

    return scope
}
