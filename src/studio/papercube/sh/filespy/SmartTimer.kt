package studio.papercube.sh.filespy

import java.lang.IllegalStateException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SmartTimer(interval: Long, private val task: SmartTimer.() -> Unit) {
    val lock = ReentrantLock()

    var interval = interval
        set(value) {
            lock.withLock { field = interval }
        }

    val thread = Thread {
        while (true) {
            try {
                if (isInterrupted) break
                val time = timeOf { task() }
                val fixedWaitingTime = interval - time
                if (fixedWaitingTime > 0) Thread.sleep(fixedWaitingTime)
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    init {

    }

    @JvmOverloads
    fun start(delay: Long = 0) {
        if (thread.isAlive) throw IllegalStateException("The driverDetector has already been started and not yet died.")
        if (delay < 0) throw IllegalArgumentException("Delay is expected positive but $delay received.")

        if (delay > 0) Thread.sleep(delay)

        thread.start()
    }

    fun cancel() {
        lock.withLock { thread.interrupt() }
    }

    val isInterrupted: Boolean get() = thread.isInterrupted

    var isDaemon
        get() = thread.isDaemon
        set(value) {
            thread.isDaemon = value
        }

    private inline fun timeOf(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }

}