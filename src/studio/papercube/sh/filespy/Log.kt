package studio.papercube.sh.filespy

import studio.papercube.library.simplelogger.AsyncSimpleLogger
import studio.papercube.library.simplelogger.Logger
import studio.papercube.library.simplelogger.MulticastWriter
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

val log: Logger by lazy {
    var abnormalLogOutputFileState = false
    val logDir = try {
        File(Environment.environment.getDataStorage(), "log")
    } catch (e: UnsupportedOperatingSystemException) {
        abnormalLogOutputFileState = true
        File(System.getProperty("java.io.tmpdir"), "log")
    }

    val dateTodayString = LocalDate.now().toString()

    val file = File(logDir, "Log_${dateTodayString}_$currentTimeDividedWithHyphens.txt")
    file.createNewFile()
    AsyncSimpleLogger(MulticastWriter(file.writer(), System.out.writer())).apply {
        if(abnormalLogOutputFileState){
            e("Something went wrong when trying to save log to file on disk.")
        }
    }
}

private val currentTimeDividedWithHyphens:String get(){
    val time = LocalTime.now()
    with(time){
        return "%02d-%02d-%02d.%03d".format(hour, minute, second, nano / 1_000_000)
    }
}