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
import com.huanli233.biliterminal2.api.LoginApi
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.util.CenterThreadPool
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.NetWorkUtil
import com.huanli233.biliterminal2.util.QRCodeUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.login.QrCode
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
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
            if (timer != null) timer!!.cancel()
            if (isAdded) requireActivity().finish()
        }

        val special = view.findViewById<MaterialCardView>(R.id.special)
        special.setOnClickListener {
            val intent = Intent(
                requireContext(),
                SpecialLoginActivity::class.java
            )
            intent.putExtra("from_setup", fromSetup)
            startActivity(intent)
            if (timer != null) timer!!.cancel()
            if (isAdded) requireActivity().finish()
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
                        MsgUtil.showMsg("切换为大二维码")
                        qrScale = 1
                    }

                    1 -> {
                        guidelineLeft.setGuidelinePercent(0.30f)
                        guidelineRight.setGuidelinePercent(0.70f)
                        MsgUtil.showMsg("切换为小二维码")
                        qrScale = 2
                    }

                    2 -> {
                        guidelineLeft.setGuidelinePercent(0.15f)
                        guidelineRight.setGuidelinePercent(0.85f)
                        MsgUtil.showMsg("切换为默认大小")
                        qrScale = 0
                    }
                }
            }
        }

        if (isAdded) refreshQrCode()
    }

    fun refreshQrCode() {
        println("refreshQrCode")
        lifecycleScope.launch {
            try {
                println("launchRequest")
                CenterThreadPool.runOnUiThread { scanStat.text = "正在获取二维码" }
                val qrCode = QrCode.generate(bilibiliApi)
                qrImage = QRCodeUtil.createQRCodeBitmap(qrCode.data!!.url, 320, 320)

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
        if (timer != null) timer!!.cancel()
        super.onDestroy()
    }

    fun startLoginDetect(data: QrCode) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
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
                            86090 -> CenterThreadPool.runOnUiThread {
                                scanStat.text =
                                    "已扫描，请在手机上点击登录"
                            }

                            86101 -> CenterThreadPool.runOnUiThread {
                                scanStat.text =
                                    "请使用官方手机端哔哩哔哩扫码登录\n点击二维码可以进行放大和缩小"
                            }

                            86038 -> {
                                CenterThreadPool.runOnUiThread {
                                    scanStat.text = "二维码已失效，点击上方重新获取"
                                    qrImageView.isEnabled = true
                                }
                                cancel()
                            }

                            0 -> {
                                cancel()
                                CenterThreadPool.runOnUiThread {
                                    scanStat.text =
                                        "正在处理登录……"
                                }
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

                                val instance = BiliTerminal.getInstanceActivityOnTop()
                                if (instance != null && !instance.isDestroyed) instance.finish()

                                NetWorkUtil.refreshHeaders()

                                bilibiliApi.getApi(ILoginApi::class.java).activeCookie(ACTIVE_COOKIE_PAYLOAD).code

                                startActivity(Intent(requireContext(), SplashActivity::class.java))

                                if (isAdded) requireActivity().finish()
                            }

                            else -> CenterThreadPool.runOnUiThread {
                                scanStat.text =
                                    "二维码登录API可能变动，\n但你仍然可以尝试扫码登录。\n建议反馈给开发者"
                            }
                        }
                    } catch (e: Exception) {
                        if (isAdded) CenterThreadPool.runOnUiThread {
                            qrImageView.isEnabled = true
                            scanStat.text = """
                            无法获取二维码信息，点击上方重试
                            ${e.message}
                            """.trimIndent()
                            MsgUtil.err(e)
                        }
                        cancel()
                    }
                }
            }
        }, 2000, 500)
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
}
