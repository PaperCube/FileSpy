package studio.papercube.sh.filespy

import java.io.File
import java.io.Writer
import java.nio.file.Path

/**
 * A class that has several methods to write file tree in xml style conveniently.
 * Note that this class is NOT thread-safe.
 */
class XMLStyleFileTreeWriter(private val writer: Writer) : XMLPrintWriter(writer) {
//    companion object {
//        @JvmStatic val EOL = System.getProperty("line.separator")
//    }

    fun beginDirectory(dir: File) {
        begin("directory", "name" to dir.name)
    }

    fun endDirectory(dir: File) {
        end("directory")
    }

    fun putFile(file: File) {
        begin("file", "name" to file.name)
        end("file")
    }

    fun putException(e: Throwable) {
        begin("exception", "type" to e)
        end("exception")
    }

    fun putSymlink(linkTo: Path) {
        begin("symlink", "to" to linkTo)
    }

    override fun write(cbuf: CharArray?, off: Int, len: Int) {
        writer.write(cbuf, off, len)
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }
}