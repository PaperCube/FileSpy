package studio.papercube.sh.filespy.test

import org.junit.Test
import studio.papercube.sh.filespy.FileWalker
import java.io.File

class FileTreeBuilderTestKtTest {
    @Test
    fun testGenerate(){
        val walker = FileWalker(File("G:"))
        val files = walker.walk()
        val fileTree = walker.fileTreeString
        println(fileTree)
    }
}