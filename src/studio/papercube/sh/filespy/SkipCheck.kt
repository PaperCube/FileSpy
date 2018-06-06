package studio.papercube.sh.filespy

import java.io.File

class SkipCheck(private val volumeId: ByteArray,
                private val skips: Skips){
    fun shouldSkip(@Suppress("UNUSED_PARAMETER") directory: File): Boolean { //should be completed later
        return skips.skipAll || skips.skipVolumes.any { it.contentEquals(volumeId) }
    }
}