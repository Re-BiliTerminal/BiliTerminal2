package com.huanli233.biliterminal2.activity.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elvishew.xlog.XLog
import com.huanli233.biliterminal2.BiliTerminal.Companion.getFitDisplayContext
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.api.DanmakuApi
import com.huanli233.biliterminal2.api.VideoInfoApi
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.databinding.ActivityPlayerBinding
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.ui.widget.BatteryView
import com.huanli233.biliterminal2.ui.widget.recycler.CustomLinearManager
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.network.NetWorkUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliterminal2.util.Preferences.getFloat
import com.huanli233.biliterminal2.util.Preferences.getInt
import com.huanli233.biliterminal2.util.Preferences.getString
import com.huanli233.biliterminal2.util.Preferences.putBoolean
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import com.huanli233.biliwebapi.bean.video.SubtitleBody
import com.huanli233.biliwebapi.bean.video.SubtitleInfoItem
import com.huanli233.biliwebapi.httplib.HeaderValues
import kotlinx.coroutines.launch
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.Objects
import java.util.Timer
import java.util.TimerTask
import java.util.zip.Inflater
import kotlin.math.sqrt

open class PlayerActivity : AppCompatActivity(), IMediaPlayer.OnPreparedListener {
    private lateinit var binding: ActivityPlayerBinding

    private var destroyed = false

    private lateinit var ijkPlayer: IjkMediaPlayer
    private var mContext: DanmakuContext? = null

    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var mSurfaceTexture: SurfaceTexture? = null

    private var subtitleLinks: List<SubtitleInfoItem>? = null
    private var subtitles: List<SubtitleBody> = mutableListOf()
    private var subtitleCurrIndex = 0
    private var subtitleCount = 0

    private var progressTimer: Timer? = null
    private var autoHideTimer: Timer? = null
    private var volumeTimer: Timer? = null
    private var speedTimer: Timer? = null
    private var loadingTimer: Timer? = null
    private var onlineTimer: Timer? = null
    private var surfaceTimer: Timer? = null
    private var videoUrl: String? = null
    private var danmakuUrl: String? = null

    private var isPlaying = false
    private var isPrepared = false
    private var hasDanmaku = false
    private var isOnlineVideo = false
    private var isLiveMode = false
    private var isSeeking = false
    private var isDanmakuVisible = false
    private var menuOpened = false

    private var videoAll = 0
    private var videoNow = 0
    private var videoNowLast = 0
    private var progressHistory: Long = 0
    private var progressStr: String? = null

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var videoWidth = 0
    private var videoHeight = 0

    private var audioManager: AudioManager? = null

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var scaleGestureListener: ViewScaleGestureListener? = null
    private var previousX = 0f
    private var previousY = 0f
    private var gestureMoved = false
    private var gestureScaling = false
    private var gestureScaled = false
    private var gestureClickDisabled = false
    private var videoOrigx = 0f
    private var videoOrigy = 0f
    private var timestampClick: Long = 0
    private var onLongClick = false

