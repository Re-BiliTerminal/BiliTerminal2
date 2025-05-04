package com.huanli233.biliterminal2.data.account

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import android.util.Base64
import com.huanli233.biliterminal2.data.security.CryptoHelper
import com.huanli233.biliterminal2.data.security.EncryptedData

class AccountSecureStorage(
    context: Context,
    private val simpleCryptoHelper: CryptoHelper = CryptoHelper.getInstance(context),
    private val gson: Gson = Gson()
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("encrypted_app_prefs", Context.MODE_PRIVATE)

    private fun getTokenDataKey(accountId: Long): String {
        return "encrypted_data_$accountId"
    }

    fun saveTokenData(accountId: Long, sensitiveData: AccountTokenData) {
        try {
            val dataString = gson.toJson(sensitiveData)
            val dataBytes = dataString.toByteArray(Charsets.UTF_8)

            val encryptedData = simpleCryptoHelper.encrypt(dataBytes)

            sharedPreferences.edit {
                putString(getTokenDataKey(accountId), encryptedData.toStorageString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTokenData(accountId: Long): AccountTokenData? {
        val encryptedDataStorageString = sharedPreferences.getString(getTokenDataKey(accountId), null)
        if (encryptedDataStorageString == null) return null

        return try {
            val encryptedData = EncryptedData.fromStorageString(encryptedDataStorageString)
            if (encryptedData == null) return null

            val decryptedBytes = simpleCryptoHelper.decrypt(encryptedData)

            val dataString = String(decryptedBytes, Charsets.UTF_8)
            gson.fromJson(dataString, object : TypeToken<AccountTokenData>() {}.type)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteAccountData(accountId: Long) {
        sharedPreferences.edit {
            remove(getTokenDataKey(accountId))
        }
    }


}