package studio.papercube.sh.filespy

import studio.papercube.library.argparser.Parameter

fun main(args: Array<String>) {
    val parameter = Parameter.resolve(args)
    FileSpy.build(parameter)?.start()
}