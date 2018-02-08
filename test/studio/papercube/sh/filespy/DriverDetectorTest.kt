package studio.papercube.sh.filespy

import org.junit.Test
import java.io.File
import java.nio.file.Files

class DriverDetectorTest {
    @Test
    fun testCanReadDirectory() {
        val dir = File("C:\\ProgramData\\INACCESSIBLE")
//        val files = dir.listFiles()
//        files.forEach(::println)
        val dirStream = Files.newDirectoryStream(dir.toPath())
        for (file in dirStream) {
            println(file)
        }
    }
}