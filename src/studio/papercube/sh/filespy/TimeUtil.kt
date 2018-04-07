package studio.papercube.sh.filespy

import java.time.LocalTime

object TimeUtil{
    private val currentTimeWith3Digits:String get(){
        val time = LocalTime.now()
        with(time){
            return "%02d:%02d:%02d.%03d".format(hour, minute, second, nano / 1_000_000)
        }
    }
}