    private val speedValues = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f)
    private val speedStrs =
        arrayOf("x 0.5", "x 0.75", "x 1.0", "x 1.25", "x 1.5", "x 1.75", "x 2.0", "x 3.0")

    private var finishWatching = false
    private var loopEnabled = false

    private var batteryView: BatteryView? = null
    private var batteryManager: BatteryManager? = null

    private var danmakuFile: File? = null

    private var screenLandscape = false
    private var screenRound = false

    @JvmField
    var onlineNumber: String = "0"

    private var aid: Long = 0
    private var cid: Long = 0
    private var mid: Long = 0

    @Deprecated("Deprecated in Java", ReplaceWith(
        "if (!getBoolean(\"back_disable\", false)) super.onBackPressed()",
        "com.huanli233.biliterminal2.util.Preferences.getBoolean",
        "androidx.appcompat.app.AppCompatActivity"
    )
    )
    override fun onBackPressed() {
        if (!getBoolean("back_disable", false)) super.onBackPressed()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(getFitDisplayContext(newBase))
    }

    private val extras: Boolean
        get() {
            val intent = intent ?: return false

            videoUrl = intent.getStringExtra("url")
            danmakuUrl = intent.getStringExtra("danmaku")
            val title = intent.getStringExtra("title")

            if (videoUrl == null) return false
            if (danmakuUrl != null) Log.d("弹幕", danmakuUrl!!)
            binding.textTitle.text = title

            aid = intent.getLongExtra("aid", 0)
            cid = intent.getLongExtra("cid", 0)
            mid = intent.getLongExtra("mid", 0)

            progressHistory = intent.getIntExtra("progress", 0).toLong()

            isLiveMode = intent.getBooleanExtra("live_mode", false)
            isOnlineVideo = videoUrl!!.contains("http")
            hasDanmaku = danmakuUrl != ""
            return true
        }

    @SuppressLint("SimpleDateFormat") @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenLandscape =
            getBoolean("player_autolandscape", false) || getBoolean("ui_landscape", false)
        requestedOrientation =
            if (screenLandscape) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        if (!extras) {
            finish()
            return
        }

        val windowManager = this.windowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels.toFloat()
        screenHeight = displayMetrics.heightPixels.toFloat()

        if (getBoolean("player_ui_showRotateBtn", true)) binding.rotateBtn.visibility = View.VISIBLE
        else binding.rotateBtn.visibility = View.GONE

        screenRound = getBoolean("player_ui_round", false)
        if (screenRound) {
            val padding = Utils.dp2px(8f)

            val paramProgress = binding.videoProgress.layoutParams as LinearLayout.LayoutParams
            paramProgress.leftMargin = padding * 4
            paramProgress.rightMargin = padding * 4
            binding.videoProgress.layoutParams = paramProgress

            binding.textOnline.setPadding(0, 0, padding * 3, 0)
            binding.textProgress.setPadding(padding * 3, 0, 0, 0)

            binding.top.setPadding(
                (padding * 5.5f).toInt(),
                padding * 2,
                (padding * 5.5f).toInt(),
                0
            )
            binding.textTitle.gravity = Gravity.CENTER

            binding.bottomButtons.setPadding(padding, 0, padding, padding)

            binding.rightControl.setPadding(0, 0, padding, 0)

            val params = binding.svDanmaku.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(0, padding * 3, 0, padding * 3)
            binding.svDanmaku.layoutParams = params

            findViewById<View>(R.id.cl_1).visibility = View.GONE
        }

        if ((!getBoolean("show_online", true)) || aid == 0L || cid == 0L) binding.textOnline.visibility =
            View.GONE

        IjkMediaPlayer.loadLibrariesOnce(null)

        ijkPlayer = IjkMediaPlayer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
            batteryView!!.setPower(batteryManager!!.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
        } else batteryView!!.visibility = View.GONE

        loopEnabled = getBoolean("player_loop", false)
        Glide.with(this).load(R.mipmap.load).into(binding.circle)

        val cachepath = cacheDir
        if (!cachepath.exists()) cachepath.mkdirs()
        danmakuFile = File(cachepath, "danmaku.xml")

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        setVideoGestures()
        autohide()

        binding.videoProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, position: Int, fromUser: Boolean) {
                runOnUiThread {
                    if (!isLiveMode) binding.textProgress.text =
                        Utils.toTime(position / 1000) + "/" + progressStr
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
                if (autoHideTimer != null) autoHideTimer!!.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isPrepared) {
                    ijkPlayer.seekTo(binding.videoProgress.progress.toLong())
                    isSeeking = false
                }
                autohide()
            }
        })

        binding.seekbarSpeed.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, position: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textNewspeed.text = speedStrs[position]
                    binding.textSpeed.text = speedStrs[position]
                    ijkPlayer.setSpeed(speedValues[position])
                    binding.svDanmaku.setSpeed(speedValues[position])
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (speedTimer != null) speedTimer!!.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                speedTimer = Timer()
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            binding.layoutSpeed.visibility =
                                View.GONE
                        }
                    }
                }
                speedTimer!!.schedule(timerTask, 200)
            }
        })

        if (isLiveMode) {
            binding.buttonVideo.visibility = View.INVISIBLE // 暂停的话可能会出一些bug，那就别暂停了，卡住就退出重进吧（
            binding.videoProgress.visibility = View.GONE
            binding.videoProgress.isEnabled = false
            streamDanmaku(null) // 用来初始化一下弹幕层
            danmuSocketConnect()
        }

        val handler = Handler()
        handler.postDelayed({
            ThreadManager.run {
                // 等界面加载完成
                if (isLiveMode) {
                    runOnUiThread { binding.menuBtn.visibility = View.GONE }
                    setDisplay()
                    return@run
                }

                runOnUiThread {
                    binding.loadingText0.text = "装填弹幕中"
                    binding.loadingText1.text = "(≧∇≦)"
                }
                if (isOnlineVideo) {
                    downDanmaku()
                    if (getBoolean(
                            "player_subtitle_autoshow",
                            true
                        )
                    ) lifecycleScope.launch {
                        downSubtitle(false)
                    }
                } else {
                    runOnUiThread {
                        binding.subtitleBtn.visibility = View.GONE
                        binding.danmakuSendBtn.visibility = View.GONE
                    }
                    streamDanmaku(danmakuUrl)
                }
                if (!destroyed) setDisplay()
            }
        }, 60)
    }


    private fun initView() {
        binding.top.setOnClickListener { finish() }

        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (getBoolean("player_display", Build.VERSION.SDK_INT < 19)) {
            textureView = TextureView(this).apply {
                surfaceTextureListener = object : SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        i: Int,
                        i1: Int
                    ) {
                        mSurfaceTexture = surfaceTexture
                        if (isPrepared) ijkPlayer.setSurface(
                            Surface(
                                surfaceTexture
                            )
                        )
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surfaceTexture: SurfaceTexture,
                        i: Int,
                        i1: Int
                    ) {
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        mSurfaceTexture = null
                        ijkPlayer.setSurface(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                }
            }
            binding.videoArea.addView(textureView, params)
        } else {
            surfaceView = SurfaceView(this)
            binding.videoArea.addView(surfaceView, params)
        }

        binding.rotateBtn.setOnClickListener { rotate() }

        binding.buttonSoundAdd.setOnClickListener {
            changeVolume(true)
        }
        binding.buttonSoundCut.setOnClickListener {
            changeVolume(false)
        }

        binding.menuBtn.setOnClickListener {
            if (menuOpened) {
                binding.rightSecond.visibility = View.GONE
                binding.menuBtn.setImageResource(R.mipmap.morehide)
            } else {
                binding.rightSecond.visibility = View.VISIBLE
                binding.menuBtn.setImageResource(R.mipmap.moreshow)
            }
            menuOpened = !menuOpened
        }

        binding.cardBg.setOnClickListener {
            binding.cardBg.visibility = View.GONE
            binding.subtitleCard.visibility = View.GONE
            binding.danmakuSendCard.visibility = View.GONE
        }
        binding.danmakuSendBtn.setOnClickListener { view: View? ->
            binding.cardBg.visibility = View.VISIBLE
            binding.danmakuSendCard.visibility = View.VISIBLE
        }
        findViewById<View>(R.id.danmaku_send).setOnClickListener {
            val editText = findViewById<EditText>(R.id.danmaku_send_edit)
            if (editText.text.toString().isEmpty()) {
                MsgUtil.showMsg("不能发送空弹幕喵")
            } else {
                binding.cardBg.visibility = View.GONE
                binding.danmakuSendCard.visibility = View.GONE

                ThreadManager.run {
                    try {
                        MsgUtil.showMsg(getString(R.string.sending))

                        val result = DanmakuApi.sendVideoDanmakuByAid(
                            cid,
                            editText.text.toString(),
                            aid,
                            videoNow.toLong(),
                            Utils.getRgb888(Color.WHITE),
                            1
                        )

                        if (result == 0) {
                            MsgUtil.showMsg(getString(R.string.send_success))
                            runOnUiThread {
                                addDanmaku(editText.text.toString(), Color.WHITE)
                                editText.setText("")
                            }
                        } else MsgUtil.showMsg(getString(R.string.send_failed_with_msg, result.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        MsgUtil.error(e)
                    }
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setVideoGestures() {
        if (getBoolean("player_scale", true)) {
            scaleGestureListener = ViewScaleGestureListener(binding.videoArea)
            scaleGestureDetector = ScaleGestureDetector(
                this, scaleGestureListener
            )

            val doublemoveEnabled = getBoolean("player_doublemove", true) //是否启用双指移动

            binding.controlLayout.setOnTouchListener { v: View?, event: MotionEvent ->
                val action = event.actionMasked
                val pointerCount = event.pointerCount
                val singleTouch = pointerCount == 1
                val doubleTouch = pointerCount == 2

                scaleGestureDetector!!.onTouchEvent(event)
                gestureScaling = scaleGestureListener!!.scaling

                if (!gestureScaled && gestureScaling) {
                    gestureScaled = true
                }

                when (action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (singleTouch) {
                            if (gestureScaling) {
                                videoMoveTo(binding.videoArea.x, binding.videoArea.y)
                            } else if (!(gestureScaled && !doublemoveEnabled)) {
                                val currentX = event.getX(0)
                                val currentY = event.getY(0)
                                val deltaX = currentX - previousX
                                val deltaY = currentY - previousY
                                if (deltaX != 0f || deltaY != 0f) {
                                    videoMoveTo(
                                        binding.videoArea.x + deltaX,
                                        binding.videoArea.y + deltaY
                                    )
                                    previousX = currentX
                                    previousY = currentY
                                }
                            }
                        }
                        if (doubleTouch && doublemoveEnabled) {
                            val currentX = (event.getX(0) + event.getX(1)) / 2
                            val currentY = (event.getY(0) + event.getY(1)) / 2
                            val deltaX = currentX - previousX
                            val deltaY = currentY - previousY
                            if (deltaX != 0f || deltaY != 0f) {
                                videoMoveTo(binding.videoArea.x + deltaX, binding.videoArea.y + deltaY)
                                previousX = currentX
                                previousY = currentY
                            }
                        }
                    }

                    MotionEvent.ACTION_DOWN -> if (singleTouch) {
                        // 如果是单指按下，设置起始位置为当前手指位置
                        previousX = event.getX(0)
                        previousY = event.getY(0)
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> if (doubleTouch) {
                        // 如果是双指按下，设置起始位置为两指连线的中心点
                        previousX = (event.getX(0) + event.getX(1)) / 2
                        previousY = (event.getY(0) + event.getY(1)) / 2
                        hideControls()
                    }

                    MotionEvent.ACTION_POINTER_UP -> if (doubleTouch) {
                        val index = event.actionIndex // actionIndex是抬起来的手指位置
                        previousX = event.getX((if (index == 0) 1 else 0))
                        previousY = event.getY((if (index == 0) 1 else 0))
                    }

                    MotionEvent.ACTION_UP -> {
                        if (onLongClick) {
                            onLongClick = false
                            ijkPlayer.setSpeed(speedValues[binding.seekbarSpeed.progress])
                            binding.svDanmaku.setSpeed(speedValues[binding.seekbarSpeed.progress])
                            binding.textSpeed.text = speedStrs[binding.seekbarSpeed.progress]
                        }
                        if (gestureMoved) {
                            gestureMoved = false
                        }
                        if (gestureScaled) {
                            gestureScaled = false
                        }
                    }
                }

                if (!gestureClickDisabled && (gestureMoved || gestureScaled)) gestureClickDisabled =
                    true
                false
            }
        } else {
            binding.controlLayout.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP && onLongClick) {
                    onLongClick = false
                    ijkPlayer.setSpeed(speedValues[binding.seekbarSpeed.progress])
                    binding.svDanmaku.setSpeed(speedValues[binding.seekbarSpeed.progress])
                    binding.textSpeed.text = speedStrs[binding.seekbarSpeed.progress]
                }
                false
            }
        }

        binding.controlLayout.setOnClickListener {
            if (gestureClickDisabled) gestureClickDisabled = false
            else clickUI()
        }
        binding.controlLayout.setOnLongClickListener {
            if (getBoolean(
                    "player_longclick",
                    true
                ) && isPlaying && !isLiveMode
            ) {
                if (!onLongClick && !gestureMoved && !gestureScaled) {
                    hideControls()
                    ijkPlayer.setSpeed(3.0f)
                    binding.svDanmaku.setSpeed(3.0f)
                    binding.textSpeed.text = "x 3.0"
                    onLongClick = true
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
            false
        }
    }


    private fun autohide() {
        if (autoHideTimer != null) autoHideTimer!!.cancel()
        autoHideTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread { hideControls() }
                this.cancel()
            }
        }
        autoHideTimer!!.schedule(timerTask, 4000)
    }

    private fun clickUI() {
        val nowTimestamp = System.currentTimeMillis()
        if (nowTimestamp - timestampClick < 300) {
            if (getBoolean("player_scale", true) && scaleGestureListener!!.can_reset) {
                scaleGestureListener!!.can_reset = false
                binding.videoArea.x = videoOrigx
                binding.videoArea.y = videoOrigy
                binding.videoArea.scaleX = 1.0f
                binding.videoArea.scaleY = 1.0f
            } else if (!isLiveMode) {
                if (isPlaying) playerPause()
                else playerResume()
                showControls()
            }
        } else {
            timestampClick = nowTimestamp
            if ((binding.top.visibility) == View.GONE) showControls()
            else hideControls()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showControls() {
        binding.rightControl.visibility = View.VISIBLE
        binding.top.visibility = View.VISIBLE
        binding.bottomButtons.visibility = View.VISIBLE
        binding.videoProgress.visibility = View.VISIBLE
        if (isPrepared && !isLiveMode) binding.textSpeed.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView!!.setPower(batteryManager!!.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
        }
        if (screenRound) {
            binding.textProgress.gravity = Gravity.NO_GRAVITY
            binding.textProgress.setPadding(Utils.dp2px(24f), 0, 0, 0)
            if (onlineTimer != null) binding.textOnline.visibility = View.VISIBLE
        }

        autohide()
    }

    private fun hideControls() {
        binding.rightControl.visibility = View.GONE
        binding.top.visibility = View.GONE
        binding.bottomButtons.visibility = View.GONE
        binding.videoProgress.visibility = View.GONE
        if (isPrepared) binding.textSpeed.visibility = View.GONE
        if (screenRound) {
            binding.textProgress.gravity = Gravity.CENTER
            binding.textProgress.setPadding(0, 0, 0, Utils.dp2px(8f))
            if (onlineTimer != null) binding.textOnline.visibility = View.GONE
        }
        if (menuOpened) binding.menuBtn.performClick()
    }


    private fun setDisplay() {
        runOnUiThread { binding.loadingText0.text = getString(R.string.init_player) }

        ijkPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "mediacodec",
            (if (getBoolean("player_codec", true)) 1 else 0).toLong()
        )
        ijkPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "opensles",
            (if (getBoolean("player_audio", false)) 1 else 0).toLong()
        )
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        ijkPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "mediacodec-handle-resolution-change",
            1
        )
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 2)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1)
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)

        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets")
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)

        if (isOnlineVideo) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1)
            ijkPlayer.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "max-buffer-size",
                (15 * 1000 * 1000).toLong()
            )
            ijkPlayer.setOption(
                IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                "user_agent",
                NetWorkUtil.USER_AGENT_WEB
            )
        }

        if (getBoolean("player_display", Build.VERSION.SDK_INT < 19)) {
            surfaceTimer = Timer()
            surfaceTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (mSurfaceTexture != null) {
                        this.cancel()
                        val surface = Surface(mSurfaceTexture)
                        ijkPlayer.setSurface(surface)
                        mpPrepare(videoUrl)
                    }
                }
            }, 0, 200)
        } else {
            val surfaceHolder = surfaceView!!.holder
            surfaceTimer = Timer()
            surfaceTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (!surfaceHolder.isCreating) {
                        this.cancel()
                        ijkPlayer.setDisplay(surfaceHolder)
                        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                                if (!destroyed) {
                                    ijkPlayer.setDisplay(surfaceHolder)
                                    if (isPrepared) {
                                        ijkPlayer.seekTo(binding.videoProgress.progress.toLong())
                                    }
                                }
                            }

                            override fun surfaceChanged(
                                surfaceHolder: SurfaceHolder,
                                i: Int,
                                i1: Int,
                                i2: Int
                            ) {
                            }

                            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                                if (isPrepared && !destroyed) ijkPlayer.setDisplay(null)
                            }
                        })
                        mpPrepare(videoUrl)
                    }
                }
            }, 0, 200)
        }
    }

    private fun mpPrepare(nowurl: String?) {
        ijkPlayer.setOnPreparedListener(this)

        if (isLiveMode) runOnUiThread { binding.loadingText0.text = "载入直播中" }
        else runOnUiThread { binding.loadingText0.text = "载入视频中" }
        try {
            if (isOnlineVideo) {
                val headers: MutableMap<String, String> = HashMap()
                headers["Referer"] = "https://www.bilibili.com/"
                headers["Cookie"] = getString(Preferences.COOKIES, "")
                ijkPlayer.setDataSource(nowurl, headers)
            } else ijkPlayer.dataSource = nowurl
        } catch (e: IOException) {
            e.printStackTrace()
        }

        ijkPlayer.setOnCompletionListener {
            finishWatching = true
            if (loopEnabled) {
                ijkPlayer.seekTo(0)
                ijkPlayer.start()
            } else {
                isPlaying = false
                if (hasDanmaku) binding.svDanmaku.pause()
                binding.buttonVideo.setImageResource(R.drawable.btn_player_play)
            }
        }

        ijkPlayer.setOnErrorListener { _: IMediaPlayer?, what: Int, extra: Int ->
            XLog.e("播放器可能遇到错误！\n错误码：$what\n附加：$extra")
            false
        }

        ijkPlayer.setOnBufferingUpdateListener { mp: IMediaPlayer?, percent: Int ->
            binding.videoProgress.secondaryProgress =
                percent * videoAll / 100
        }

        if (isOnlineVideo || isLiveMode) ijkPlayer.setOnInfoListener { mp: IMediaPlayer?, what: Int, extra: Int ->
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                runOnUiThread {
                    binding.circle.visibility = View.VISIBLE
                    binding.loadingText0.text = "正在缓冲"
                    showLoadingSpeed()
                    if (isPlaying) binding.svDanmaku.pause()
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                runOnUiThread {
                    if (loadingTimer != null) loadingTimer!!.cancel()
                    binding.circle.visibility = View.GONE
                    if (isPlaying) binding.svDanmaku.start(ijkPlayer.currentPosition)
                }
            }
            false
        }

        ijkPlayer.setScreenOnWhilePlaying(true)
        ijkPlayer.prepareAsync()
    }

    @SuppressLint("SetTextI18n")
    override fun onPrepared(mediaPlayer: IMediaPlayer) {
        if (destroyed) {
            mediaPlayer.release()
            return
        }

        isPrepared = true
        videoAll = mediaPlayer.duration.toInt()

        changeVideoSize(mediaPlayer.videoWidth, mediaPlayer.videoHeight)

        if (isLiveMode || hasDanmaku) binding.svDanmaku.start()
        if (getBoolean("player_ui_showDanmakuBtn", true)) {
            isDanmakuVisible = !getBoolean("pref_switch_danmaku", true)
            binding.danmakuBtn.setOnClickListener {
                if (isDanmakuVisible) binding.svDanmaku.hide()
                else binding.svDanmaku.show()
                binding.danmakuBtn.setImageResource((if (isDanmakuVisible) R.mipmap.danmakuoff else R.mipmap.danmakuon))
                isDanmakuVisible = !isDanmakuVisible
                putBoolean("pref_switch_danmaku", isDanmakuVisible)
            }
            binding.danmakuBtn.performClick()

            binding.danmakuBtn.visibility = View.VISIBLE
        } else binding.danmakuBtn.visibility = View.GONE

        if (!isLiveMode) {
            if (loopEnabled) binding.loopBtn.setImageResource(R.mipmap.loopon)
            else binding.loopBtn.setImageResource(R.mipmap.loopoff)
            binding.loopBtn.setOnClickListener { view: View? ->
                binding.loopBtn.setImageResource((if (loopEnabled) R.mipmap.loopoff else R.mipmap.loopon))
                loopEnabled = !loopEnabled
            }
            binding.loopBtn.visibility = View.VISIBLE
        }


        binding.videoProgress.max = videoAll
        progressStr = Utils.toTime(videoAll / 1000)

        if (getBoolean("player_from_last", true) && !isLiveMode) {
            if (progressHistory > 6 && ((videoAll / 1000) - progressHistory) > 6) {
                mediaPlayer.seekTo(progressHistory * 1000)
                runOnUiThread { MsgUtil.showMsg("已从上次的位置播放") }
            }
        }

        binding.circle.visibility = View.GONE
        isPlaying = true
        binding.buttonVideo.setImageResource(R.drawable.btn_player_pause)

        binding.textSpeed.visibility = binding.top.visibility
        if (isLiveMode) binding.textSpeed.visibility = View.GONE
        binding.textSpeed.setOnClickListener {
            binding.layoutSpeed.visibility =
                View.VISIBLE
        }
        binding.layoutSpeed.setOnClickListener {
            binding.layoutSpeed.visibility =
                View.GONE
        }

        progressChange()
        onlineChange()

        mediaPlayer.start()

        binding.buttonVideo.setOnClickListener { controlVideo() }
        binding.buttonVideo.setOnClickListener {
            lifecycleScope.launch {
                downSubtitle(
                    true
                )
            }
        }
    }

    private fun showLoadingSpeed() {
        loadingTimer = Timer()
        loadingTimer!!.schedule(object : TimerTask() {
            override fun run() {
                val text =
                    String.format(Locale.CHINA, "%.1f", ijkPlayer.tcpSpeed / 1024f) + "KB/s"
                runOnUiThread { binding.loadingText1.text = text }
            }
        }, 0, 500)
    }

    private fun changeVideoSize(width: Int, height: Int) {
        if (getBoolean("player_ui_round", false)) {
            val videoMul = height.toFloat() / width.toFloat()
            val sqrt =
                sqrt((screenWidth.toDouble() * screenWidth.toDouble()) / (((height.toDouble() * height.toDouble()) / (width.toDouble() * width.toDouble())) + 1))
            videoHeight = (sqrt * videoMul + 0.5).toInt()
            videoWidth = (sqrt + 0.5).toInt()
        } else {
            val multiplewidth = screenWidth / width
            val multipleheight = screenHeight / height
            val endhi1 = (height * multipleheight).toInt()
            val endwi1 = (width * multipleheight).toInt()
            val endhi2 = (height * multiplewidth).toInt()
            val endwi2 = (width * multiplewidth).toInt()
            if (endhi1 <= screenHeight && endwi1 <= screenWidth) {
                videoHeight = endhi1
                videoWidth = endwi1
            } else {
                videoHeight = endhi2
                videoWidth = endwi2
            }
        }

        runOnUiThread {
            binding.videoArea.layoutParams = RelativeLayout.LayoutParams(videoWidth, videoHeight)
            videoOrigx = (screenWidth - videoWidth) / 2
            videoOrigy = (screenHeight - videoHeight) / 2
            binding.videoArea.postDelayed({
                binding.videoArea.x = videoOrigx
                binding.videoArea.y = videoOrigy
            }, 60)
        }
    }


    private fun progressChange() {
        progressTimer = Timer()
        val task: TimerTask = object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (isPrepared && isPlaying && !isSeeking) {
                    videoNow = ijkPlayer.currentPosition.toInt()
                    if (videoNowLast != videoNow) {
                        videoNowLast = videoNow
                        val currSec = videoNow / 1000f
                        runOnUiThread {
                            if (isLiveMode) {
                                binding.textProgress.text = Utils.toTime(currSec.toInt())
                                binding.textOnline.text = onlineNumber
                            } else {
                                binding.videoProgress.progress = videoNow
                            }
                        }
                        if (subtitles.isNotEmpty()) showSubtitle(currSec)
                        else runOnUiThread {
                            binding.textSubtitle.visibility =
                                View.GONE
                        }
                    }
                }
            }
        }
        progressTimer!!.schedule(task, 0, 250)
    }

    private fun onlineChange() {
        if (!getBoolean("show_online", false) || isLiveMode || aid == 0L || cid == 0L) return

        onlineTimer = Timer()
        val task: TimerTask = object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                try {
                    onlineNumber = VideoInfoApi.getWatching(aid, cid)
                    runOnUiThread {
                        if (onlineNumber.isNotEmpty()) binding.textOnline.text =
                            getString(R.string.live_viewer_count, onlineNumber)
                        else binding.textOnline.text = ""
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        MsgUtil.error(e)
                        binding.textOnline.visibility = View.GONE
                    }
                    this.cancel()
                }
            }
        }
        onlineTimer!!.schedule(task, 0, 5000)
    }

    private suspend fun getSubtitle(subtitleUrl: String?) {
        if (subtitleUrl.isNullOrEmpty()) return
        try {
            subtitles = bilibiliApi.api(IVideoApi::class) { getSubtitleContent(subtitleUrl) }.apiResultNonNull().getOrThrow().body
            subtitleCount = subtitles.size
            subtitleCurrIndex = 0
            runOnUiThread { binding.subtitleBtn.setImageResource(R.mipmap.subtitle_on) }
        } catch (e: Exception) {
            MsgUtil.error(e)
        }
    }

    private fun showSubtitle(currSec: Float) {
        val subtitleCurr = subtitles[subtitleCurrIndex]

        var needAdjust = true
        var needShow = true

        while (needAdjust) {
            if (currSec < subtitleCurr.from) {
                // 进度在当前字幕的起始位置之前
                // 如果不是第一条字幕，且进度在上一条字幕的结束位置之前，那么字幕前移一位
                // 否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitleCurrIndex != 0 && currSec < subtitles[subtitleCurrIndex - 1].to) {
                    subtitleCurrIndex--
                } else {
                    needAdjust = false
                    needShow = false
                }
            } else if (currSec > subtitleCurr.to) {
                // 在当前字幕的结束位置之后
                // 如果不是最后一条字幕，且进度在下一条字幕的开始位置之后，那么字幕后移一位
                // 否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitleCurrIndex + 1 < subtitleCount && currSec > subtitles[subtitleCurrIndex + 1].from) {
                    subtitleCurrIndex++
                } else {
                    needAdjust = false
                    needShow = false
                }
            } else {
                // 在当前字幕的时间段内，则退出校准
                needAdjust = false
            }
        }

        if (needShow) runOnUiThread {
            binding.textSubtitle.text = subtitles[subtitleCurrIndex].content
            binding.textSubtitle.visibility = View.VISIBLE
        }
        else runOnUiThread { binding.textSubtitle.visibility = View.GONE }
    }

    private var subtitleSelected = -1

    private suspend fun downSubtitle(fromBtn: Boolean) {
        try {
            val subtitles = if (subtitleLinks == null)
                bilibiliApi.api(IVideoApi::class) { getPlayerInfo(aid, cid) }.apiResultNonNull().getOrThrow().subtitle.list
            else subtitleLinks!!
            val aiNotOnly =
                subtitles.size > 2 || (subtitles.size == 2 && subtitles[0].type != 1)
            val aiAllowed = fromBtn || getBoolean("player_subtitle_ai_allowed", false)

            if (aiNotOnly || aiAllowed) {
                if (subtitleSelected == -1) subtitleSelected = subtitles.size

                runOnUiThread {
                    val eposideRecyclerView =
                        findViewById<RecyclerView>(R.id.subtitle_list)
                    val adapter = SubtitleAdapter()
                    adapter.setData(subtitles)
                    adapter.selectedItemIndex = subtitleSelected
                    adapter.setOnItemClickListener { index: Int ->
                        binding.cardBg.visibility = View.GONE
                        binding.subtitleCard.visibility = View.GONE
                        subtitleSelected = index
                        if (subtitles[index].id == -1L) {
                            subtitleLinks = listOf()
                            binding.subtitleBtn.setImageResource(R.mipmap.subtitle_off)
                        } else lifecycleScope.launch {
                            getSubtitle(
                                subtitles[index].subtitleUrl
                            )
                        }
                    }
                    eposideRecyclerView.layoutManager =
                        CustomLinearManager(
                            this,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    eposideRecyclerView.setHasFixedSize(true)
                    eposideRecyclerView.adapter = adapter
                    binding.cardBg.visibility = View.VISIBLE
                    binding.subtitleCard.visibility = View.VISIBLE
                }
            } else if (fromBtn) MsgUtil.showMsg(getString(R.string.subtitle_option_not_available))
        } catch (e: Exception) {
            e.printStackTrace()
            MsgUtil.error(e)
        }
    }

    private fun downDanmaku() {
        if (danmakuUrl!!.isEmpty()) return
        try {
            val response = NetWorkUtil.get(danmakuUrl, NetWorkUtil.webHeaders)
            var bufferedSink: BufferedSink? = null
            try {
                if (!danmakuFile!!.exists()) danmakuFile!!.createNewFile()
                val sink = danmakuFile!!.sink()
                val decompressBytes = decompress(Objects.requireNonNull(response.body).bytes())
                bufferedSink = sink.buffer()
                bufferedSink.write(decompressBytes)
                bufferedSink.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                bufferedSink?.close()
            }
            streamDanmaku(danmakuFile.toString())
        } catch (e: Exception) {
            runOnUiThread { MsgUtil.error(e) }
        }
    }

    private fun createParser(stream: String?): BaseDanmakuParser {
        if (stream == null) {
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }

        val loader = checkNotNull(DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI))

        loader.load(stream)
        val parser: BaseDanmakuParser = BiliDanmukuParser()
        parser.sharedPreferences = Preferences.sharedPreferences
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    private fun streamDanmaku(danmakuPath: String?) {
        val mContext = DanmakuContext.create().also {
            this.mContext = it
        }
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] =
            getInt("player_danmaku_maxline", 15)
        val overlap = HashMap<Int, Boolean>()
        overlap[BaseDanmaku.TYPE_SCROLL_LR] =
            getBoolean("player_danmaku_allowoverlap", true)
        overlap[BaseDanmaku.TYPE_FIX_BOTTOM] =
            getBoolean("player_danmaku_allowoverlap", true)
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 1f)
            .setDuplicateMergingEnabled(getBoolean("player_danmaku_mergeduplicate", false))
            .setScrollSpeedFactor(getFloat("player_danmaku_speed", 1.0f))
            .setScaleTextSize(getFloat("player_danmaku_size", 0.7f)) //缩放值
            .setMaximumLines(maxLinesPair)
            .setDanmakuTransparency(getFloat("player_danmaku_transparency", 0.5f))
            .preventOverlapping(overlap)

        val mParser = createParser(danmakuPath)

        binding.svDanmaku.setCallback(object : DrawHandler.Callback {
            override fun prepared() {
                addDanmaku("弹幕君准备完毕～(*≧ω≦)", Color.WHITE)
            }

            override fun updateTimer(timer: DanmakuTimer) {
                // 不需要if(isPlaying)，因为本来就为了让弹幕跟随ijkPlayer的时间停止而停止
                timer.update(ijkPlayer.currentPosition) // 实时同步弹幕和播放器时间
            }

            override fun danmakuShown(danmaku: BaseDanmaku) {
            }

            override fun drawingFinished() {
            }
        })
        binding.svDanmaku.enableDanmakuDrawingCache(true)
        binding.svDanmaku.prepare(mParser, mContext)
    }

    @JvmOverloads
    fun addDanmaku(
        text: String?,
        color: Int,
        textSize: Int = 25,
        type: Int = 1,
        backgroundColor: Int = 0
    ) {
        val danmaku = mContext!!.mDanmakuFactory.createDanmaku(type)
        if (text == null || danmaku == null) return
        danmaku.text = text
        danmaku.padding = 5
        danmaku.priority = 1
        danmaku.textColor = color
        danmaku.backgroundColor = backgroundColor
        danmaku.textSize = textSize * (mContext!!.displayer.density - 0.6f)
        danmaku.time = binding.svDanmaku.currentTime + 100
        binding.svDanmaku.addDanmaku(danmaku)
    }

    fun controlVideo() {
        if (isPlaying) {
            playerPause()
            if (autoHideTimer != null) autoHideTimer!!.cancel()
        } else {
            isPlaying = true
            // 因为弹幕实时同步，不需要自行设置弹幕时间了
            if (videoNow >= videoAll - 250) {
                ijkPlayer.seekTo(0)
                binding.svDanmaku.resume()
            }
            ijkPlayer.start()
            binding.buttonVideo.setImageResource(R.drawable.btn_player_pause)
        }
        autohide()
    }

    @SuppressLint("SetTextI18n")
    fun changeVolume(addOrCut: Boolean) {
        if (autoHideTimer != null) autoHideTimer!!.cancel()
        var volumeNow = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volumeMax = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumeNew = volumeNow + (if (addOrCut) 1 else -1)
        if (volumeNew in 0..volumeMax) {
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volumeNew, 0)
            volumeNow = volumeNew
        }
        val show = (volumeNow.toFloat() / volumeMax.toFloat() * 100).toInt()

        binding.showSound.visibility = View.VISIBLE
        binding.showSound.text = "音量：$show%"

        hideVolume()
        autohide()
    }

    private fun hideVolume() {
        if (volumeTimer != null) volumeTimer!!.cancel()
        volumeTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread { binding.showSound.visibility = View.GONE }
            }
        }
        volumeTimer!!.schedule(timerTask, 3000)
    }

    fun rotate() {
        screenLandscape = !screenLandscape
        requestedOrientation =
            if (screenLandscape) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    @Suppress("DEPRECATION")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val windowManager = this.windowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels.toFloat() // 获取屏宽
        screenHeight = displayMetrics.heightPixels.toFloat() // 获取屏高
        if (isPrepared) {
            changeVideoSize(ijkPlayer.videoWidth, ijkPlayer.videoHeight)
        }
    }

    private fun videoMoveTo(x: Float, y: Float) {
        var newX = x
        var newY = y
        val widthDelta = 0.5f * videoWidth * (binding.videoArea.scaleX - 1f)
        val heightDelta = 0.5f * videoHeight * (binding.videoArea.scaleY - 1f)
        val videoXMin = videoOrigx - widthDelta
        val videoXMax = videoOrigx + widthDelta
        val videoYMin = videoOrigy - heightDelta
        val videoYMax = videoOrigy + heightDelta

        if (newX < videoXMin) newX = videoXMin
        if (newX > videoXMax) newX = videoXMax
        if (newY < videoYMin) newY = videoYMin
        if (newY > videoYMax) newY = videoYMax

        if (binding.videoArea.x != newX || binding.videoArea.y != newY) {
            binding.videoArea.x = newX
            binding.videoArea.y = newY
            if (!gestureMoved) {
                gestureMoved = true
                hideControls()
            }
        }
    }

    private fun playerPause() {
        isPlaying = false
        if (isPrepared) ijkPlayer.pause()
        // 这里不需要pause()，实时同步弹幕时间之后，暂停视频弹幕会自行停住不动，pause()反而会导致弹幕卡住，无法通过start()重新滚动
        // if (hasDanmaku) mDanmakuView.pause();
        binding.buttonVideo.setImageResource(R.drawable.btn_player_play)
    }

    private fun playerResume() {
        isPlaying = true
        if (isPrepared) {
            ijkPlayer.start()
        }
        binding.buttonVideo.setImageResource(R.drawable.btn_player_pause)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (!getBoolean("player_background", false)) {
            playerPause()
        }
    }

    var liveWebSocket: WebSocket? = null

    override fun onDestroy() {
        if (eventBusInit) {
            EventBus.getDefault().unregister(this)
            eventBusInit = false
        }
        destroyed = true

        cancelAllTimers()

        binding.svDanmaku.release()
        ijkPlayer.release()

        if (danmakuFile != null && danmakuFile!!.exists()) danmakuFile!!.delete()

        if (liveWebSocket != null) {
            liveWebSocket!!.close(1000, "")
            liveWebSocket = null
        }

        requestedOrientation = if (getBoolean(
                "ui_landscape",
                false
            )
        ) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        super.onDestroy()
    }

    private fun cancelAllTimers() {
        if (surfaceTimer != null) {
            surfaceTimer!!.cancel()
            surfaceTimer = null
        }
        if (autoHideTimer != null) {
            autoHideTimer!!.cancel()
            autoHideTimer = null
        }
        if (volumeTimer != null) {
            volumeTimer!!.cancel()
            volumeTimer = null
        }
        if (progressTimer != null) {
            progressTimer!!.cancel()
            progressTimer = null
        }
        if (onlineTimer != null) {
            onlineTimer!!.cancel()
            onlineTimer = null
        }
        if (loadingTimer != null) {
            loadingTimer!!.cancel()
            loadingTimer = null
        }
    }

    var okHttpClient: OkHttpClient? = null

    private fun danmuSocketConnect() {
        ThreadManager.run {
            try {
                var url =
                    "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?type=0&id=$aid"
                val mHeaders = mapOf(
                    "Cookie" to getString(Preferences.COOKIES, ""),
                    "Referer" to "https://live.bilibili.com/$aid",
                    "Origin" to "https://live.bilibili.com",
                    "User-Agent" to HeaderValues.USER_AGENT_VAL
                )
                val response = NetWorkUtil.getOkHttpInstance().newCall(
                    Request.Builder()
                        .get()
                        .url(url).apply {
                            mHeaders.forEach { (key, value) ->
                                addHeader(key, value)
                            }
                        }
                        .build()
                ).execute()
                val data = JSONObject(
                    Objects.requireNonNull(response.body).string()
                ).getJSONObject("data")
                val host = data.getJSONArray("host_list").getJSONObject(0)

                url = "wss://" + host.getString("host") + ":" + host.getInt("wss_port") + "/sub"

                okHttpClient = OkHttpClient()
                val request: Request = Request.Builder()
                    .url(url)
                    .header(
                        "Cookie",
                        getString(Preferences.COOKIES, "")
                    )
                    .header("Origin", "https://live.bilibili.com")
                    .header("User-Agent", NetWorkUtil.USER_AGENT_WEB)
                    .build()

                val listener = PlayerDanmuClientListener()
                listener.mid = mid
                listener.roomid = aid
                listener.key = data.getString("token")
                listener.playerActivity = this

                liveWebSocket = okHttpClient!!.newWebSocket(request, listener)
            } catch (e: Exception) {
                MsgUtil.showMsg(getString(R.string.live_danmaku_connection_failed))
                e.printStackTrace()
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isPrepared) when (keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> controlVideo()
            KeyEvent.KEYCODE_DPAD_LEFT -> ijkPlayer.seekTo(ijkPlayer.currentPosition - 10000L)
            KeyEvent.KEYCODE_DPAD_RIGHT -> ijkPlayer.seekTo(ijkPlayer.currentPosition + 10000L)
            KeyEvent.KEYCODE_DPAD_UP -> changeVolume(true)
            KeyEvent.KEYCODE_DPAD_DOWN -> changeVolume(false)
        }
        return super.onKeyDown(keyCode, event)
    }

    private var eventBusInit = false

    override fun onStart() {
        super.onStart()
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this)
            eventBusInit = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(event: SnackEvent) {
        if (isFinishing) return
        MsgUtil.toast(event.message)
    }

    protected fun eventBusEnabled(): Boolean {
        return getBoolean(Preferences.SNACKBAR_ENABLE, true)
    }

    override fun finish() {
        if (isPlaying) playerPause()
        if (videoNow != 0) {
            val result = Intent()
            result.putExtra("progress", videoNow)
            setResult(RESULT_OK, result)
        } else setResult(RESULT_CANCELED)
        super.finish()
    }

    companion object {
        fun decompress(data: ByteArray): ByteArray {
            var output: ByteArray
            val decompresser = Inflater(true)
            decompresser.reset()
            decompresser.setInput(data)
            val o = ByteArrayOutputStream(data.size)
            try {
                val buf = ByteArray(2048)
                while (!decompresser.finished()) {
                    val i = decompresser.inflate(buf)
                    o.write(buf, 0, i)
                }
                output = o.toByteArray()
            } catch (e: Exception) {
                output = data
                e.printStackTrace()
            } finally {
                try {
                    o.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            decompresser.end()
            return output
        }
    }
}