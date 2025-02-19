package com.huanli233.biliwebapi.api.util

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Locale
import java.util.Random

object RequestParamUtil {
    private val MP = arrayOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F", "10"
    )
    private val PCK = intArrayOf(8, 4, 4, 4, 12)
    private const val CHARSET = "0123456789ABCDEF"

    fun genBlsid(): String {
        val random = SecureRandom()
        val sb = StringBuilder(8)
        for (i in 0..7) {
            sb.append(CHARSET[random.nextInt(CHARSET.length)])
        }
        val randomString = sb.toString()
        val currentTimeMillis = System.currentTimeMillis()
        return randomString + "_" + java.lang.Long.toHexString(currentTimeMillis)
            .uppercase(Locale.getDefault())
    }

    fun genUuidInfoc(): String {
        val t = System.currentTimeMillis() % 100000
        val sb = StringBuilder()
        val random = Random()
        for (len in PCK) {
            for (i in 0 until len) {
                sb.append(MP[random.nextInt(16)])
            }
            sb.append("-")
        }
        sb.deleteCharAt(sb.length - 1)
        sb.append(String.format(Locale.getDefault(), "%05d", t)).append("infoc")
        return sb.toString()
    }

    fun genBnut(): String {
        val timestampInSeconds = System.currentTimeMillis() / 1000
        return timestampInSeconds.toString()
    }

    private val MOD: BigInteger = BigInteger.ONE.shiftLeft(64)
    private val C1 = BigInteger("87C37B91114253D5", 16)
    private val C2 = BigInteger("4CF5AD432745937F", 16)
    private val C3: BigInteger = BigInteger.valueOf(0x52DCE729L)
    private val C4: BigInteger = BigInteger.valueOf(0x38495AB5L)
    private const val R1 = 27
    private const val R2 = 31
    private const val R3 = 33
    private const val M = 5

    fun genBuvidFp(key: String, seed: Long): String {
        val source: InputStream = ByteArrayInputStream(key.toByteArray(charset("ascii")))
        val m = murmur3_x64_128(source, BigInteger.valueOf(seed))
        return String.format("%016x%016x", m.mod(MOD), m.shiftRight(64).mod(MOD))
    }

    private fun rotateLeft(x: BigInteger, k: Int): BigInteger {
        return x.shiftLeft(k).or(x.shiftRight(64 - k)).mod(MOD)
    }

    @Throws(IOException::class)
    private fun murmur3_x64_128(source: InputStream, seed: BigInteger): BigInteger {
        var h1 = seed
        var h2 = seed
        var processed: Long = 0
        val buffer = ByteArray(16)
        while (true) {
            val bytesRead = source.read(buffer)
            processed += bytesRead.toLong()
            if (bytesRead == 16) {
                val k1 = ByteBuffer.wrap(buffer, 0, 8).getLong()
                val k2 = ByteBuffer.wrap(buffer, 8, 8).getLong()
                h1 = h1.xor(
                    rotateLeft(BigInteger.valueOf(k1).multiply(C1).mod(MOD), R2).multiply(
                        C2
                    ).mod(MOD)
                )
                h1 = (rotateLeft(h1, R1).add(h2).multiply(BigInteger.valueOf(M.toLong())).add(
                    C3
                )).mod(MOD)
                h2 = h2.xor(
                    rotateLeft(BigInteger.valueOf(k2).multiply(C2).mod(MOD), R3).multiply(
                        C1
                    ).mod(MOD)
                )
                h2 = (rotateLeft(h2, R2).add(h1).multiply(BigInteger.valueOf(M.toLong())).add(
                    C4
                )).mod(MOD)
            } else if (bytesRead == -1) {
                h1 = h1.xor(BigInteger.valueOf(processed))
                h2 = h2.xor(BigInteger.valueOf(processed))
                h1 = h1.add(h2).mod(MOD)
                h2 = h2.add(h1).mod(MOD)
                h1 = fmix64(h1)
                h2 = fmix64(h2)
                h1 = h1.add(h2).mod(MOD)
                h2 = h2.add(h1).mod(MOD)
                return h2.shiftLeft(64).or(h1)
            } else {
                var k1: Long = 0
                var k2: Long = 0
                val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                if (bytesRead >= 15) {
                    k2 = k2 xor (byteBuffer[14].toLong() shl 48)
                }
                if (bytesRead >= 14) {
                    k2 = k2 xor (byteBuffer[13].toLong() shl 40)
                }
                if (bytesRead >= 13) {
                    k2 = k2 xor (byteBuffer[12].toLong() shl 32)
                }
                if (bytesRead >= 12) {
                    k2 = k2 xor (byteBuffer[11].toLong() shl 24)
                }
                if (bytesRead >= 11) {
                    k2 = k2 xor (byteBuffer[10].toLong() shl 16)
                }
                if (bytesRead >= 10) {
                    k2 = k2 xor (byteBuffer[9].toLong() shl 8)
                }
                if (bytesRead >= 9) {
                    k2 = k2 xor byteBuffer[8].toLong()
                    h2 = h2.xor(
                        rotateLeft(BigInteger.valueOf(k2).multiply(C2).mod(MOD), R3).multiply(
                            C1
                        ).mod(MOD)
                    )
                }
                if (bytesRead >= 8) {
                    k1 = k1 xor (byteBuffer[7].toLong() shl 56)
                }
                if (bytesRead >= 7) {
                    k1 = k1 xor (byteBuffer[6].toLong() shl 48)
                }
                if (bytesRead >= 6) {
                    k1 = k1 xor (byteBuffer[5].toLong() shl 40)
                }
                if (bytesRead >= 5) {
                    k1 = k1 xor (byteBuffer[4].toLong() shl 32)
                }
                if (bytesRead >= 4) {
                    k1 = k1 xor (byteBuffer[3].toLong() shl 24)
                }
                if (bytesRead >= 3) {
                    k1 = k1 xor (byteBuffer[2].toLong() shl 16)
                }
                if (bytesRead >= 2) {
                    k1 = k1 xor (byteBuffer[1].toLong() shl 8)
                }
                if (bytesRead >= 1) {
                    k1 = k1 xor byteBuffer[0].toLong()
                    h1 = h1.xor(rotateLeft(BigInteger.valueOf(k1).multiply(C1).mod(MOD), R2))
                }
            }
        }
    }

    private fun fmix64(k: BigInteger): BigInteger {
        var result = k
        val c1 = BigInteger("FF51AFD7ED558CCD", 16)
        val c2 = BigInteger("C4CEB9FE1A85EC53", 16)
        val r = 33
        result = result.xor(result.shiftRight(r)).multiply(c1).mod(MOD)
        result = result.xor(result.shiftRight(r)).multiply(c2).mod(MOD)
        result = result.xor(result.shiftRight(r)).mod(MOD)
        return k
    }
}