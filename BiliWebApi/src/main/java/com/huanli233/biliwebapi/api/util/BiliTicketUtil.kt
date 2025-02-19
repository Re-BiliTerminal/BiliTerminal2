package com.huanli233.biliwebapi.api.util

import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.request_param.BiliTicket
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object BiliTicketUtil {
    suspend fun genBiliTicket(api: BiliWebApi): ApiResponse<BiliTicket> {
        val ts = (System.currentTimeMillis() / 1000).toInt()
        val o = hmacSha256("XgwSnGZ1p", "ts$ts")
        return BiliTicket.generate(api, "ec02", o, ts.toString())
    }

    fun genBiliTicketSync(api: BiliWebApi): ApiResponse<BiliTicket> {
        return runBlocking {
            genBiliTicket(api)
        }
    }

    private fun hmacSha256(key: String, message: String): String {
        try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            sha256Hmac.init(secretKey)
            val hashBytes = sha256Hmac.doFinal(message.toByteArray())
            val hexHash = StringBuilder()
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexHash.append('0')
                hexHash.append(hex)
            }
            return hexHash.toString()
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }
}