package studio.papercube.sh.filespy

import java.io.File

abstract class Environment {

    abstract fun getDataStorage(): File
    abstract fun volumeSupplier(): () -> List<File>

    open fun getConfigFile(): File {
        return File(getDataStorage(), "config.txt")
    }

    open fun getPatternsStoreFile(): File {
        return File(getDataStorage(), "patterns.txt")
    }

    open fun getPatternsManager() = PatternsManager.default

    companion object {
        val environment: Environment by lazy {
            val osName: String = System.getProperty("os.name") ?: "null"
            when {
                "Windows" in osName -> WindowsEnvironment()
                else -> throw UnsupportedOperatingSystemException("Operating system $osName is not supported")
            }
        }
    }
}


class UnsupportedOperatingSystemException(msg: String) : RuntimeException(msg)

open class WindowsEnvironment : Environment() {
    private val dataStorage = File("C:\\ProgramData\\Local\\FileSpy")

    private fun ensureDataStorageExists() {
        if (!dataStorage.exists()) {
            dataStorage.mkdirs()
        }
    }

    override fun getDataStorage(): File {
        ensureDataStorageExists()
        return dataStorage
    }

    override fun volumeSupplier(): () -> List<File> = {
        File.listRoots().toList()
    }

}