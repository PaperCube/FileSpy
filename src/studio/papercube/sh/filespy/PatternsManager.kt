package studio.papercube.sh.filespy

import studio.papercube.sh.filespy.concurrent.SharedLoadableValue
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import java.util.regex.PatternSyntaxException

open class PatternsManager(private val patternFile: File) {
    companion object {
        private const val LOG_TAG = "PatternsManager"
        //language=RegExp
        const val DEFAULT_PATTERN = ".*(期([中末])|考试|(月考)|(([文理])科)?.*成绩|名次|分班|排名|学生(信息)?).*\\.(xls(x)?|doc(x)?)"
        val default by lazy {
            log.i(LOG_TAG, "Initializing patterns manager")
            PatternsManager(Environment.environment.getPatternsStoreFile())
        }
    }

    private val loadedValue = SharedLoadableValue<List<FilePattern>> { load() }

    private fun load(): List<FilePattern> {
        var patterns: List<FilePattern> = Collections.emptyList()

        try {
            if (!patternFile.exists()) patternFile.createNewFile()
            val reader = patternFile.bufferedReader()
            val patternsString = reader.readLines()
            reader.close()

            patterns = patternsString.filter { !it.isBlank() }.mapNotNull {
                try {
                    FilePattern.ofRegexString(it.trim())
                } catch (e: PatternSyntaxException) {
                    null
                }
            }


            if (patterns.isEmpty()) {
                patterns = Collections.singletonList(FilePattern.ofRegexString(DEFAULT_PATTERN))
                writeDefault(patternsString)
            }
        } catch (e: Exception) {
            log.e(tag = LOG_TAG, msg = "Failed to load patterns.", e = e)
        }

        log.v(tag = LOG_TAG, msg = "Done loading patterns. Patterns count = ${patterns.size}")
//        val patternTypes = patterns.joinToString("\n")
//        log.v(LOG_TAG, "Pattern type: \n$patternTypes")

        return patterns
    }

    /**
     * @throws IOException if failed to write default patterns into a file
     */
    private fun writeDefault(unresolvedLines: List<String>) {
        val writer = PrintWriter(patternFile.bufferedWriter())
        writer.println(unresolvedLines.joinToString(separator = "\n") { it })
        writer.println(DEFAULT_PATTERN)
        writer.close()
    }

    open fun readPatterns(): List<FilePattern> {
        return loadedValue.get()
    }
}

abstract class FilePattern {
    companion object {
        /**
         * @throws PatternSyntaxException if given [regexString] isn't a valid regular expression.
         */
        fun ofRegexString(regexString: String): FilePattern {
            val regexStringTrimmed = regexString.trim()
            when {
                regexStringTrimmed.endsWith('/') -> { // represents a directory
                    val regex = regexString.removeSuffix("/").regexIgnoringCase()
                    return object : FilePattern() {
                        override fun matchesWithFile(file: File): Boolean {
                            return file.absoluteFile.invariantSeparatorsPath.contains(regex) ||
                                    (file.isDirectory && (file.name + '/').matches(regex))
                        }
                    }
                }
                regexStringTrimmed.contains('/') -> { //represents a file in a specified directory
                    val regex = regexString.regexIgnoringCase()
                    return object : FilePattern() {
                        override fun matchesWithFile(file: File): Boolean {
                            return file.absoluteFile.invariantSeparatorsPath.contains(regex)
                        }
                    }
                }
                else -> {
                    val regex = regexString.regexIgnoringCase()
                    return object : FilePattern() {
                        override fun matchesWithFile(file: File): Boolean {
                            return file.name.matches(regex)
                        }
                    }
                }
            }
        }

        @JvmStatic
        private fun String.regexIgnoringCase(): Regex {
            return Regex(this, RegexOption.IGNORE_CASE)
        }

    }

    @Deprecated("This method isn't designed to handle directories. Use matchesWithFile instead", ReplaceWith(""))
    open fun matchesWithName(name: String): Boolean {
        return matchesWithFile(File(name))
    }

    abstract fun matchesWithFile(file: File): Boolean
}