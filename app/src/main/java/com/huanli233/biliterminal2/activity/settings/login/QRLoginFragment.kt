package com.huanli233.biliterminal2.activity.settings.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.SplashActivity
import com.huanli233.biliterminal2.api.CookiesApi.ACTIVE_COOKIE_PAYLOAD
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.util.CenterThreadPool
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.NetWorkUtil
import com.huanli233.biliterminal2.util.QRCodeUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.login.QrCode
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class QRLoginFragment : Fragment() {
    private lateinit var qrImageView: ImageView
    private lateinit var scanStat: TextView
    var qrImage: Bitmap? = null
    var timer: Timer? = null
    var needRefresh: Boolean = false
    var fromSetup: Boolean = false
    var qrScale: Int = 0
    private var state = QRLoginState.NONE
        set(value) {
            field = value
            CenterThreadPool.runOnUiThread {
                when (value) {
                    QRLoginState.NONE ->
                        scanStat.text = getString(R.string.login_qrcode_reuesting)

                    QRLoginState.WAITING ->
                        scanStat.text = getString(R.string.login_qrcode_wating)

                    QRLoginState.EXPIRED -> {
                        scanStat.text = getString(R.string.login_qrcode_expired)
                        needRefresh = true
                        timer?.cancel()
                    }

                    QRLoginState.SCANNED ->
                        scanStat.text = getString(R.string.login_qrcode_scanned)

                    QRLoginState.LOGGED_IN -> {
                        scanStat.text =
                            getString(R.string.login_qrcode_logining)
                        timer?.cancel()
                    }

                    else -> scanStat.text =
                        getString(R.string.login_qrcode_unknown_code)
                }
            }
        }

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        val bundle = arguments
        if (bundle != null) fromSetup = bundle.getBoolean("from_setup", false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_login, container, false)

        qrImageView = view.findViewById(R.id.qrImage)
        scanStat = view.findViewById(R.id.scanStat)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val jump = view.findViewById<MaterialCardView>(R.id.jump)
        jump.setOnClickListener {
            if (fromSetup) startActivity(
                Intent(
                    requireContext(),
                    SplashActivity::class.java
                )
            )
            timer?.cancel()
            activity?.finish()
        }

        val special = view.findViewById<MaterialCardView>(R.id.special)
        special.setOnClickListener {
            val intent = Intent(
                requireContext(),
                SpecialLoginActivity::class.java
            )
            intent.putExtra("from_setup", fromSetup)
            startActivity(intent)
            timer?.cancel()
            activity?.finish()
        }

        qrImageView.setOnClickListener {
            if (needRefresh) {
                qrImageView.setImageResource(R.mipmap.loading)
                qrImageView.isEnabled = false
                refreshQrCode()
            } else {
                val guidelineLeft = view.findViewById<Guideline>(R.id.guideline33)
                val guidelineRight = view.findViewById<Guideline>(R.id.guideline34)
                when (qrScale) {
                    0 -> {
                        guidelineLeft.setGuidelinePercent(0.00f)
                        guidelineRight.setGuidelinePercent(1.00f)
                        qrScale = 1
                    }

                    1 -> {
                        guidelineLeft.setGuidelinePercent(0.30f)
                        guidelineRight.setGuidelinePercent(0.70f)
                        qrScale = 2
                    }

                    2 -> {
                        guidelineLeft.setGuidelinePercent(0.15f)
                        guidelineRight.setGuidelinePercent(0.85f)
                        qrScale = 0
                    }
                }
            }
        }

        if (isAdded) refreshQrCode()
    }

    private fun refreshQrCode() {
        lifecycleScope.launch {
            try {
                CenterThreadPool.runOnUiThread { scanStat.text = "正在获取二维码" }
                val qrCode = QrCode.generate(bilibiliApi)
                qrImage = QRCodeUtil.createQRCodeBitmap(qrCode.data!!.url, 320, 320)
                needRefresh = false

                CenterThreadPool.runOnUiThread {
                    qrImageView.setImageBitmap(qrImage)
                    startLoginDetect(qrCode.data!!)
                }
            } catch (e: IOException) {
                CenterThreadPool.runOnUiThread {
                    qrImageView.isEnabled = true
                    scanStat.text = "获取二维码失败，网络错误"
                }
                e.printStackTrace()
            } catch (e: JSONException) {
                CenterThreadPool.runOnUiThread {
                    qrImageView.isEnabled = true
                    scanStat.text = "登录接口可能失效，请找开发者"
                }
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun startLoginDetect(data: QrCode) {
        state = QRLoginState.WAITING
        timer = Timer().apply {
            schedule(object : TimerTask() {
                @SuppressLint("SetTextI18n")
                override fun run() {
                    lifecycleScope.launch {
                        try {
                            val response = data.poll()
                            if (!isAdded) {
                                cancel()
                                return@launch
                            }

                            val code = response.data?.code
                            when (code) {
                                86090 -> state = QRLoginState.SCANNED
                                86101 -> state = QRLoginState.WAITING
                                86038 -> state = QRLoginState.EXPIRED
                                0 -> {
                                    state = QRLoginState.LOGGED_IN
                                    processLogin(response)
                                }

                                else -> state = QRLoginState.UNKNOWN
                            }
                        } catch (e: Exception) {
                            if (isAdded) CenterThreadPool.runOnUiThread {
                                qrImageView.isEnabled = true
                                scanStat.text = """
                                    无法获取二维码信息，点击上方重试
                                    ${e.message}
                                """.trimIndent()
                                needRefresh = true
                                MsgUtil.err(e)
                            }
                            cancel()
                        }
                    }
                }
            }, 2000, 500)
        }
    }

    private suspend fun processLogin(response: ApiResponse<QrCode.LoginResult>) {
        val cookies =
            SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "")

        SharedPreferencesUtil.putLong(
            SharedPreferencesUtil.mid,
            NetWorkUtil.getInfoFromCookie("DedeUserID", cookies).toLong()
        )
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.csrf,
            NetWorkUtil.getInfoFromCookie("bili_jct", cookies)
        )
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.refresh_token,
            response.data?.refreshToken.orEmpty()
        )

        SharedPreferencesUtil.putBoolean(
            SharedPreferencesUtil.cookie_refresh,
            true
        )

        val instance = BiliTerminal.instanceActivityOnTop
        if (instance != null && !instance.isDestroyed) instance.finish()
        NetWorkUtil.refreshHeaders()
        bilibiliApi.getApi(ILoginApi::class.java).activeCookie(ACTIVE_COOKIE_PAYLOAD).code
        startActivity(Intent(requireContext(), SplashActivity::class.java))
        if (isAdded) requireActivity().finish()
    }

    companion object {
        @JvmStatic
        fun newInstance(fromSetup: Boolean): QRLoginFragment {
            val args = Bundle()
            args.putBoolean("from_setup", fromSetup)
            val fragment = QRLoginFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private enum class QRLoginState {
        NONE, WAITING, SCANNED, LOGGED_IN, EXPIRED, UNKNOWN
    }
}
