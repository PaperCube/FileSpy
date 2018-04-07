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

    private val loadedValue = SharedLoadableValue<List<NamePattern>> { load() }

    private fun load(): List<NamePattern> {
        var patterns: List<NamePattern> = Collections.emptyList()

        try {
            if (!patternFile.exists()) patternFile.createNewFile()
            val reader = patternFile.bufferedReader()
            val patternsString = reader.readLines()
            reader.close()

            patterns = patternsString.filter { !it.isBlank() }.mapNotNull {
                try {
                    NamePattern.ofRegexString(it.trim())
                } catch (e: PatternSyntaxException) {
                    null
                }
            }


            if (patterns.isEmpty()) {
                patterns = Collections.singletonList(NamePattern.ofRegexString(DEFAULT_PATTERN))
                writeDefault(patternsString)
            }
        } catch (e: Exception) {
            log.e(tag = LOG_TAG, msg = "Failed to load patterns.", e = e)
        }

        log.v(tag = LOG_TAG, msg = "Done loading patterns. Patterns count = ${patterns.size}")

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

    open fun readPatterns(): List<NamePattern> {
        return loadedValue.get()
    }
}

abstract class NamePattern {
    companion object {
        fun ofRegex(regex: Regex): NamePattern = object : NamePattern() {
            override fun matchesWithName(name: String): Boolean {
                return regex.matches(name)
            }
        }

        /**
         * @throws PatternSyntaxException if given [regexString] isn't a valid regular expression.
         */
        fun ofRegexString(regexString: String) = ofRegex(regexString.toRegex())

        fun ofPartialString(partialString: String): NamePattern = object : NamePattern() {
            override fun matchesWithName(name: String): Boolean {
                return partialString in name
            }
        }
    }

    abstract fun matchesWithName(name: String): Boolean
}