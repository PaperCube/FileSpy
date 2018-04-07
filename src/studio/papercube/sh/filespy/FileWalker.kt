package studio.papercube.sh.filespy

import java.io.File

class FileWalker(private val directory: File) {
    private val fileList = ArrayList<File>()
    private val exceptions = ArrayList<Throwable>()
    private val fileTreeBuilder = FileTreeBuilder()

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

    val fileTreeString get() = fileTreeBuilder.toString()

    private fun addFiles(dir: File, toList: MutableList<File>, depth: Int = 0) {
        try {
            val files = dir.listFiles() ?: throw IllegalStateException("Cannot list files in ${dir.absolutePath}")
            files.partition(File::isDirectory)
                    .let { (directories, files) ->
                        toList.addAll(files)

                        if (takeDownFileTree) {
                            for (file in files) {
                                fileTreeBuilder.putFile(file, depth)
                            }
                        }

                        for (directory in directories) {
                            if (takeDownFileTree) fileTreeBuilder.putFile(directory, depth)
                            addFiles(directory, toList, depth + 1)
                        }
                    }
        } catch (e: Throwable) {
            exceptions.add(e)
            if (takeDownFileTree) fileTreeBuilder.putException(e, depth)
        }
    }

    fun getExceptions(): List<Throwable> {
        return exceptions
    }

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
            stringBuilder.appendln("*$str")
        }

        fun putException(e: Throwable, depth: Int) = apply {
            putPadding(depth)
            appendln(e.toString())
        }

        override fun toString(): String {
            return stringBuilder.toString()
        }
    }
}