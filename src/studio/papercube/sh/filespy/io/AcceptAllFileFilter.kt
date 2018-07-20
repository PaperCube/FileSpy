package studio.papercube.sh.filespy.io

import java.io.File
import java.io.FileFilter

class AcceptAllFileFilter : FileFilter {
    override fun accept(pathname: File?): Boolean {
        return true
    }
}