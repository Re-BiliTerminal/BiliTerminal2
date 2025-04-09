package com.huanli233.biliterminal2.activity.video.info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.ImageViewerActivity
import com.huanli233.biliterminal2.activity.dynamic.send.SendDynamicActivity
import com.huanli233.biliterminal2.activity.search.SearchActivity
import com.huanli233.biliterminal2.activity.settings.SettingPlayerChooseActivity
import com.huanli233.biliterminal2.activity.user.WatchLaterActivity
import com.huanli233.biliterminal2.activity.video.MultiPageActivity
import com.huanli233.biliterminal2.activity.video.QualityChooserActivity
import com.huanli233.biliterminal2.activity.video.collection.CollectionInfoActivity
import com.huanli233.biliterminal2.adapter.user.StaffListAdapter
import com.huanli233.biliterminal2.api.BangumiApi
import com.huanli233.biliterminal2.api.DynamicApi
import com.huanli233.biliterminal2.api.HistoryApi
import com.huanli233.biliterminal2.api.LikeCoinFavApi
import com.huanli233.biliterminal2.api.PlayerApi
import com.huanli233.biliterminal2.api.VideoInfoApi
import com.huanli233.biliterminal2.api.WatchLaterApi
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.ui.widget.RadiusBackgroundSpan
import com.huanli233.biliterminal2.ui.widget.recycler.CustomLinearManager
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.ThreadManager.runOnUiThread
import com.huanli233.biliterminal2.util.FileUtil
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.LinkUrlUtil
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliterminal2.util.Preferences.getLong
import com.huanli233.biliterminal2.util.Preferences.getString
import com.huanli233.biliterminal2.util.Preferences.putBoolean
import com.huanli233.biliterminal2.util.TerminalContext
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.Utils.LinkClickableSpan
import com.huanli233.biliterminal2.util.extensions.appendWithSpan
import com.huanli233.biliterminal2.util.extensions.formatToDate
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.regex.Pattern

private const val RESULT_ADDED = 1
private const val RESULT_DELETED = -1

class VideoInfoFragment : Fragment() {
    private var onFinishLoad: Runnable? = null
    private var loadFinished = false

    private lateinit var videoInfo: VideoInfo

    private lateinit var description: TextView
    private lateinit var tagsText: TextView
    private lateinit var fav: ImageView
    private var progressPair: Pair<Long, Int>? = null
    private var playClicked = false

    private var coverPlayEnabled = getBoolean(Preferences.COVER_PLAY_ENABLE, false)

    private var coinAdd = 0

    private var descExpand = false
    private var tagsExpand = false
    val favLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { o ->
            val code = o.resultCode
            if (code == RESULT_ADDED) {
                fav.setImageResource(R.drawable.icon_fav_active)
            } else if (code == RESULT_DELETED) {
                fav.setImageResource(R.drawable.icon_fav_deactivate)
            }
        }

