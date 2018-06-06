package studio.papercube.sh.filespy

data class Skips(
        var skipAll: Boolean = false,
        var doNotSkip: Boolean = false,
        var skipVolumes: List<ByteArray> = ArrayList(),
        var skipKeys: List<ByteArray> = ArrayList()
)