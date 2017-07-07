package studio.papercube.sh.filespy

import java.util.concurrent.ConcurrentHashMap

//Build info
const val VERSION = "0.2"


//Internal constants
typealias PropertyMap = ConcurrentHashMap<String,String>
const val DATA_PATH = "C:/ProgramData/Local/FileSpy/"
const val CONFIG_PATH = DATA_PATH + "config.txt"
const val STORAGE_PATH = DATA_PATH + "storage"

//language=RegExp
const val DEFAULT_REGEX = ".*(期([中末])|考试|(月考)|(([文理])科)?.*成绩|名次|排名|学生(信息)?).*\\.(xls(x)?|doc(x)?)"