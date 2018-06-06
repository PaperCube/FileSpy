package studio.papercube.sh.filespy

import java.util.*

internal object EncodingUtil {
    @JvmStatic
    fun ByteArray.encodeHexString(separator: String = ""): String {
        val hexValues = "0123456789ABCDEF"
        return this.joinToString(separator) {
            hexValues[it.toInt() ushr 4 and 0xF].toString() +
                    hexValues[it.toInt() and 0xF].toString()
        }
    }

    @JvmStatic
    fun String.decodeHexString(): ByteArray {
        val strBuilder = StringBuilder(length)
        for (c in this.toUpperCase()) {
            if (c in '0'..'9' || c in 'A'..'F') {
                strBuilder.append(c)
            }
        }

        return strBuilder.toString().decodeHexStringStrict()
    }

    @JvmStatic
    fun String.decodeHexStringStrict(): ByteArray {
        val str = trim()
        if (str.length % 2 != 0) throw IllegalArgumentException("size of string must be even")
        val byteArray = ByteArray(str.length / 2)
        for (i in 0 until str.length / 2) {
            val high: Int = str[i * 2].hexCharToInt()
            val low: Int = str[i * 2 + 1].hexCharToInt()
            byteArray[i] = ((high and 0xF shl 4) or (low and 0xF)).toByte()
        }

        return byteArray.copyOf()
    }

    @JvmStatic
    fun Char.hexCharToInt(): Int {
        return when (this) {
            in '0'..'9' -> this - '0'
            in 'A'..'F' -> this - 'A' + 10
            in 'a'..'f' -> this - 'a' + 10
            else -> throw IllegalArgumentException("Illegal char: $this")
        }
    }

    @JvmStatic
    fun String.decodeBase64String(): ByteArray =
            Base64.getDecoder().decode(this)


    @JvmStatic
    fun ByteArray.encodeBase64String(): String =
            Base64.getEncoder().encodeToString(this)
}