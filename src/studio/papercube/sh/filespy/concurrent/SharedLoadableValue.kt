package studio.papercube.sh.filespy.concurrent

import studio.papercube.sh.filespy.SynchronizedConsole
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class SharedLoadableValue<R>(private val callable: (() -> R)?) {

    constructor() : this(null)

    private val lock = ReentrantLock()
    private val returnCondition = lock.newCondition()

    private var result: R? = null

    private var isLoading = false

    protected open fun loadValue(): R {
        return callable!!()
    }

    fun get(): R {
        var valueGot = false
        var result:R? = null
        lock.withLock {
            if(isLoading){
//                SynchronizedConsole.log("${Thread.currentThread().name} is waiting for a result")
                returnCondition.await()
                valueGot = true
                result = this.result
            } else {
                isLoading = true
            }
        }

        if(!valueGot){
//            SynchronizedConsole.log("${Thread.currentThread().name} is calculating a result itself.")
            result = loadValue()
            valueGot = true

            lock.withLock {
                this.result = result
                isLoading = false
                returnCondition.signalAll()
            }
        }

        @Suppress("UNCHECKED_CAST")
        return result as R
    }
}