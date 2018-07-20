package studio.papercube.sh.filespy.io

import java.io.File
import java.io.FileFilter
import java.io.IOException

class RecursiveCopy(private val from: File,
                    private val to: File,
                    private val fileFilter: FileFilter = AcceptAllFileFilter()) : Runnable {
    var canOverwrite = true
    var bufferSize = DEFAULT_BUFFER_SIZE
    private var isClosed = false
    private val exceptions: MutableList<Throwable> = ArrayList()

    private fun copyFilesIn(srcDir: File, destDir: File) {
        try {
            if (!srcDir.exists()) return
            val files = srcDir.listFiles() ?: throw IOException("Cannot list files in $srcDir")

            var isParentCreated = destDir.exists()

            for (file in files) {
                val fileName = file.name
                if (file.isDirectory) {
                    copyFilesIn(File(srcDir, fileName), File(destDir, fileName))
                } else {
                    if (fileFilter.accept(file)) {
                        if (canOverwrite) cleanUp(destDir)
                        if (!isParentCreated) isParentCreated = destDir.mkdirs()
                        val targetFile = file.copyTo(File(destDir, fileName), canOverwrite, bufferSize)
                        if (targetFile.length() != file.length())
                            exceptions.add(IOException("Source file wasn't copied completely, length of destination file differs."))
                    }
                }
            }
        } catch (e: Exception) {
            exceptions.add(e)
        }
    }

    fun perform() {
        if (isClosed) {
            throw IllegalStateException("Already used")
        }
        isClosed = true
        copyFilesIn(from, to)
    }

    override fun run() {
        perform()
    }

    private fun cleanUp(directory: File) {
        if (directory.isFile && canOverwrite) {
            directory.delete()
        }
    }
}