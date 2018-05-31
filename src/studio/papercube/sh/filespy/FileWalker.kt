package studio.papercube.sh.filespy

import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path

class FileWalker(private val directory: File) {
    private val fileList = ArrayList<File>()
    private val exceptions = ArrayList<Throwable>()
    private val stringWriter = StringWriter()
    private val xmlFileTreeWriter = XMLStyleFileTreeWriter(stringWriter)

    var takeDownFileTree = true

    init {

    }

    fun walk(): List<File> {
        if (fileList.isEmpty()) {
            if (directory.isDirectory) addFiles(directory, fileList)
            else fileList.add(directory)
        }
        return fileList
    }

    val fileTreeString get() = stringWriter.toString()

    private fun addFiles(dir: File, toList: MutableList<File>, depth: Int = 0) {
        try {
            val path = dir.toPath()
//            log.i("Adding files in $dir")
            if (Files.isSymbolicLink(path)) {
//                log.w("Detected symlink $path")
                writeFileTree { putSymlink(Files.readSymbolicLink(path)) }
                return
            }

            val files = dir.listFiles() ?: throw FileListDenied(dir)
            files.partition(File::isDirectory)
                    .let { (directories, files) ->
                        toList.addAll(files)

                        writeFileTree {
                            for (file in files) {
                                putFile(file)
                            }
                        }

                        for (directory in directories) {
                            writeFileTree { beginDirectory(directory) }
                            addFiles(directory, toList, depth + 1)
                            writeFileTree { endDirectory(directory) }
                        }
                    }
        } catch (e: Throwable) {
            exceptions.add(e)
            writeFileTree { putException(e) }
        }
    }

    fun getExceptions(): List<Throwable> {
        return exceptions
    }

    private inline fun writeFileTree(action: XMLStyleFileTreeWriter.() -> Unit) {
        if (takeDownFileTree) {
            xmlFileTreeWriter.action()
        }
    }

    @Suppress("unused")
    @Deprecated("Deprecated. Use XMLStyleFileTreeWriter instead")
    class FileTreeBuilder {
        private val stringBuilder = StringBuilder()
        var treeItemPrefix = " | "

        fun putPadding(depth: Int) = apply {
            stringBuilder.appendRepeatedly(treeItemPrefix, depth)
        }

        fun putFile(file: File, depth: Int) {
            putPadding(depth)
            stringBuilder.append(file.name)
            if (file.isDirectory) stringBuilder.append(" : [DIR]")
            stringBuilder.appendln()
        }

        private fun StringBuilder.appendRepeatedly(content: String, count: Int): StringBuilder = apply {
            for (i in 1..count) {
                append(content)
            }
        }

        fun appendln(str: String) = apply {
            stringBuilder.appendln("*** $str ***")
        }

        fun putException(e: Throwable, depth: Int) = apply {
            putPadding(depth)
            appendln(e.toString())
        }

        fun putSymbolicIndication(to: Path, depth: Int) = apply {
            putPadding(depth)
            appendln("symlink => $to")
        }

        override fun toString(): String {
            return stringBuilder.toString()
        }
    }
}