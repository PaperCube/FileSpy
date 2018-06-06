package studio.papercube.sh.filespy.test

import org.junit.Test
import studio.papercube.sh.filespy.concurrent.sharedExecutor
import studio.papercube.sh.filespy.log

class SharedExecutorTest{
    @Test
    fun testExceptionHandling(){
        val future = sharedExecutor.submit {
            Thread.currentThread().setUncaughtExceptionHandler { _, e ->
                log.e(e = e, msg = "Exception caught by uncaught exception handler")
            }
            log.i("Preparing to throw an exception.")
            throw RuntimeException()
        }
        try {
            future.get()
        } catch (e: Exception) {
            //ignore
        }
        log.i("Done")
        log.stop()
    }
}