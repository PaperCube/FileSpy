package studio.papercube.sh.filespy

import java.io.File

open class FileListDenied(file: File) : FileSystemException(file, null, "listFiles() returned null")