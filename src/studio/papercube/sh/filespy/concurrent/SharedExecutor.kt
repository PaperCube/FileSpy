package studio.papercube.sh.filespy.concurrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private var sharedExecutorCount = -1
val sharedExecutor: ExecutorService by lazy initExecutor@ {
    return@initExecutor Executors.newCachedThreadPool { runnable: Runnable? ->
        runnable ?: return@newCachedThreadPool null
        sharedExecutorCount++
        Thread(runnable, "SharedExecutor-Worker-$sharedExecutorCount")
    }
}