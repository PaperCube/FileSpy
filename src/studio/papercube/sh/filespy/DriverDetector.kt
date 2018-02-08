package studio.papercube.sh.filespy

import java.io.File
import java.nio.file.Files

class DriverDetector @JvmOverloads constructor(private val interval: Long = 1000L,
                                               val insertListener: ((File) -> Unit)? = null,
                                               val removeListener: ((File) -> Unit)? = null,
                                               val gainAccessListener: ((File) -> Unit)? = null) {
    private var lastCheck: List<File>
    private var lastAccessibleDirectories: List<File>

    init {
        lastCheck = File.listRoots().toList()
        lastAccessibleDirectories = lastCheck.filter(this::canReadDirectory)
    }

    private val timer = SmartTimer(interval) {
        val rootFolders = File.listRoots().toList()
        val accessibleRootFolders = rootFolders.filter(this@DriverDetector::canReadDirectory)

        rootFolders.filter { it !in lastCheck }.forEach { insertListener?.invoke(it) } //newly inserted
        lastCheck.filter { it !in rootFolders }.forEach { removeListener?.invoke(it) } //newly removed

        accessibleRootFolders.filter { it !in lastAccessibleDirectories }.forEach { gainAccessListener?.invoke(it) }

        lastCheck = rootFolders
        lastAccessibleDirectories = accessibleRootFolders
    }

    fun start() = timer.start()
    fun cancel() = timer.cancel()

    private fun canReadDirectory(f: File): Boolean {
        try {
            val dirStream = Files.newDirectoryStream(f.toPath())
            try {
                dirStream.close()
            } catch (e: Exception) {
                //ignored
            }
        } catch (e: Exception) {
            return false
        }

        return true
    }
}