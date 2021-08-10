import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class Scheduler(val interval: Long, val initialDelay: Long?, val debugMode: Boolean = false) :
    CoroutineScope {
    private val job = Job()

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()


    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        while (isActive) {
            val currentTime = LocalDateTime.now()
            val min = if (debugMode) currentTime.second else currentTime.minute
            SessionPool.periodicFire(min)
            delay(interval)
        }
        println("coroutine done")
    }
}