    val writeDynamicLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val code = result.resultCode
            val data = result.data
            if (code == Activity.RESULT_OK && data != null) {
                val text = data.getStringExtra("text").orEmpty()
                ThreadManager.run {
                    try {
                        val dynId: Long
                        val atUids: MutableMap<String, Long> =
                            HashMap()
                        val pattern =
                            Pattern.compile("@(\\S+)\\s")
                        val matcher = pattern.matcher(text)
                        while (matcher.find()) {
                            val matchedString = matcher.group(1)
                            var uid: Long
                            if ((DynamicApi.mentionAtFindUser(matchedString)
                                    .also { uid = it }) != -1L
                            ) {
                                atUids[matchedString.orEmpty()] = uid
                            }
                        }
                        dynId = DynamicApi.relayVideo(
                            text,
                            (if (atUids.isEmpty()) null else atUids),
                            videoInfo.aid
                        )

                        if (dynId != -1L) MsgUtil.showMsg("转发成功~")
                        else MsgUtil.showMsg("转发失败")
                    } catch (e: Exception) {
                        MsgUtil.error(e)
                    }
                }
            }
        }

    private val state = VideoInfoFragmentState()

    private data class VideoInfoFragmentState(
        var liked: Boolean = false,
        var coined: Int = 0,
        var favoured: Boolean = false,
        var likes: Int = 0,
        var coins: Int = 0,
        var favourites: Int = 0,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle == null) {
            activity?.finish()
            return
        }
        val aid = bundle.getLong("aid")
        val bvid = bundle.getString("bvid").orEmpty()
        lifecycleScope.launch {
            bilibiliApi.api(IVideoApi::class) {
                getVideoInfo(aid, bvid)
            }.apiResultNonNull().onSuccess { data ->
                videoInfo = data
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_info, container, false)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(rootview: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootview, savedInstanceState)

        if (getBoolean("ui_landscape", false)) {
            val windowManager =
                rootview.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics)
            else display.getMetrics(metrics)
            val paddings = metrics.widthPixels / 6
            rootview.setPadding(paddings, 0, paddings, 0)
        }

        val cover = rootview.findViewById<ImageView>(R.id.img_cover)
        val title = rootview.findViewById<TextView>(R.id.text_title)
        description = rootview.findViewById(R.id.description)
        tagsText = rootview.findViewById(R.id.tags)
        val exclusiveTip = rootview.findViewById<MaterialCardView>(R.id.exclusiveTip)
        val upRecyclerview = rootview.findViewById<RecyclerView>(R.id.uploader_list)
        val exclusiveTipLabel = rootview.findViewById<TextView>(R.id.exclusiveTipLabel)
        val viewCount = rootview.findViewById<TextView>(R.id.views_count)
        val timeText = rootview.findViewById<TextView>(R.id.time_text)
        val durationText = rootview.findViewById<TextView>(R.id.durationText)
        val play = rootview.findViewById<MaterialButton>(R.id.play)
        val addWatchlater = rootview.findViewById<MaterialButton>(R.id.addWatchlater)
        val download = rootview.findViewById<MaterialButton>(R.id.download)
        val relay = rootview.findViewById<MaterialButton>(R.id.relay)
        val bvidText = rootview.findViewById<TextView>(R.id.bvidText)
        val danmakuCount = rootview.findViewById<TextView>(R.id.danmakuCount)
        val like = rootview.findViewById<ImageView>(R.id.btn_like)
        val coin = rootview.findViewById<ImageView>(R.id.btn_coin)
        fav = rootview.findViewById(R.id.btn_fav)
        val likeLabel = rootview.findViewById<TextView>(R.id.like_label)
        val coinLabel = rootview.findViewById<TextView>(R.id.coin_label)
        val favLabel = rootview.findViewById<TextView>(R.id.fav_label)
        val collectionCard = rootview.findViewById<MaterialCardView>(R.id.collection)

        rootview.visibility = View.GONE
        if (onFinishLoad != null) onFinishLoad!!.run()
        else loadFinished = true

        videoInfo.redirectUrl?.let { redirectUrl ->
            if (redirectUrl.contains("bangumi")) {
                redirectUrl.replace("https://www.bilibili.com/bangumi/play/ep", "").toLongOrNull()?.let {
                    ThreadManager.run {
                        val context = context ?: return@run
                        TerminalContext.getInstance()
                            .enterVideoDetailPage(
                                context,
                                BangumiApi.getMdidFromEpid(it),
                                null,
                                "media"
                            )
                        val activity: FragmentActivity = activity ?: return@run
                        runOnUiThread { activity.finish() }
                    }
                }
            }
        }

        if (!getBoolean("tags_enable", true)) tagsText.visibility = View.GONE

        ThreadManager.run {
            // 历史上报
            try {
                progressPair = VideoInfoApi.getWatchProgress(videoInfo.aid).let {
                    if (it.first == null || !videoInfo.pages.any { page -> page.cid == it.first })
                        Pair(videoInfo.pages[0].cid, 0)
                    else it
                }

                HistoryApi.reportHistory(
                    videoInfo.aid,
                    progressPair!!.first!!, videoInfo.staff[0].mid, progressPair!!.second.toLong()
                )
            } catch (e: Exception) {
                MsgUtil.error(e)
                progressPair = Pair(0L, 0)
            }

            // 标签显示
            if (getBoolean("tags_enable", true)) {
                ThreadManager.run {
                    try {
                        val tagsSpannable = getDescSpan(VideoInfoApi.getTagsByAid(videoInfo.aid))

                        if (isAdded) requireActivity().runOnUiThread {
                            tagsText.movementMethod = LinkMovementMethod.getInstance()
                            tagsText.text = tagsSpannable.toString()
                            tagsText.setOnClickListener {
                                tagsExpand = !tagsExpand
                                if (tagsExpand) {
                                    tagsText.setMaxLines(233)
                                    tagsText.text = tagsSpannable
                                } else {
                                    tagsText.setMaxLines(1)
                                    tagsText.text = tagsSpannable.toString()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        MsgUtil.error(e)
                    }
                }
            }

            // 点赞投币收藏
            ThreadManager.run {
                try {
//                    videoInfo.stat.coined = LikeCoinFavApi.getCoined(videoInfo.aid)
//                    videoInfo.stat.liked = LikeCoinFavApi.getLiked(videoInfo.aid)
//                    videoInfo.stats.favoured = LikeCoinFavApi.getFavoured(videoInfo.aid)
//                    videoInfo.stats.allow_coin =
//                        if ((videoInfo.copyright == VideoInfo.COPYRIGHT_REPRINT)) 1 else 2
//                    if (isAdded) requireActivity().runOnUiThread {
//                        if (videoInfo.stats.coined != 0) coin.setImageResource(R.drawable.icon_coin_1)
//                        if (videoInfo.stats.liked) like.setImageResource(R.drawable.icon_like_1)
//                        if (videoInfo.stats.favoured) fav.setImageResource(R.drawable.icon_fav_1)
//                    }
                } catch (e: Exception) {
                    MsgUtil.error(e)
                }
            }
            if (isAdded) requireActivity().runOnUiThread {
                // 标题
                title.text = titleSpan
                Utils.copyable(title, videoInfo.title)

                // 争议信息
                videoInfo.argueInfo.argueMsg.let { argueMsg ->
                    if (argueMsg.isNotEmpty()) {
                        exclusiveTipLabel.text = argueMsg
                        exclusiveTip.visibility = View.VISIBLE
                    }
                }

                // UP主列表
                val adapter =
                    StaffListAdapter(
                        requireContext(),
                        videoInfo.staff
                    )
                upRecyclerview.setHasFixedSize(true)
                upRecyclerview.layoutManager = CustomLinearManager(context)
                upRecyclerview.adapter = adapter


                // 封面
                cover.loadPicture(videoInfo.pic, setClick = true)
                cover.requestFocus()
                cover.setOnClickListener {
                    if (getString(Preferences.PLAYER, "").isEmpty()) {
                        putBoolean(Preferences.COVER_PLAY_ENABLE, true)
                        Toast.makeText(
                            requireContext(),
                            "将播放视频！\n如需变更点击行为请至设置->偏好设置喵",
                            Toast.LENGTH_SHORT
                        ).show()
                        coverPlayEnabled = true
                    }
                    if (coverPlayEnabled) {
                        playClick()
                        return@setOnClickListener
                    }
                    showCover()
                }
                if (coverPlayEnabled) cover.setOnLongClickListener {
                    showCover()
                    true
                }

                viewCount.text = Utils.toWan(videoInfo.stat.view.toLong())
                likeLabel.text = Utils.toWan(videoInfo.stat.like.toLong())
                coinLabel.text = Utils.toWan(videoInfo.stat.coin.toLong())
                favLabel.text = Utils.toWan(videoInfo.stat.favorite.toLong())

                danmakuCount.text = String.format(Locale.getDefault(), "%d", videoInfo.stat.danmaku)
                bvidText.text = videoInfo.bvid
                timeText.text = videoInfo.pubDate.formatToDate()
                durationText.text = String.format(Locale.getDefault(), "%d", videoInfo.duration)

                description.text = buildSpannedString {
                    videoInfo.descV2.forEach {
                        if (it.type == 2 && it.bizId != 0) {
                            appendWithSpan("@${it.rawText} ") { text -> LinkClickableSpan(text, LinkUrlUtil.TYPE_USER) }
                        } else append(it.rawText)
                    }
                }
                description.setOnClickListener {
                    if (descExpand) description.setMaxLines(3)
                    else description.setMaxLines(512)
                    descExpand = !descExpand
                }
                Utils.setLink(description)
                Utils.copyable(description)

                bvidText.setOnLongClickListener {
                    val context = context ?: return@setOnLongClickListener true
                    Utils.copyText(context, videoInfo.bvid)
                    MsgUtil.showMsg("BV号已复制")
                    true
                }

                play.setOnClickListener { playClick() }
                play.setOnLongClickListener {
                    val intent = Intent()
                    val context = context
                    if (context != null) {
                        intent.setClass(context, SettingPlayerChooseActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }

                // 点赞
                rootview.findViewById<View>(R.id.layout_like).setOnClickListener {
                    ThreadManager.run thread@{
                        if (getLong(Preferences.MID, 0) == 0L) {
                            MsgUtil.showMsg("还没有登录喵~")
                            return@thread
                        }
                        try {
                            val result = LikeCoinFavApi.like(
                                videoInfo.aid,
                                (if (state.liked) 2 else 1)
                            )
                            if (result == 0) {
                                state.liked = !state.liked
                                if (isAdded) runOnUiThread {
                                    MsgUtil.showMsg(if (state.liked) "点赞成功" else "取消成功")
                                    if (state.liked) likeLabel.text =
                                        Utils.toWan((++state.likes).toLong())
                                    else likeLabel.text =
                                        Utils.toWan((--state.likes).toLong())
                                    like.setImageResource(if (state.liked) R.drawable.icon_like_active else R.drawable.icon_like_deactivate)
                                }
                            } else if (isAdded) {
                                var msg = "操作失败：$result"
                                if (result == -403) {
                                    msg = "当前请求触发B站风控"
                                }
                                val finalMsg = msg
                                MsgUtil.showMsg(finalMsg)
                            }
                        } catch (e: Exception) {
                            MsgUtil.error(e)
                        }
                    }
                }

                rootview.findViewById<View>(R.id.layout_coin).setOnClickListener {
                    ThreadManager.run thread@{
                        if (getLong(Preferences.MID, 0) == 0L) {
                            MsgUtil.showMsg("还没有登录喵~")
                            return@thread
                        }
                        if (state.coined < videoInfo.coinLimit) {
                            try {
                                val result = LikeCoinFavApi.coin(videoInfo.aid, 1)
                                if (result == 0) {
                                    if (++coinAdd <= 2) state.coins++
                                    if (isAdded) requireActivity().runOnUiThread {
                                        MsgUtil.showMsg("投币成功")
                                        coinLabel.text =
                                            Utils.toWan((++state.coins).toLong())
                                        coin.setImageResource(R.drawable.icon_coin_active)
                                    }
                                } else if (isAdded) {
                                    var msg = "投币失败：$result"
                                    if (result == -403) {
                                        msg = "当前请求触发B站风控"
                                    } else if (result == 34002) {
                                        msg = "不能给自己投币哦"
                                    }
                                    val finalMsg = msg
                                    MsgUtil.showMsg(finalMsg)
                                }
                            } catch (e: Exception) {
                                MsgUtil.error(e)
                            }
                        } else {
                            MsgUtil.showMsg("投币数量到达上限")
                        }
                    }
                }

                // 收藏
                rootview.findViewById<View>(R.id.layout_fav).setOnClickListener {
                    val intent = Intent()
                    intent.setClass(requireContext(), AddFavoriteActivity::class.java)
                    intent.putExtra("aid", videoInfo.aid)
                    intent.putExtra("bvid", videoInfo.bvid)
                    favLauncher.launch(intent)
                }

                // 稍后再看
                addWatchlater.setOnClickListener {
                    ThreadManager.run {
                        try {
                            val result = WatchLaterApi.add(videoInfo.aid)
                            if (result == 0) MsgUtil.showMsg("添加成功")
                            else MsgUtil.showMsg("添加失败，错误码：$result")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                addWatchlater.setOnLongClickListener {
                    val intent = Intent()
                    intent.setClass(requireContext(), WatchLaterActivity::class.java)
                    startActivity(intent)
                    true
                }

                // 下载
                download.setOnClickListener { downloadClick() }
                download.setOnLongClickListener {
                    ThreadManager.run {
                        val downPath = FileUtil.getDownloadPath(
                            videoInfo.title, null
                        )
                        FileUtil.deleteFolder(downPath)
                        MsgUtil.showMsg("已清除此视频的缓存文件夹")
                    }
                    true
                }

                // 转发
                relay.setOnClickListener {
                    val intent = Intent()
                    intent.setClass(requireContext(), SendDynamicActivity::class.java)
                    writeDynamicLauncher.launch(intent)
                }
                relay.setOnLongClickListener {
                    Utils.copyText(
                        requireContext(),
                        "https://www.bilibili.com/" + videoInfo.bvid
                    )
                    MsgUtil.showMsg("视频完整链接已复制")
                    true
                }

                // 未登录隐藏按钮
                if (getLong(Preferences.MID, 0) == 0L) {
                    addWatchlater.visibility = View.GONE
                    relay.visibility = View.GONE
                }

                // 合集按钮
                videoInfo.ugcSeason?.let {
                    val collectionTitle = rootview.findViewById<TextView>(R.id.collectionText)
                    collectionTitle.text = String.format("合集 · %s", it.title)
                    collectionCard.setOnClickListener {
                        startActivity(
                            Intent(
                                requireContext(),
                                CollectionInfoActivity::class.java
                            )
                                .putExtra("fromVideo", videoInfo.aid)
                        )
                    }
                } ?: let { collectionCard.visibility = View.GONE }
            }
        }
    }


    private val titleSpan: SpannableString
        get() {
            var string = ""

            if (videoInfo.isUpowerExclusive) string = "充电专属"
            else if (videoInfo.rights.isSteinGate == 1) string = "互动视频"
            else if (videoInfo.rights.is360 == 1) string = "全景视频"
            else if (videoInfo.rights.isCooperation == 1) string = "联合投稿"

            if (string.isEmpty()) return SpannableString(videoInfo.title)

            val titleStr = SpannableString(" " + string + " " + videoInfo.title)
            val badgeBG = RadiusBackgroundSpan(
                0,
                resources.getDimension(R.dimen.card_round).toInt(),
                Color.WHITE,
                Color.rgb(207, 75, 95)
            )
            titleStr.setSpan(badgeBG, 0, string.length + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return titleStr
        }

    private fun getDescSpan(tags: String): SpannableStringBuilder {
        val tagStr = SpannableStringBuilder("标签：")
        for (str in tags.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val oldLen = tagStr.length
            tagStr.append(str).append("/")
            tagStr.setSpan(object : ClickableSpan() {
                override fun onClick(arg0: View) {
                    val intent = Intent(
                        requireContext(),
                        SearchActivity::class.java
                    )
                    intent.putExtra("keyword", str)
                    requireContext().startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = Color.parseColor("#03a9f4")
                }
            }, oldLen, tagStr.length - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return tagStr
    }

    private fun playClick() {
        Glide.get(requireContext()).clearMemory()
        // 在播放前清除内存缓存，因为手表内存太小了，播放完回来经常把Activity全释放掉
        // ...经过测试，还是会释放，但会好很多
        if (videoInfo.pages.size > 1) {
            val intent = Intent()
                .setClass(requireContext(), MultiPageActivity::class.java)
                .putExtra("progress_cid", progressPair!!.first)
                .putExtra("progress", (if (playClicked) -1 else progressPair!!.second))
                .putExtra("aid", videoInfo.aid)
                .putExtra("bvid", videoInfo.bvid)
            // 这里也会传过去，如果后面选择当页就不再获取直接传，选择其他页就传-1剩下的交给解析页
            startActivity(intent)
        } else {
            PlayerApi.startGettingUrl(
                requireContext(),
                videoInfo,
                0,
                (if (progressPair == null) 0 else if (playClicked) -1 else progressPair!!.second)
            )
            // 避免重复获取的同时保证播放进度是新的，如果是-1会在解析页里再获取一次
        }
        playClicked = true
    }

    private fun downloadClick() {
        if (!FileUtil.checkStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                FileUtil.requestStoragePermission(requireActivity())
            }
        } else {
            val downPath = FileUtil.getDownloadPath(
                videoInfo.title, null
            )

            if (downPath.exists() && videoInfo.pages.size == 1) {
                val fileSign = File(downPath, ".DOWNLOADING")
                MsgUtil.showMsg(if (fileSign.exists()) "已在下载队列" else "已下载完成")
            } else {
                if (videoInfo.pages.size > 1) {
                    val intent = Intent()
                    intent.setClass(requireContext(), MultiPageActivity::class.java)
                        .putExtra("download", 1)
                        .putExtra("aid", videoInfo.aid)
                        .putExtra("bvid", videoInfo.bvid)
                    startActivity(intent)
                } else {
                    startActivity(
                        Intent(requireContext(), QualityChooserActivity::class.java)
                            .putExtra("page", 0)
                            .putExtra("aid", videoInfo.aid)
                            .putExtra("bvid", videoInfo.bvid)
                    )
                }
            }
        }
    }

    private fun showCover() {
        try {
            val intent = Intent()
            intent.setClass(requireContext(), ImageViewerActivity::class.java)
            val imageList = ArrayList<String>()
            imageList.add(videoInfo.pic)
            intent.putExtra("imageList", imageList)
            requireContext().startActivity(intent)
        } catch (ignored: Exception) {
        }
    }


    fun setOnFinishLoad(onFinishLoad: Runnable) {
        if (loadFinished) onFinishLoad.run()
        else this.onFinishLoad = onFinishLoad
    }

    companion object {
        @JvmStatic
        fun newInstance(aid: Long, bvid: String?): VideoInfoFragment {
            val args = Bundle()
            args.putLong("aid", aid)
            args.putString("bvid", bvid)
            val fragment = VideoInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}