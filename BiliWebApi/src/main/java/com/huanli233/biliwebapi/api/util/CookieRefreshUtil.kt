package com.huanli233.biliwebapi.api.util

import java.security.KeyFactory
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object CookieRefreshUtil {

    fun extractRefreshCsrf(html: String): String? =
        "<div id=\"1-name\">([0-9a-f]+)</div>".toRegex().find(html)?.groups?.get(1)?.value

    // https://github.com/SocialSisterYi/bilibili-API-collect/blob/0f0a8816da270317aa73f7c58dee2e7f2e3a13bf/docs/login/cookie_refresh.md#kotlin
    fun getCorrespondPath(timestamp: Long): String {
        val publicKeyPEM = """
        -----BEGIN PUBLIC KEY-----
        MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLgd2OAkcGVtoE3ThUREbio0Eg
        Uc/prcajMKXvkCKFCWhJYJcLkcM2DKKcSeFpD/j6Boy538YXnR6VhcuUJOhH2x71
        nzPjfdTcqMz7djHum0qSZA0AyCBDABUqCrfNgCiJ00Ra7GmRj+YCK1NJEuewlb40
        JNrRuoEUXpabUzGB8QIDAQAB
        -----END PUBLIC KEY-----
    """.trimIndent()

        val publicKey = KeyFactory.getInstance("RSA").generatePublic(
            X509EncodedKeySpec(android.util.Base64.decode(publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .trim(), android.util.Base64.DEFAULT))
        )

        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding").apply {
            init(Cipher.ENCRYPT_MODE,
                publicKey,
                OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
            )
        }

        return cipher.doFinal("refresh_$timestamp".toByteArray()).joinToString("") { "%02x".format(it) }
    }

}