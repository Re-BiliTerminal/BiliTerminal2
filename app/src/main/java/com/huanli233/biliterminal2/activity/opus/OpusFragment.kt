package com.huanli233.biliterminal2.activity.opus

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.drakeet.multitype.MultiTypeAdapter
import com.huanli233.biliterminal2.databinding.FragmentSimpleListBinding
import com.huanli233.biliterminal2.databinding.ItemOpusTextViewBinding
import com.huanli233.biliterminal2.util.multitype.register
import com.huanli233.biliwebapi.bean.opus.OpusContentModule
import androidx.core.graphics.toColorInt
import androidx.core.text.backgroundColor
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ItemOpusFooterBinding
import com.huanli233.biliterminal2.databinding.ItemOpusHeaderBinding
import com.huanli233.biliterminal2.databinding.ItemOpusImageBinding
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.extensions.FooterView
import com.huanli233.biliterminal2.util.extensions.HeaderView
import com.huanli233.biliterminal2.util.extensions.toColorIntOrNull
import com.huanli233.biliterminal2.util.extensions.dp2px
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.biliwebapi.bean.opus.PARAGRAPH_TYPE_LINE
import com.huanli233.biliwebapi.bean.opus.PARAGRAPH_TYPE_LIST
import com.huanli233.biliwebapi.bean.opus.PARAGRAPH_TYPE_PICTURE
import com.huanli233.biliwebapi.bean.opus.PARAGRAPH_TYPE_QUOTE
import com.huanli233.biliwebapi.bean.opus.PARAGRAPH_TYPE_WORD
import kotlin.math.roundToInt

private const val ARG_OPUS_ID = "opusId"

class OpusFragment: Fragment() {

    companion object {
        fun newInstance(opusId: String): OpusFragment {
            return OpusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_OPUS_ID, opusId)
                }
            }
        }
    }

    private lateinit var opusId: String

    private lateinit var binding: FragmentSimpleListBinding
    val viewModel: OpusViewModel by activityViewModels()
    private val multiTypeAdapter: MultiTypeAdapter by lazy { MultiTypeAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            opusId = it.getString(ARG_OPUS_ID).orEmpty()
        }
        if (opusId.isEmpty()) {
            activity?.finish()
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentSimpleListBinding.inflate(inflater).also {
        binding = it
    }.root

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = CustomLinearManager(context)
            adapter = multiTypeAdapter.register {
                +OpusHeaderViewDelegate(viewModel, this@OpusFragment)
                +OpusFooterViewDelegate(viewModel, this@OpusFragment)
            }.apply {
                register(OpusContentModule.Paragraph::class).to(
                    OpusTextViewDelegate(),
                    OpusImageViewDelegate(this@OpusFragment)
                ).withKotlinClassLinker { _, data ->
                    when (data.type) {
                        PARAGRAPH_TYPE_WORD, PARAGRAPH_TYPE_QUOTE, PARAGRAPH_TYPE_LIST -> OpusTextViewDelegate::class
                        PARAGRAPH_TYPE_LINE, PARAGRAPH_TYPE_PICTURE -> OpusImageViewDelegate::class
                        else -> OpusTextViewDelegate::class
                    }
                }
            }
        }
        viewModel.opusState.observe(viewLifecycleOwner) {
            it.onSuccess {
                multiTypeAdapter.items = buildList {
                    add(HeaderView(it))
                    addAll(it.modules.moduleContent.paragraphs)
                    add(FooterView(it))
                }
                multiTypeAdapter.notifyDataSetChanged()
            }
        }
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.handle { data ->
                MsgUtil.showMsg(getString(R.string.operation_failed, data))
            }
        }
    }

}

class OpusHeaderViewDelegate(
    private val viewModel: OpusViewModel,
    private val lifecycleOwner: LifecycleOwner
): ItemViewBinder<HeaderView<Opus>, OpusHeaderViewDelegate.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: HeaderView<Opus>) {
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder =
        ViewHolder(ItemOpusHeaderBinding.inflate(inflater, parent, false).also {
            it.viewModel = viewModel
            it.lifecycleOwner = lifecycleOwner
        })

    class ViewHolder(val binding: ItemOpusHeaderBinding): RecyclerView.ViewHolder(binding.root)
}

