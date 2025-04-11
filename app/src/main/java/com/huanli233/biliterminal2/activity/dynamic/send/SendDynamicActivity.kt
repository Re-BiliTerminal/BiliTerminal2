package com.huanli233.biliterminal2.activity.dynamic.send

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.EmoteActivity
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.adapter.video.VideoCardHolder
import com.huanli233.biliterminal2.api.EmoteApi
import com.huanli233.biliterminal2.databinding.ActivitySendDynamicBinding
import com.huanli233.biliterminal2.bean.VideoCard
import com.huanli233.biliterminal2.bean.toVideoCard
import com.huanli233.biliterminal2.util.EmoteUtil
import com.huanli233.biliterminal2.util.GlideUtil
import com.huanli233.biliterminal2.util.GlideUtil.load
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.TerminalContext
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.view.AsyncLayoutInflaterX
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.biliwebapi.bean.opus.DynamicOpus
import com.huanli233.biliwebapi.bean.video.VideoInfo
import java.util.concurrent.ExecutionException

class SendDynamicActivity : BaseActivity() {

    private lateinit var binding: ActivitySendDynamicBinding
    private lateinit var editText: EditText

    private val emoteLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("text")?.let { editText.append(it) }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AsyncLayoutInflaterX(this).inflate(R.layout.activity_send_dynamic) { layoutView, _, _ ->
            setContentView(layoutView)
            setupViews(layoutView)
            checkLoginStatus()
            handleForwardContent()
        }
    }

    private fun setupViews(layoutView: View) {
        binding = ActivitySendDynamicBinding.bind(layoutView)
        setTopbarExit()

        with(binding) {
            setupSendButton()
            setupEmoteButton()
        }
    }

    private fun checkLoginStatus() {
        if (Preferences.getLong(Preferences.MID, 0) == 0L) {
            setResult(RESULT_CANCELED)
            finish()
            MsgUtil.showMsg("还没有登录喵~")
        }
    }

    private fun handleForwardContent() {
        val forwardContent = TerminalContext.getInstance().forwardContent
        when (forwardContent) {
            is VideoInfo -> setupVideoContent(forwardContent)
            is Dynamic -> setupDynamicContent(forwardContent)
        }
    }

    private fun setupVideoContent(video: VideoInfo) {
        VideoCardHolder(binding.extraCard.inflateVideoCard()).bindData(video.toVideoCard(), this)
    }

    private fun ConstraintLayout.inflateVideoCard(): View =
        LayoutInflater.from(this@SendDynamicActivity).inflate(R.layout.cell_video_list, this, false)

    private fun setupDynamicContent(dynamic: Dynamic) {
        val childView = LayoutInflater.from(this).inflate(R.layout.cell_dynamic_child, binding.extraCard)
        showChildDyn(childView, dynamic)
    }

    private fun setupSendButton() {
        binding.send.setOnClickListener {
            if (Preferences.getBoolean(Preferences.COOKIE_REFRESH, true)) {
                handleSuccessfulSend()
            } else {
                showCookieRefreshWarning()
            }
        }
    }

    private fun handleSuccessfulSend() {
        Intent().apply {
            putExtras(intent.extras ?: Bundle())
            putExtra("text", editText.text.toString())
            setResult(RESULT_OK, this)
        }
        finish()
    }

    private fun showCookieRefreshWarning() {
        MsgUtil.showDialog(
            "无法发送",
            "上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作",
            -1
        )
    }

    private fun setupEmoteButton() {
        binding.emote.setOnClickListener {
            emoteLauncher.launch(
                Intent(this, EmoteActivity::class.java).apply {
                    putExtra("from", EmoteApi.BUSINESS_DYNAMIC)
                }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showChildDyn(itemView: View, dynamic: Dynamic) {
        with(itemView) {
            val username = findViewById<TextView>(R.id.child_username)
            val content = findViewById<TextView>(R.id.child_content)
            val avatar = findViewById<ImageView>(R.id.child_avatar)
            val extraCard = findViewById<LinearLayout>(R.id.child_extraCard)

            bindUserInfo(dynamic, username, avatar)
            bindDynamicContent(dynamic, content)
            bindMajorContent(dynamic, extraCard)
        }
    }

    private fun bindUserInfo(dynamic: Dynamic, username: TextView, avatar: ImageView) {
        username.text = dynamic.modules.moduleAuthor.name
        loadAvatar(dynamic.modules.moduleAuthor.face, avatar)
        setupAvatarClickHandler(dynamic.modules.moduleAuthor.mid, avatar)
    }

    private fun loadAvatar(avatarUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(GlideUtil.url(avatarUrl))
            .transition(GlideUtil.transitionOptions)
            .placeholder(R.mipmap.akari)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    private fun setupAvatarClickHandler(mid: Long, avatar: ImageView) {
        avatar.setOnClickListener {
            startActivity(
                Intent(this, UserInfoActivity::class.java).apply {
                    putExtra("mid", mid)
                }
            )
        }
    }

    private fun bindDynamicContent(dynamic: Dynamic, content: TextView) {
        content.apply {
            val dynamicDesc = dynamic.modules.moduleDynamic.desc ?: return
            val dynamicContent = dynamicDesc.content
            isVisible = dynamicContent.text.isNotEmpty()
            text = dynamicContent.text
            processEmotes(dynamicContent.emotes, content)
            Utils.setLink(this)
            dynamicContent.ats.let { Utils.setMentionLink(it, this) }
            maxLines = 999
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private fun processEmotes(emotes: List<EmoteContent>, content: TextView) {
        ThreadManager.run {
            try {
                val spannableString = EmoteUtil.textReplaceEmote(
                    content.text.toString(),
                    emotes.associateBy { it.text },
                    1.0f,
                    this,
                    content.text
                )
                ThreadManager.runOnUiThread {
                    content.text = spannableString
                    Utils.setLink(content)
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun bindMajorContent(dynamic: Dynamic, extraCard: LinearLayout) {
        val major = dynamic.modules.moduleDynamic.major ?: return
        val majorType = major.type

        extraCard.resetChildViews()

        when (majorType) {
            "MAJOR_TYPE_PGC", "MAJOR_TYPE_ARCHIVE", "MAJOR_TYPE_UGC_SEASON" ->
                (major.archive ?: major.ugcSeason)?.toVideoCard()
                    ?.let { setupVideoCard(it, extraCard, majorType == "MAJOR_TYPE_PGC") }

            "MAJOR_TYPE_ARTICLE" ->
                major.opus?.let { setupOpus(it, extraCard) }

            "MAJOR_TYPE_DRAW" ->
                major.opus?.pics?.map { it.url }?.let { setupImageCard(it, extraCard) }
        }
    }

    private fun LinearLayout.resetChildViews() {
        listOf<View?>(
            findViewById(R.id.dynamic_video_child),
            findViewById(R.id.dynamic_article_child),
            findViewById(R.id.dynamic_image_child)
        ).forEach { it?.isVisible = false }
    }

    private fun setupVideoCard(videoCard: VideoCard, container: LinearLayout, isPgc: Boolean) {
        container.findViewById<MaterialCardView>(R.id.dynamic_video_child).apply {
            VideoCardHolder(this).bindData(videoCard, this@SendDynamicActivity)
            setOnClickListener {
                TerminalContext.getInstance().enterVideoDetailPage(
                    this@SendDynamicActivity,
                    videoCard.aid,
                    "",
                    if (isPgc) "media" else null
                )
            }
            isVisible = true
        }
    }

    private fun setupOpus(opus: DynamicOpus, container: LinearLayout) {
//        container.findViewById<MaterialCardView>(R.id.dynamic_article_child).apply {
//            ArticleCardHolder(this).showDynamicOpus(opus, this@SendDynamicActivity)
//            setOnClickListener {
//            }
//            isVisible = true
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupImageCard(pictureList: List<String>, container: LinearLayout) {
        container.findViewById<MaterialCardView>(R.id.dynamic_image_child).apply {
            val imageView = findViewById<ImageView>(R.id.imageView)
            val textView = findViewById<TextView>(R.id.imageCount)

            imageView.load(pictureList.first())
            textView.text = getString(R.string.picture_count, pictureList.size.toString())

            isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TerminalContext.getInstance().forwardContent = null
    }
}

// ViewExtensions.kt
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}