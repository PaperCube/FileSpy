package studio.papercube.sh.filespy

import studio.papercube.sh.filespy.EncodingUtil.decodeBase64String
import studio.papercube.sh.filespy.EncodingUtil.decodeHexString
import studio.papercube.sh.filespy.EncodingUtil.encodeBase64String
import studio.papercube.sh.filespy.concurrent.SharedLoadableValue
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList

object SkipManagement {
    private val md5Digest = MessageDigest.getInstance("MD5")
    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()
    private val skipTemplateBytes = "#FSSKIP#".toByteArray(Charsets.UTF_8)

    private val skipLoader: SharedLoadableValue<Skips> = SharedSkipsLoader()

    /**
     * Incompleted
     */
    private

    fun buildChecksumWith(key: ByteArray, volumeId: ByteArray): ByteArray {
        val keyString = key.encodeBase64String()
        val volumeIdString = volumeId.encodeBase64String()
        return md5Digest.digest((keyString + volumeIdString).toByteArray())
    }

    fun getSkipCheck(volumeId: ByteArray): SkipCheck {
        return SkipCheck(volumeId, skipLoader.get())
    }

    private class SharedSkipsLoader : SharedLoadableValue<Skips>() {
        private var currentSkips = Skips()
        override fun loadValue(): Skips {
            log.i("Refreshing skip-cfg")
            val file = Environment.environment.getSkipConfigFile()
            if (!file.exists()) file.createNewFile()
            val lines = file.readLines()
            resolve(lines)
            return currentSkips
        }

        private fun resolve(commands: List<String>) {
            var skipAll = false
            var skipNone = false
            val skipVolumes = ArrayList<ByteArray>()
            for (command in commands) {
                val list = command.split(' ', limit = 0)
                val cmdName = list.getOrNull(0)?.toLowerCase()
                val cmdArg = list.getOrNull(1)
                when (cmdName) {
                    "skip-all" -> skipAll = true
                    "skip-none" -> skipNone = true
                    "skip-volume" -> tryAddEncodedByteArray(skipVolumes, cmdArg, cmdName, "hex")
                }
            }

            currentSkips = Skips(
                    skipAll = skipAll,
                    doNotSkip = skipNone,
                    skipVolumes = skipVolumes
            )
        }

        private fun tryAddEncodedByteArray(list: MutableList<ByteArray>,
                                           cmdArg: String?,
                                           cmdName: String,
                                           cmdArgType: String) {
            try {
                if (cmdArg != null) {
                    val decoded = when (cmdArgType) {
                        "base64" -> cmdArg.decodeBase64String()
                        "hex" -> cmdArg.decodeHexString()
                        else -> throw IllegalArgumentException("Unsupported command argument type: $cmdArgType")
                    }
                    list.add(decoded)
                } else {
                    log.e("Cannot resolve $cmdName: No arguments supplied but 1 required")
                }
            } catch (e: Exception) {
                log.e("Cannot resolve $cmdName: unable to parse argument $cmdArg as $cmdArgType: $e")
            }
        }

    }
}