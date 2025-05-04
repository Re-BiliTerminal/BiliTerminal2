package com.huanli233.biliterminal2.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

private const val CRYPTO_PREFS_NAME = "crypto_prefs"
private const val PREF_ENCRYPTION_KEY = "encryption_key"

class CryptoHelper(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(CRYPTO_PREFS_NAME, Context.MODE_PRIVATE)

    private val cipherTransformation = "AES/CBC/PKCS7Padding"
    private val keyAlgorithm = "AES"
    private val keySize = 256 // AES-256

    private var secretKey: SecretKey? = null

    init {
        secretKey = getOrCreateKey()
    }

    private fun getOrCreateKey(): SecretKey {
        val base64Key = preferences.getString(PREF_ENCRYPTION_KEY, null)
        return if (base64Key == null) {
            generateAndSaveNewKey()
        } else {
            decodeKey(base64Key)
        }
    }

    private fun generateAndSaveNewKey(): SecretKey {
        try {
            val keyGen = KeyGenerator.getInstance(keyAlgorithm)
            keyGen.init(keySize, SecureRandom()) // 使用 SecureRandom 生成随机密钥
            val key = keyGen.generateKey()
            val base64Key = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
            preferences.edit { putString(PREF_ENCRYPTION_KEY, base64Key) }
            return key
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to generate or save encryption key", e)
        }
    }

    private fun decodeKey(base64Key: String): SecretKey {
        val decodedKeyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
        return SecretKeySpec(decodedKeyBytes, keyAlgorithm)
    }

    fun encrypt(data: ByteArray): EncryptedData {
        try {
            val cipher = Cipher.getInstance(cipherTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(data)
            return EncryptedData(iv, ciphertext)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to encrypt data", e)
        }
    }

    fun decrypt(encryptedData: EncryptedData): ByteArray {
        try {
            val cipher = Cipher.getInstance(cipherTransformation)
            val ivSpec = IvParameterSpec(encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            return cipher.doFinal(encryptedData.ciphertext)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to decrypt data", e)
        }
    }

    companion object {

        private var _instance: CryptoHelper? = null
        fun getInstance(context: Context): CryptoHelper =
            synchronized(CryptoHelper::class.java) {
                _instance ?: CryptoHelper(context).also {
                    _instance = it
                }
            }

    }

}

data class EncryptedData(val iv: ByteArray, val ciphertext: ByteArray) {
    fun toStorageString(): String {
        val ivBase64 = Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        val ciphertextBase64 = Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP)
        return "$ivBase64:$ciphertextBase64" // 使用分隔符
    }

    companion object {

        fun fromStorageString(storageString: String): EncryptedData? {
            val parts = storageString.split(":")
            if (parts.size != 2) return null
            return try {
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
                EncryptedData(iv, ciphertext)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!iv.contentEquals(other.iv)) return false
        if (!ciphertext.contentEquals(other.ciphertext)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        return result
    }
}