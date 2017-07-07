package studio.papercube.sh.filespy

import java.io.File

class DriverDetector @JvmOverloads constructor(private val interval: Long = 1000L,
                                               val insertListener: ((File) -> Unit)? = null,
                                               val removeListener: ((File) -> Unit)? = null) {
    private var lastCheck: List<File> = File.listRoots().toList()


    private val timer = SmartTimer(interval) {
        val rootFolders = File.listRoots().toList()

        rootFolders.filter { it !in lastCheck }.forEach { insertListener?.invoke(it) } //newly inserted

        lastCheck.filter { it !in rootFolders }.forEach { removeListener?.invoke(it) } //newly removed

        lastCheck = rootFolders
    }

    fun start() = timer.start()
    fun cancel() = timer.cancel()

}