class OpusFooterViewDelegate(
    private val viewModel: OpusViewModel,
    private val lifecycleOwner: LifecycleOwner
): ItemViewBinder<FooterView<Opus>, OpusFooterViewDelegate.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: FooterView<Opus>) {
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder =
        ViewHolder(ItemOpusFooterBinding.inflate(inflater, parent, false).also {
            it.viewModel = viewModel
            it.lifecycleOwner = lifecycleOwner
        })

    class ViewHolder(val binding: ItemOpusFooterBinding): RecyclerView.ViewHolder(binding.root)
}

class OpusTextViewDelegate(): ItemViewBinder<OpusContentModule.Paragraph, OpusTextViewDelegate.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: OpusContentModule.Paragraph) {
        when (item.type) {
            PARAGRAPH_TYPE_WORD, PARAGRAPH_TYPE_QUOTE -> {
                holder.binding.textView.gravity = when (item.align) {
                    0 -> Gravity.LEFT
                    1 -> Gravity.CENTER
                    2 -> Gravity.RIGHT
                    else -> Gravity.LEFT
                }
                Utils.copyable(holder.binding.textView)
                holder.binding.textView.text = buildSpannedString {
                    item.text?.nodes?.forEach { node ->
                        when (node.type) {
                            "TEXT_NODE_TYPE_WORD" -> {
                                style(node.word) {
                                    if (item.type == PARAGRAPH_TYPE_QUOTE) {
                                        backgroundColor("#e3e5e7".toColorInt()) {
                                            append(node.words)
                                        }
                                    } else {
                                        append(node.words)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            PARAGRAPH_TYPE_LIST -> {
                holder.binding.textView.text = buildSpannedString {
                    if (item.list?.type == 1) {
                        // ordered list
                        item.list?.items?.forEach {
                            val number = "${it.order}."
                            val fullText = "$number $item"

                            val numberWidth = holder.binding.textView.paint.measureText("$number ")
                            inSpans({
                                LeadingMarginSpan.Standard(0, numberWidth.roundToInt())
                            }) {
                                append(fullText)
                            }
                            append("\n")
                        }
                        holder.binding.textView.apply { updatePadding(left = paddingLeft + context.dp2px(30f)) }
                    } else {
                        // unordered list
                        item.list?.items?.forEach {
                            inSpans(BulletSpan(15, holder.binding.textView.currentTextColor)) {
                                append(it.nodes.joinToString("") { it.words })
                            }
                            append("\n")
                        }
                    }
                }
            }
        }
    }

    inline fun SpannableStringBuilder.style(word: OpusContentModule.Text.Word, action: SpannableStringBuilder.() -> Unit) {
        val style = word.style
        val spans = mutableListOf<Any>()

        val typefaceStyle = when {
            style.bold && style.italic -> Typeface.BOLD_ITALIC
            style.bold -> Typeface.BOLD
            style.italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        if (typefaceStyle != Typeface.NORMAL) {
            spans += StyleSpan(typefaceStyle)
        }
        if (style.strikethrough) {
            spans += StrikethroughSpan()
        }
        word.color.takeIf { it.isNullOrEmpty().not() }?.let {
            it.toColorIntOrNull()?.let { color -> spans += ForegroundColorSpan(color) }
        }

        inSpans(
            spans = spans.toTypedArray(),
            builderAction = action
        )
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemOpusTextViewBinding.inflate(inflater, parent, false))
    }

    class ViewHolder(val binding: ItemOpusTextViewBinding): RecyclerView.ViewHolder(binding.root)
}

class OpusImageViewDelegate(
    val lifecycleOwner: LifecycleOwner
): ItemViewBinder<OpusContentModule.Paragraph, OpusImageViewDelegate.ViewHolder>() {
    override fun onBindViewHolder(
        holder: ViewHolder,
        item: OpusContentModule.Paragraph
    ) {
        when (item.type) {
            PARAGRAPH_TYPE_PICTURE -> {
                holder.binding.urls = item.pic?.pics?.map { it.url }.orEmpty()
            }
            PARAGRAPH_TYPE_LINE -> {
                holder.binding.urls = listOf(item.line?.pic?.url.orEmpty())
            }
        }
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewHolder = ViewHolder(ItemOpusImageBinding.inflate(inflater, parent, false).also {
        it.lifecycleOwner = lifecycleOwner
    })

    class ViewHolder(val binding: ItemOpusImageBinding): RecyclerView.ViewHolder(binding.root)

}