package studio.papercube.sh.filespy

import studio.papercube.library.argparser.Parameter
import java.util.*

private const val LOG_TAG_LAUNCHER = "Launcher"
fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        val threadName = thread.name
        log.e("DefaultUncaughtExceptionHandler", "Thread $threadName crashed.", exception)
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        log.i("The program is requested to terminate")
        log.stop()
    }.apply { name = "Shutdown hook" })

    val parameter = Parameter.resolve(args)
    log.v(LOG_TAG_LAUNCHER, "Starting with args ${Arrays.toString(args)}")
    log.v(LOG_TAG_LAUNCHER, "Version: $VERSION")
    FileSpy.build(parameter)?.start()
}