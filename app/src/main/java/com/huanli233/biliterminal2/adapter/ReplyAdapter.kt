package com.huanli233.biliterminal2.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.ImageViewerActivity
import com.huanli233.biliterminal2.activity.reply.ReplyInfoActivity
import com.huanli233.biliterminal2.activity.reply.WriteReplyActivity
import com.huanli233.biliterminal2.api.ReplyApi
import com.huanli233.biliterminal2.api.apiResult
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.onApiFailure
import com.huanli233.biliterminal2.api.toResult
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.databinding.CellReplyActionBinding
import com.huanli233.biliterminal2.databinding.CellReplyListBinding
import com.huanli233.biliterminal2.ui.widget.RadiusBackgroundSpan
import com.huanli233.biliterminal2.util.EmoteUtil
import com.huanli233.biliterminal2.util.GlideUtil.loadFace
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.extensions.addReturning
import com.huanli233.biliterminal2.util.extensions.formatNumber
import com.huanli233.biliterminal2.util.extensions.formatToDate
import com.huanli233.biliterminal2.util.extensions.msg
import com.huanli233.biliterminal2.util.htmlToString
import com.huanli233.biliterminal2.util.requireLoggedIn
import com.huanli233.biliterminal2.util.runOnUi
import com.huanli233.biliwebapi.api.interfaces.IReplyApi
import com.huanli233.biliwebapi.bean.content.EmoteContent
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.biliwebapi.bean.user.UserInfo
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Locale

class ReplyAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val replyList: MutableList<Reply>,
    private val oid: Long,
    private val root: Long,
    private val type: Int,
    var sort: Int,
    private val exitDetail: () -> Unit,
    var source: Any? = null,
    private val upMid: Long = -1
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isDetail = false
    var isManager = false
    private val stateList = mutableListOf<ReplyState>()
    var onSortChangeListener: ((Int) -> Unit)? = null
    private val sorts = listOf("未知排序", "未知排序", "时间排序", "热度排序")

    init {
        checkManagerPermission()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> WriteReplyViewHolder(
                CellReplyActionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ReplyViewHolder(
                CellReplyListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WriteReplyViewHolder -> bindWriteReply(holder.binding, position)
            is ReplyViewHolder -> bindReply(holder.binding, position)
        }
    }

    private fun bindWriteReply(binding: CellReplyActionBinding, position: Int) {
        binding.writeReply.setOnClickListener {
            context.startActivity(Intent(context, WriteReplyActivity::class.java).apply {
                putExtra("oid", oid)
                putExtra("rpid", root)
                putExtra("parent", root)
                putExtra("parentSender", "")
                putExtra("replyType", type)
            })
        }

        if (isDetail) {
            binding.sort.visibility = ViewGroup.GONE
            binding.countLabel.visibility = ViewGroup.GONE
        } else {
            with(binding) {
                sort.text = sorts[this@ReplyAdapter.sort]
                sort.setOnClickListener {
                    onSortChangeListener?.invoke(0)
                    sort.text = sorts[this@ReplyAdapter.sort]
                }

                lifecycleOwner.lifecycleScope.launch {
                    bilibiliApi.api(IReplyApi::class.java) { getReplyCount(oid, type) }
                        .apiResultNonNull().onSuccess {
                            runOnUi {
                                countLabel.text = context.getString(R.string.reply_count, it.count.toString())
                            }
                        }
                }
                ThreadManager.run {
                    try {


                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindReply(binding: CellReplyListBinding, position: Int) {
        val realPosition = calculateRealPosition(position)
        val reply = replyList[realPosition]

        with(binding) {
            loadAvatar(reply)
            setupUserName(reply)
            setupMessage(reply)
            setupLikes(reply)
            setupChildReplies(reply, realPosition)
            setupImages(reply)
            setupInteraction(reply, realPosition)
        }
    }

    private fun calculateRealPosition(position: Int): Int {
        return if (isDetail) if (position != 0) position - 1 else 0 else position - 1
    }

    private fun CellReplyListBinding.loadAvatar(reply: Reply) {
        replyAvatar.loadFace(reply.member.face, reply.mid)
    }

    private fun CellReplyListBinding.setupUserName(reply: Reply) {
        val sender = reply.member
        val nameBuilder = SpannableStringBuilder(sender.name).apply {
            applyVipColor(sender)
            appendUpBadge(sender)
            appendLevel(sender)
            appendMedal(sender)
        }

        replyUsername.apply {
            text = nameBuilder
            marqueeSettings()
        }
    }

    private fun SpannableStringBuilder.applyVipColor(sender: UserInfo) {
        if (!TextUtils.isEmpty(sender.vip.nicknameColor) && !Preferences.getBoolean(
                Preferences.NO_VIP_COLOR,
                false
            )
        ) {
            setSpan(
                ForegroundColorSpan(Color.parseColor(sender.vip.nicknameColor)),
                0,
                length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun SpannableStringBuilder.appendUpBadge(sender: UserInfo) {
        if (sender.mid == upMid) {
            insert(0, " UP ").apply {
                setSpan(
                    RadiusBackgroundSpan(
                        2,
                        context.resources.getDimension(R.dimen.round_small).toInt(),
                        Color.WHITE,
                        Color.rgb(207, 75, 95)
                    ),
                    0,
                    4,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                setSpan(RelativeSizeSpan(0.8f), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }

    private fun SpannableStringBuilder.appendLevel(sender: UserInfo) {
        val start = length
        append(" ${sender.level}${if (sender.isSeniorMember == 1) "+" else ""}")
        setSpan(
            Utils.getLevelBadge(context, sender),
            start + 1,
            length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
    }

    private fun SpannableStringBuilder.appendMedal(sender: UserInfo) {
        if (!TextUtils.isEmpty(sender.fansMedal?.medal?.medalName)) {
            val start = length
            append("  ${sender.fansMedal?.medal?.medalName}Lv${sender.fansMedal?.medal?.level} ")
            setSpan(
                RadiusBackgroundSpan(
                    2,
                    context.resources.getDimension(R.dimen.round_small).toInt(),
                    Color.WHITE,
                    Color.argb(140, 158, 186, 232)
                ),
                start + 1,
                length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(RelativeSizeSpan(0.8f), start + 1, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
    }

    private fun TextView.marqueeSettings() {
        if (Preferences.getBoolean(Preferences.REPLY_MARQUEE_NAME, false)) {
            setSingleLine(true)
            maxLines = 1
        } else {
            setSingleLine(false)
            maxLines = 3
        }
    }

    private fun CellReplyListBinding.setupMessage(reply: Reply) {
        val message = reply.content.message.htmlToString()
        replyText.text = message
        Utils.copyable(replyText)
        reply.content.emote?.let { processEmotes(reply, it, message) } ?: setTopSpan(reply)
        Utils.setLink(replyText)
        Utils.setMentionLink(reply.content.atNameToMid, replyText)
    }

    private fun CellReplyListBinding.processEmotes(reply: Reply, emotes: Map<String, EmoteContent>, text: String) {
        ThreadManager.run {
            try {
                val spannable = EmoteUtil.textReplaceEmote(
                    text = text,
                    emotes = emotes,
                    scale = 1.0f,
                    context = context,
                    source = replyText.text
                )
                runOnUi {
                    replyText.text = spannable
                    setTopSpan(reply)
                    Utils.setLink(replyText)
                    Utils.setMentionLink(reply.content.atNameToMid, replyText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun CellReplyListBinding.setTopSpan(reply: Reply) {
        if (reply.replyTipControl.isUpTop) {
            SpannableString(replyText.text).apply {
                setSpan(
                    ForegroundColorSpan(Color.rgb(207, 75, 95)),
                    0,
                    ReplyApi.TOP_TIP.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                replyText.text = this
            }
        }
    }

    private fun CellReplyListBinding.setupLikes(reply: Reply) {
        likes.text = reply.like.formatNumber()
        if (reply.actionState == 1) {
            likes.setTextColor(Color.rgb(0xFE, 0x67, 0x9A))
            likes.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.icon_reply_like1),
                null,
                null,
                null
            )
        } else {
            likes.setTextColor(Color.WHITE)
            likes.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.icon_reply_like0),
                null,
                null,
                null
            )
        }

        likes.setOnClickListener { handleLikeClick(reply) }
    }

    private fun CellReplyListBinding.handleLikeClick(reply: Reply) {
        val replyState = stateList.addReturning(
            ReplyState(
                liked = reply.actionState == 1,
                likeCount = reply.like
            )
        )
        lifecycleOwner.lifecycleScope.launch {
            requireLoggedIn {
                bilibiliApi.api(IReplyApi::class) {
                    likeReply(
                        oid = oid,
                        replyId = reply.replyId,
                        action = if (reply.actionState == 0) 1 else 0
                    )
                }.apiResult().onSuccess {
                    replyState.liked = !replyState.liked
                    replyState.likeCount += if (replyState.liked) 1 else -1
                    runOnUi {
                        likes.text = String.format(Locale.getDefault(), "%d", replyState.likeCount.toString())
                        val (colorRes, drawableRes) = if (replyState.liked) {
                            Color.rgb(0xFE, 0x67, 0x9A) to R.drawable.icon_reply_like1
                        } else {
                            Color.WHITE to R.drawable.icon_reply_like0
                        }
                        likes.setTextColor(colorRes)
                        likes.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, drawableRes),
                            null,
                            null,
                            null
                        )
                        MsgUtil.showMsg(
                            if (replyState.liked) context.getString(R.string.like_success)
                            else context.getString(R.string.cancel_success)
                        )
                    }
                }.onFailure {
                    MsgUtil.showMsg(
                        if (replyState.liked) context.getString(R.string.cancel_failed)
                        else context.getString(R.string.like_failed)
                    )
                }
            }
        }
    }

    private fun CellReplyListBinding.setupChildReplies(reply: Reply, realPosition: Int) {
        if (reply.rcount != 0 && !(isDetail && realPosition == 0)) {
            repliesCard.visibility = View.VISIBLE
            repliesControl.text = if (reply.upAction.like) context.getString(
                R.string.reply_count_with_creator,
                reply.rcount.toString()
            )
            else context.getString(R.string.reply_count_total, reply.rcount.toString())

            reply.replies?.let { setupChildMessages(it) }
        } else {
            repliesCard.visibility = View.GONE
        }

        repliesCard.setOnClickListener { startReplyInfoActivity(reply) }
        repliesList.setOnItemClickListener { _, _, position, _ ->
            startReplyInfoActivity(reply.replies?.get(position) ?: reply)
        }
    }

    private fun CellReplyListBinding.setupChildMessages(replies: List<Reply>) {
        val upTip = "  UP  "
        val items = replies.map { reply ->
            SpannableString("${reply.member.name}${if (reply.member.mid == upMid) upTip else ""}：${reply.member}").apply {
                if (reply.mid == upMid) {
                    val start = reply.member.name.length
                    val end = start + upTip.length
                    val lineHeight = Utils.getTextHeightWithSize(context)
                    setSpan(
                        RadiusBackgroundSpan(
                            2,
                            context.resources.getDimension(R.dimen.card_round).toInt(),
                            Color.WHITE,
                            Color.rgb(207, 75, 95),
                            lineHeight.toInt()
                        ),
                        start,
                        end,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    setSpan(RelativeSizeSpan(0.8f), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                processChildEmotes(reply)
            }
        }

        repliesList.apply {
            isVerticalScrollBarEnabled = true
            adapter = ArrayAdapter(context, R.layout.cell_reply_child, items)
        }
    }

    private fun SpannableString.processChildEmotes(reply: Reply) {
        reply.content.emote?.let {
            ThreadManager.run {
                try {
                    EmoteUtil.textReplaceEmote(toString(), it, 1.0f, context, this@processChildEmotes)
                } catch (e: Exception) {
                    // Handle exception
                }
            }
        }
    }

    private fun CellReplyListBinding.setupImages(reply: Reply) {
        reply.content.pictures?.takeIf { it.isNotEmpty() }?.let { pictures ->
            imageCard.visibility = View.VISIBLE
            imageCount.visibility = View.VISIBLE
            imageCount.text = context.getString(R.string.picture_count, pictures.size.toString())

            imageCard.loadPicture(pictures[0].src)

            imageCard.setOnClickListener {
                context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                    putExtra("imageList", arrayListOf(pictures))
                })
            }
        } ?: run {
            imageCard.visibility = View.GONE
            imageCount.visibility = View.GONE
        }
    }

    private fun CellReplyListBinding.setupInteraction(reply: Reply, realPosition: Int) {
        replyPubDate.text = if (reply.createTime == 0L) {
            context.getString(R.string.just)
        } else {
            reply.replyTipControl.timeDesc ?: reply.createTime.formatToDate()
        }
        upLiked.visibility = if (reply.upAction.like) View.VISIBLE else View.GONE

        if (!isDetail) {
            root.setOnClickListener { startReplyInfoActivity(reply) }
            replyText.setOnClickListener { startReplyInfoActivity(reply) }
        }

        setupDeleteButton(reply, realPosition)
        setupReplyButton(reply, realPosition)
    }

    private fun CellReplyListBinding.setupDeleteButton(reply: Reply, realPosition: Int) {
        itemReplyDelete.visibility = if (isManager || reply.mid == Preferences.getLong("mid", 0)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        var longClickPosition = -1
        var longClickTime = -1L

        itemReplyDelete.setOnClickListener { MsgUtil.showMsg("长按删除") }
        itemReplyDelete.setOnLongClickListener {
            val currentTime = System.currentTimeMillis()
            if (longClickPosition == realPosition && currentTime - longClickTime < 6000) {
                deleteReply(reply, realPosition)
                true
            } else {
                longClickPosition = realPosition
                longClickTime = currentTime
                MsgUtil.showMsg("再次长按删除")
                true
            }
        }
    }

    private fun deleteReply(reply: Reply, position: Int) {
        lifecycleOwner.lifecycleScope.launch {
            bilibiliApi.api(IReplyApi::class) {
                deleteReply(type, oid, reply.replyId)
            }.apiResult().onSuccess {
                replyList.removeAt(position)
                runOnUi {
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, replyList.size - position)
                    if (position == 0 && isDetail) {
                        exitDetail()
                    }
                }
                MsgUtil.showMsg(context.getString(R.string.delete_success))
            }.onApiFailure {
                MsgUtil.showMsg(
                    when (val result = it.code) {
                        -404 -> context.getString(R.string.no_such_reply)
                        -403 -> context.getString(R.string.no_permission)
                        else -> context.getString(R.string.action_failed_with_msg, result.toString())
                    }
                )
            }.onFailure {
                MsgUtil.showMsg(it.msg())
            }
        }
    }

    private fun CellReplyListBinding.setupReplyButton(reply: Reply, position: Int) {
        replyBtn.setOnClickListener {
            val noParent = isDetail && position == 0
            context.startActivity(Intent(context, WriteReplyActivity::class.java).apply {
                putExtra("oid", oid)
                putExtra("rpid", if (noParent) this@ReplyAdapter.root else reply.replyId)
                putExtra("parent", if (noParent) this@ReplyAdapter.root else reply.replyId)
                putExtra("replyType", type)
                putExtra("pos", position)
                putExtra("parentSender", if (this@ReplyAdapter.root != 0L && !noParent) reply.member.name else "")
            })
        }
    }

    private fun startReplyInfoActivity(reply: Reply) {
        context.startActivity(Intent(context, ReplyInfoActivity::class.java).apply {
            putExtra("rpid", reply.replyId)
            putExtra("oid", reply.oid)
            putExtra("type", type)
            putExtra("up_mid", upMid)
            if (source is Serializable) putExtra("source", source as Serializable)
        })
    }

    private fun checkManagerPermission() {
        if (Preferences.getLong(Preferences.MID, 0) == 0L) return

        try {
            when (source) {
                is List<*> -> isManager =  @Suppress("UNCHECKED_CAST") (source as List<UserInfo>).any { it.mid == Preferences.getLong(Preferences.MID, 0) }
                is UserInfo -> isManager = (source as UserInfo).mid == Preferences.getLong(Preferences.MID, 0)
            }
        } catch (e: Exception) {
            MsgUtil.error(e)
        }
    }

    override fun getItemCount(): Int = replyList.size + 1

    override fun getItemViewType(position: Int): Int = when {
        isDetail && position == 1 -> 0
        !isDetail && position == 0 -> 0
        else -> 1
    }

    class ReplyViewHolder(val binding: CellReplyListBinding) : RecyclerView.ViewHolder(binding.root)
    class WriteReplyViewHolder(val binding: CellReplyActionBinding) : RecyclerView.ViewHolder(binding.root)

    data class ReplyState(
        var liked: Boolean = false,
        var likeCount: Int
    )
}