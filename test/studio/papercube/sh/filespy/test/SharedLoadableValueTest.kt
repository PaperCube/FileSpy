package studio.papercube.sh.filespy.test

import org.junit.Test
import studio.papercube.sh.filespy.SynchronizedConsole
import studio.papercube.sh.filespy.concurrent.SharedLoadableValue
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SharedLoadableValueTest {
    private val random = Random()
    private val randomLock = ReentrantLock()

    @Test
    fun get() {
        val atomicInteger = AtomicInteger(0)
        val sharedLoadableValue = SharedLoadableValue {
            val before = atomicInteger.get()
            log("Calculating $before")
            Thread.sleep(nextRandomLong(2000, 3000))
            log("Done calculation")
            atomicInteger.getAndIncrement()
        }

        val threads = (1..10).map { i->
            Thread {
                while (true) {
                    val currentThreadName = Thread.currentThread().name
                    Thread.sleep(1000)
                    val result = sharedLoadableValue.get()
                    log("$currentThreadName got result: $result")
                }
            }.apply {
                name = "Test-thread-$i"
                start()
            }
        }.forEach { it.join() }
    }

    inline fun <reified T> log(any: T) {
        SynchronizedConsole.log(any)
    }

    private fun nextRandomLong(lowerBound: Int = 0, upperBound: Int): Long {
        var value: Int = -1
        randomLock.withLock {
            value = lowerBound + random.nextInt(upperBound - lowerBound)
        }
        return value.toLong()
    }
}

//inline fun <reified T> go() {
//
//}