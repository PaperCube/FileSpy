package studio.papercube.sh.filespy

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FileWalkerTest {
    @Test
    fun testSymlink() {
        val exampleFiles = arrayOf(
                "G:\\DirectorySymbolicLink",
                "G:\\FileSymlink",
                "G:\\DirectoryConjunction"
        )

        val result = BooleanArray(exampleFiles.size){ i->
            val path = File(exampleFiles[i]).toPath()
            println(Files.readSymbolicLink(path))
            Files.isSymbolicLink(path)
        }

        assertArrayEquals(BooleanArray(exampleFiles.size) { true },
                result
        )
    }
}