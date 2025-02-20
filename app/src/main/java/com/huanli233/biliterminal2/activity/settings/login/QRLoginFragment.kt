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
import com.huanli233.biliwebapi.bean.login.CookieActivePayload
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
            SharedPreferencesUtil.getString(SharedPreferencesUtil.COOKIES, "")

        SharedPreferencesUtil.putLong(
            SharedPreferencesUtil.MID,
            NetWorkUtil.getInfoFromCookie("DedeUserID", cookies).toLong()
        )
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.CSRF,
            NetWorkUtil.getInfoFromCookie("bili_jct", cookies)
        )
        SharedPreferencesUtil.putString(
            SharedPreferencesUtil.REFRESH_TOKEN,
            response.data?.refreshToken.orEmpty()
        )

        SharedPreferencesUtil.putBoolean(
            SharedPreferencesUtil.COOKIE_REFRESH,
            true
        )

        val instance = BiliTerminal.instanceActivityOnTop
        if (instance != null && !instance.isDestroyed) instance.finish()
        NetWorkUtil.refreshHeaders()
        bilibiliApi.getApi(ILoginApi::class.java).activeCookie(CookieActivePayload("{\"3064\":1,\"5062\":\"%d\",\"03bf\":\"https%%3A%%2F%%2Fwww.bilibili.com%%2F\",\"39c8\":\"333.1193.fp.risk\",\"34f1\":\"\",\"d402\":\"\",\"654a\":\"\",\"6e7c\":\"0x0\",\"3c43\":{\"2673\":0,\"5766\":24,\"6527\":0,\"7003\":1,\"807e\":1,\"b8ce\":\"%s\",\"641c\":1,\"07a4\":\"zh-CN\",\"1c57\":8,\"0bd0\":12,\"748e\":[830,1475],\"d61f\":[783,1475],\"fc9d\":-480,\"6aa9\":\"Asia/Shanghai\",\"75b8\":1,\"3b21\":1,\"8a1c\":1,\"d52f\":\"not available\",\"adca\":\"Win32\",\"80c9\":[[\"Chromium PDF Plugin\",\"Portable Document Format\",[[\"application/x-google-chrome-pdf\",\"pdf\"]]],[\"Chromium PDF Viewer\",\"\",[[\"application/pdf\",\"pdf\"]]]],\"13ab\":\"mCaDAAAAAElFTkSuQmCC\",\"bfe9\":\"EKJKMJaErGahJFAfsK/A/GlBW1/fBxgwAAAABJRU5ErkJggg==\",\"a3c1\":[\"extensions:ANGLE_instanced_arrays;EXT_blend_minmax;EXT_color_buffer_half_float;EXT_disjoint_timer_query;EXT_float_blend;EXT_frag_depth;EXT_shader_texture_lod;EXT_texture_compression_bptc;EXT_texture_compression_rgtc;EXT_texture_filter_anisotropic;EXT_sRGB;KHR_parallel_shader_compile;OES_element_index_uint;OES_fbo_render_mipmap;OES_standard_derivatives;OES_texture_float;OES_texture_float_linear;OES_texture_half_float;OES_texture_half_float_linear;OES_vertex_array_object;WEBGL_color_buffer_float;WEBGL_compressed_texture_s3tc;WEBGL_compressed_texture_s3tc_srgb;WEBGL_debug_renderer_info;WEBGL_debug_shaders;WEBGL_depth_texture;WEBGL_draw_buffers;WEBGL_lose_context;WEBGL_multi_draw\",\"webgl aliased line width range:[1, 1]\",\"webgl aliased point size range:[1, 1024]\",\"webgl alpha bits:8\",\"webgl antialiasing:yes\",\"webgl blue bits:8\",\"webgl depth bits:24\",\"webgl green bits:8\",\"webgl max anisotropy:16\",\"webgl max combined texture image units:32\",\"webgl max cube map texture size:16384\",\"webgl max fragment uniform vectors:1024\",\"webgl max render buffer size:16384\",\"webgl max texture image units:16\",\"webgl max texture size:16384\",\"webgl max varying vectors:30\",\"webgl max vertex attribs:16\",\"webgl max vertex texture image units:16\",\"webgl max vertex uniform vectors:4096\",\"webgl max viewport dims:[32767, 32767]\",\"webgl red bits:8\",\"webgl renderer:WebKit WebGL\",\"webgl shading language version:WebGL GLSL ES 1.0 (OpenGL ES GLSL ES 1.0 Chromium)\",\"webgl stencil bits:0\",\"webgl vendor:WebKit\",\"webgl version:WebGL 1.0 (OpenGL ES 2.0 Chromium)\",\"webgl unmasked vendor:Google Inc. (Intel)\",\"webgl unmasked renderer:ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)\",\"webgl vertex shader high float precision:23\",\"webgl vertex shader high float precision rangeMin:127\",\"webgl vertex shader high float precision rangeMax:127\",\"webgl vertex shader medium float precision:23\",\"webgl vertex shader medium float precision rangeMin:127\",\"webgl vertex shader medium float precision rangeMax:127\",\"webgl vertex shader low float precision:23\",\"webgl vertex shader low float precision rangeMin:127\",\"webgl vertex shader low float precision rangeMax:127\",\"webgl fragment shader high float precision:23\",\"webgl fragment shader high float precision rangeMin:127\",\"webgl fragment shader high float precision rangeMax:127\",\"webgl fragment shader medium float precision:23\",\"webgl fragment shader medium float precision rangeMin:127\",\"webgl fragment shader medium float precision rangeMax:127\",\"webgl fragment shader low float precision:23\",\"webgl fragment shader low float precision rangeMin:127\",\"webgl fragment shader low float precision rangeMax:127\",\"webgl vertex shader high int precision:0\",\"webgl vertex shader high int precision rangeMin:31\",\"webgl vertex shader high int precision rangeMax:30\",\"webgl vertex shader medium int precision:0\",\"webgl vertex shader medium int precision rangeMin:31\",\"webgl vertex shader medium int precision rangeMax:30\",\"webgl vertex shader low int precision:0\",\"webgl vertex shader low int precision rangeMin:31\",\"webgl vertex shader low int precision rangeMax:30\",\"webgl fragment shader high int precision:0\",\"webgl fragment shader high int precision rangeMin:31\",\"webgl fragment shader high int precision rangeMax:30\",\"webgl fragment shader medium int precision:0\",\"webgl fragment shader medium int precision rangeMin:31\",\"webgl fragment shader medium int precision rangeMax:30\",\"webgl fragment shader low int precision:0\",\"webgl fragment shader low int precision rangeMin:31\",\"webgl fragment shader low int precision rangeMax:30\"],\"6bc5\":\"Google Inc. (Intel)~ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)\",\"ed31\":0,\"72bd\":0,\"097b\":0,\"52cd\":[0,0,0],\"a658\":[],\"d02f\":\"124.04347527516074\"},\"54ef\":\"{}\",\"8b94\":\"https%%3A%%2F%%2Fwww.bilibili.com%%2F\",\"df35\":\"%s\",\"07a4\":\"zh-CN\",\"5f45\":null,\"db46\":0}")).code
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
