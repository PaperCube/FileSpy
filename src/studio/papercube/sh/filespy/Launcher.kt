package studio.papercube.sh.filespy

import studio.papercube.library.argparser.Parameter
import java.util.*

private const val LOG_TAG_LAUNCHER = "Launcher"
fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        val threadName = thread.name
        val msg = "Thread $threadName crashed unhandled."
        try {
            log.e("DefaultUncaughtExceptionHandler", msg, exception)
        } catch (e: Throwable) {
            System.err.println("An internal error encountered inside UncaughtExceptionHandler of thread $threadName")
            System.err.println(msg)
            e.addSuppressed(exception)
            e.printStackTrace()
            System.err.println()
        }
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