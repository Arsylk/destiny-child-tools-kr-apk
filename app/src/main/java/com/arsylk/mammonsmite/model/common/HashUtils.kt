package com.arsylk.mammonsmite.model.common


import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashUtils {

    fun ByteArray.toHex(): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(size * 2)
        for (i in indices) {
            val v: Int = get(i).toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun md5(string: String): String? {
        return kotlin.runCatching {
            val md5Digest = MessageDigest.getInstance("md5")
            md5Digest.update(string.toByteArray())
            md5Digest.digest().toHex()
        }.getOrNull()
    }

    fun md5(file: File): String? {
        return kotlin.runCatching {
            val md5Digest = MessageDigest.getInstance("md5")
            FileInputStream(file).buffered().use {
                val buffer = ByteArray(4096)
                var bytes = it.read(buffer)
                while (bytes >= 0) {
                    md5Digest.update(buffer)
                    bytes = it.read(buffer)
                }
            }
            md5Digest.digest().toHex()
        }.getOrNull()
    }
}