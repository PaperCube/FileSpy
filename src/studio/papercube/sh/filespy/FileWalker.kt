package studio.papercube.sh.filespy

import java.io.File

class FileWalker(private val directory: File) {
    private val fileList = ArrayList<File>()
    private val exceptions = ArrayList<Throwable>()

    init {

    }

    fun walk(): List<File> {
        if (fileList.isEmpty()) {
            if (directory.isDirectory) addFiles(directory, fileList)
            else fileList.add(directory)

            return fileList
        } else return fileList
    }

    private fun addFiles(dir: File, list: MutableList<File>) {
        try {
            dir.listFiles()
                    .partition(File::isDirectory)
                    .let { (directories, files) ->
                        list.addAll(files)
                        directories.forEach { addFiles(it, list) }
                    }
        } catch (e: Throwable) {
            exceptions.add(e)
        }
    }
}