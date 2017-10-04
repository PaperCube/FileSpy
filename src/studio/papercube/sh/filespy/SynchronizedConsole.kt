package studio.papercube.sh.filespy

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object SynchronizedConsole{
    private val lock = ReentrantLock()

    fun log(any: Any?) {
        lock.withLock {
            println(any)
        }
    }
}