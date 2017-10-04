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
        return if (fileList.isEmpty()) {
            if (directory.isDirectory) addFiles(directory, fileList)
            else fileList.add(directory)

            fileList
        } else fileList
    }

    val fileTreeString get() = fileTreeBuilder.toString()

    private fun addFiles(dir: File, toList: MutableList<File>, depth: Int = 0) {
        try {
            dir.listFiles()
                    .partition(File::isDirectory)
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
        }
    }

    class FileTreeBuilder {
        private val stringBuilder = StringBuilder()
        var treeItemPrefix = " | "

        fun putFile(file: File, depth: Int) {
            stringBuilder.appendRepeatedly(treeItemPrefix, depth).append(file.name)
            if(file.isDirectory) stringBuilder.append(" : [DIR]")
            stringBuilder.appendln()
        }

        private fun StringBuilder.appendRepeatedly(content: String, count: Int): StringBuilder = apply {
            for (i in 1..count) {
                append(content)
            }
        }

        override fun toString(): String {
            return stringBuilder.toString()
        }
    }
}