package com.huanli233.biliterminal2.activity.reply

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.EmoteActivity
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.api.EmoteApi
import com.huanli233.biliterminal2.api.ReplyApi
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.onApiFailure
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.databinding.ActivityReplyWriteBinding
import com.huanli233.biliterminal2.event.ReplyEvent
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliwebapi.api.interfaces.IReplyApi
import com.huanli233.biliwebapi.bean.reply.Reply
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WriteReplyActivity : BaseActivity() {

    private lateinit var binding: ActivityReplyWriteBinding
    private var isSending = false

    // 使用伴生对象存放静态数据
    companion object {
        private val ERROR_MESSAGES = mapOf(
            -101 to "没有登录or登录信息有误？",
            -102 to "账号被封禁！",
            -509 to "请求过于频繁！",
            12015 to "需要评论验证码...？",
            12016 to "包含敏感内容！",
            12025 to "字数过多啦QAQ",
            12035 to "被拉黑了...",
            12051 to "重复评论，请勿刷屏！"
        )
    }

    // Activity Result 处理
    private val emoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("text")?.let { emote ->
                binding.editText.append(emote)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplyWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLoginStatus()
        setupViews()
        setupIntentData()
    }

    private fun checkLoginStatus() {
        if (Preferences.getLong(Preferences.MID, 0) == 0L) {
            MsgUtil.showMsg("还没有登录喵~")
            finish()
        }
    }

    private fun setupViews() {
        with(binding) {
            send.setOnClickListener { handleSendClick() }
            emote.setOnClickListener { openEmotePicker() }
        }
    }

    private fun setupIntentData() {
        intent?.extras?.let { extras ->
            extras.getString("parentSender")?.takeIf { it.isNotEmpty() }?.let { sender ->
                binding.editText.setText("回复 @$sender :")
                binding.editText.setSelection(binding.editText.text.length)
            }
        }
    }

    private fun handleSendClick() {
        if (!Preferences.getBoolean(Preferences.COOKIE_REFRESH, true)) {
            MsgUtil.showDialog("无法发送", "上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作")
            return
        }

        if (isSending) {
            MsgUtil.showMsg("正在发送中")
            return
        }

        val text = binding.editText.text.toString().trim()
        if (text.isEmpty()) {
            MsgUtil.showMsg("还没输入内容呢~")
            return
        }

        val (oid, rpid, parent, replyType, pos) = parseIntentParams()
        sendReply(oid, rpid, parent, replyType, pos, text)
    }

    private fun parseIntentParams(): ReplyParams {
        return intent?.extras?.let {
            ReplyParams(
                oid = it.getLong("oid"),
                rpid = it.getLong("rpid"),
                parent = it.getLong("parent"),
                replyType = it.getInt("replyType", ReplyApi.REPLY_TYPE_VIDEO),
                pos = it.getInt("pos", -1)
            )
        } ?: ReplyParams()
    }

    private fun sendReply(oid: Long, rpid: Long, parent: Long, replyType: Int, pos: Int, text: String) {
        isSending = true

        lifecycleScope.launch {
            bilibiliApi.api(IReplyApi::class) {
                sendReply(
                    oid = oid,
                    type = replyType,
                    content = text,
                    extraParams = if (rpid == 0L) emptyMap()
                    else mapOf("root" to rpid, "parent" to parent)
                )
            }.apiResultNonNull().onSuccess {
                it.reply?.let { it1 -> handleSuccess(it1, pos, oid) }
            }.onApiFailure {
                handleError(it.code)
            }
            isSending = false
        }
    }

    private fun handleSuccess(reply: Reply, pos: Int, oid: Long) {
        MsgUtil.showMsg("发送成功>w<")
        EventBus.getDefault().post(
            ReplyEvent(
                type = 1,
                message = reply.copy(
                    createTime = 0L
                ),
                oid = oid,
                pos = pos
            )
        )
        finish()
    }

    private fun handleError(code: Int) {
        val message = ERROR_MESSAGES[code] ?: "未知错误：$code"
        MsgUtil.showMsg("评论发送失败：\n$message")
    }

    private fun openEmotePicker() {
        emoteLauncher.launch(
            Intent(this, EmoteActivity::class.java).apply {
                putExtra("from", EmoteApi.BUSINESS_REPLY)
            }
        )
    }

    private data class ReplyParams(
        val oid: Long = 0L,
        val rpid: Long = 0L,
        val parent: Long = 0L,
        val replyType: Int = ReplyApi.REPLY_TYPE_VIDEO,
        val pos: Int = -1
    )
}