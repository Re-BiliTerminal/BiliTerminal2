package com.huanli233.bilizepam.ui.screens.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.account.AccountEntity
import com.huanli233.bilizepam.data.account.AccountRepository
import com.huanli233.bilizepam.data.account.CookieEntity
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.biliwebapi.httplib.CookieManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

@Composable
fun ImportLoginScreen(
    onLoginSuccess: () -> Unit,
    showMode: Boolean = false
) {
    var tokenJson by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val accountRepository = getHiltViewModel<ImportLoginViewModel>().accountRepository
    val cookieManager = getHiltViewModel<ImportLoginViewModel>().cookieManager

    if (showMode) {
        // Logic to show/export the current token
        // This is a side-effect, so it should be in a LaunchedEffect
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = tokenJson,
            onValueChange = { tokenJson = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.input_token_info)) },
            minLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    runCatching {
                        val token = Gson().fromJson(tokenJson, AccountTokenData::class.java)
                        // Basic validation
                        requireNotNull(token.cookies)
                        requireNotNull(token.refreshToken)
                        require(token.cookies.any { it.name == "DedeUserID" && it.value.toLongOrNull() != null })
                        token
                    }.onSuccess { tokenData ->
                        cookieManager.saveFromResponse(
                            "https://www.bilibili.com/".toHttpUrl(),
                            tokenData.cookies!!.map { it.toOkHttpCookie() }
                        )
                        accountRepository.addAccount(
                            AccountEntity(
                                accountId = tokenData.cookies.find { it.name == "DedeUserID" }?.value?.toLongOrNull() ?: -1,
                                refreshToken = tokenData.refreshToken,
                                appKey = tokenData.appKey,
                                lastActiveTime = System.currentTimeMillis()
                            )
                        )
                        MsgUtil.showMsg(context.getString(R.string.import_success))
                        onLoginSuccess()
                    }.onFailure {
                        it.printStackTrace()
                        MsgUtil.showMsg(context.getString(R.string.invalid_input))
                    }
                }
            },
            enabled = tokenJson.isNotBlank()
        ) {
            Text(stringResource(R.string.confirm))
        }
    }
}

data class AccountTokenData(
    val cookies: List<CookieEntity>? = null,
    val refreshToken: String? = null,
    val appKey: String? = null
)

@HiltViewModel
class ImportLoginViewModel @Inject constructor(
    val accountRepository: AccountRepository,
    val cookieManager: CookieManager
) : androidx.lifecycle.ViewModel()

@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> getHiltViewModel(): T {
    return hiltViewModel()
}