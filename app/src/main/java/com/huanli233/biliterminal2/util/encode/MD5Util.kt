package com.huanli233.biliterminal2.util.encode

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object MD5Util {
    fun md5(
        plainText: String
    ): String {
        val encoded: ByteArray
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
            encoded = md.digest()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        val md5code = StringBuilder(BigInteger(1, encoded).toString(16))
        for (i in 0..<32 - md5code.length) {
            md5code.insert(0, "0")
        }
        return md5code.toString()
    }
}