package com.huanli233.biliterminal2.ui.fragment.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.data.account.AccountDao
import com.huanli233.biliterminal2.data.account.AccountEntity
import com.huanli233.biliterminal2.data.account.AccountManager
import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliterminal2.data.account.CookieEntity
import com.huanli233.biliterminal2.data.menu.MenuConfigManager
import com.huanli233.biliterminal2.databinding.FragmentLoginImportBinding
import com.huanli233.biliterminal2.ui.activity.login.EXTRA_NAME_FROM_SETUP
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.network.Cookies
import com.huanli233.biliwebapi.httplib.CookieManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject


@AndroidEntryPoint
class ImportLoginFragment(
    private val showMode: Boolean = false
): BaseFragment() {

    private lateinit var binding: FragmentLoginImportBinding

    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var accountDao: AccountDao
    @Inject lateinit var cookieManager: CookieManager

    private val fromSetup by lazy { arguments?.getBoolean(EXTRA_NAME_FROM_SETUP) == true }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (showMode) {
            lifecycleScope.launch {
                val token = AccountTokenData(
                    cookies = accountRepository.getCookies(),
                    refreshToken = AccountManager.currentAccount.refreshToken,
                    appKey = AccountManager.currentAccount.appKey
                )

                binding.tokenInput.setText(
                    Gson().toJson(token)
                )
                binding.confirmButton.text = getString(R.string.copy)
                binding.confirmButton.setOnClickListener {
                    val clipboardManager: ClipboardManager =
                        getSystemService(view.context, ClipboardManager::class.java) as ClipboardManager
                    val clipData = ClipData.newPlainText("label", binding.tokenInput.getText())
                    clipboardManager.setPrimaryClip(clipData)
                }
            }
        } else {
            binding.confirmButton.setOnClickListener {
                lifecycleScope.launch {
                    runCatching {
                        Gson().fromJson(binding.tokenInput.text.toString(), AccountTokenData::class.java).let {
                            it.also {
                                requireNotNull(it.cookies)
                                requireNotNull(it.refreshToken)
                                require(it.cookies.any { it.name == "DedeUserID" && it.value.toLongOrNull() != null })
                            }
                        }
                    }.onSuccess { tokenData ->
                        cookieManager.saveFromResponse(
                            "https://www.bilibili.com/".toHttpUrl(),
                            tokenData.cookies!!.map {
                                it.toOkHttpCookie()
                            }
                        )
                        accountRepository.addAccount(
                            AccountEntity(
                                accountId = tokenData.cookies.find { it.name == "DedeUserID" }?.value?.toLongOrNull() ?: -1,
                                refreshToken = tokenData.refreshToken,
                                appKey = tokenData.appKey,
                                lastActiveTime = System.currentTimeMillis()
                            )
                        )
                        MsgUtil.showMsg(getString(R.string.import_success))
                        if (fromSetup) {
                            startActivity(Intent(context, MenuConfigManager.readMenuConfig().firstActivityClass))
                        }
                        activity?.finish()
                    }.onFailure {
                        MsgUtil.showMsg(getString(R.string.invalid_input))
                    }
                }
            }
        }
    }

    data class AccountTokenData(
        val cookies: List<CookieEntity>? = null,
        val refreshToken: String? = null,
        val appKey: String? = null
    )

}