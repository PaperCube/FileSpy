package studio.papercube.sh.filespy

import studio.papercube.library.simplelogger.AsyncSimpleLogger
import studio.papercube.library.simplelogger.Logger
import studio.papercube.library.simplelogger.MulticastWriter
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

val log: Logger by lazy {
    val logDir = File(Environment.environment.getDataStorage(), "log")
    val todayDir = File(logDir, LocalDate.now().toString())
    todayDir.mkdirs()
    val file = File(todayDir, "Log_$currentTimeDividedByHyphens.txt")
    file.createNewFile()
    AsyncSimpleLogger(MulticastWriter(file.writer(), System.out.writer()))
}

private val currentTimeDividedByHyphens:String get(){
    val time = LocalTime.now()
    with(time){
        return "%02d-%02d-%02d.%03d".format(hour, minute, second, nano / 1_000_000)
    }
}