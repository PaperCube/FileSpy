package studio.papercube.sh.filespy

import java.io.PrintWriter
import java.io.Writer
import java.util.*

/**
 * A general purpose XML writer, which has two basic operations, BEGIN and END.
 * Operation BEGIN begins a tag and operation END ends a tag. When BEGIN is called,
 * it doesn't write a complete tag but leave the ending > or /> for a next BEGIN call
 * or END call. Both BEGIN and END call deal with and solve incomplete tags, but operation
 * BEGIN creates incomplete tag state, and operation END finalizes it.
 */
open class XMLPrintWriter(writer: Writer) : PrintWriter(writer) {
    private var tagIncomplete = false
    private var empty = true
    private val tagStack = ArrayDeque<String>()

    var encoding = "UTF-8"
    var indents = "\t"

    fun begin(tag: String, vararg prop: Pair<String, Any?>) {
        begin(tag, prop.joinToString(" ") { "${it.first}=\"${it.second}\"" })
    }

    open fun begin(tag: String, prop: String) {
        putHeader()
        if (tagIncomplete) {
            println(">")
        }
        writeIndents()
        tagStack.push(tag)
        print("<$tag $prop")
        tagIncomplete = true
    }

    open fun end(tagName: String? = null) {
        val tag = tagName ?: tagStack.first
        if (tag != tagStack.pop()) {
            throw IllegalArgumentException("$tagName doesn't match item in stack $tag")
        }

        if (tagIncomplete) {
            println("/>")
        } else {
            writeIndents()
            println("</$tag>")
        }

        tagIncomplete = false
    }

    protected fun putHeader() {
        if (empty) {
            println("<?xml version=\"1.0\" encoding=\"$encoding\"?>")
            empty = false
        }
    }

    private fun writeIndents() {
        for (i in 0 until tagStack.size) {
            print(indents)
        }
    }
}