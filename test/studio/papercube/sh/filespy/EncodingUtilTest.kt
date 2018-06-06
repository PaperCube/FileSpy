package studio.papercube.sh.filespy

import org.junit.Assert
import org.junit.Test
import studio.papercube.sh.filespy.EncodingUtil.decodeHexString
import studio.papercube.sh.filespy.EncodingUtil.encodeHexString

class EncodingUtilTest {
    @Test
    fun testHexDecodeEncode() {
        for (i in 1..2147483647 step 997) {
            val byteArray = byteArrayOf(
                    (i and 0xFF).toByte(),
                    (i ushr 8 and 0xFF).toByte(),
                    (i ushr 16 and 0xFF).toByte(),
                    (i ushr 24 and 0xFF).toByte())
            Assert.assertArrayEquals(byteArray, byteArray.encodeHexString().decodeHexString())
        }
    }
}