package studio.papercube.sh.filespy

import java.util.concurrent.ConcurrentHashMap

//Build info
const val VERSION = "0.3.2"


//Internal constants
typealias PropertyMap = ConcurrentHashMap<String, String>

@Deprecated("Deprecated. Use patterns manager instead")
const val DATA_PATH = "C:/ProgramData/Local/FileSpy/"

@Suppress("DEPRECATION")
@Deprecated("Deprecated. Use patterns manager instead")
const val CONFIG_PATH = DATA_PATH + "config.txt"

@Suppress("DEPRECATION")
@Deprecated("Deprecated. Use patterns manager instead")
const val STORAGE_PATH = DATA_PATH + "storage"

@Deprecated("Deprecated. Use patterns manager instead")
//language=RegExp
const val DEFAULT_REGEX = ".*(期([中末])|考试|(月考)|(([文理])科)?.*成绩|名次|排名|学生(信息)?).*\\.(xls(x)?|doc(x)?)"