package com.huanli233.biliterminal2.activity.video.info

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.settings.SettingPlayerChooseActivity
import com.huanli233.biliterminal2.activity.video.JumpToPlayerActivity
import com.huanli233.biliterminal2.adapter.common.Choice
import com.huanli233.biliterminal2.adapter.common.ChoiceAdapter
import com.huanli233.biliterminal2.api.BangumiApi
import com.huanli233.biliterminal2.bean.Bangumi
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Result
import com.huanli233.biliterminal2.util.ThreadManager.supplyAsyncWithLiveData
import java.util.concurrent.Callable
import java.util.stream.Collectors

class BangumiInfoFragment : Fragment() {
    private var mediaId: Long = 0
    private var selectedSection = 0
    private var selectedEpisode = 0
    private var dialog: Dialog? = null
    private var rootView: View? = null
    private var episodeRecyclerView: RecyclerView? = null
    private var sectionChoose: Button? = null
    private var episodeChoose: TextView? = null
    private var onFinishLoad: Runnable? = null
    private var loadFinished = false
    private var bangumi: Bangumi? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arguments = getArguments()
        if (arguments != null) {
            mediaId = arguments.getLong("media_id")
        }
        rootView = inflater.inflate(R.layout.fragment_media_info, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.visibility = View.GONE
        episodeRecyclerView = rootView!!.findViewById<RecyclerView>(R.id.rv_episode_list)
        // 拉数据
        supplyAsyncWithLiveData<Bangumi?>(Callable { BangumiApi.getBangumi(mediaId) })
            .observe(getViewLifecycleOwner(), Observer { result: Result<Bangumi?>? ->
                result!!.onSuccess(
                    Consumer { bangumi: Bangumi? ->
                        this.bangumi = bangumi
                        initView()
                    }).onFailure(Consumer { error: Throwable? ->
                    MsgUtil.error(
                        "番剧详情：",
                        error!!
                    )
                })
            })
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        // init data.
        val imageMediaCover = rootView!!.findViewById<ImageView>(R.id.image_media_cover)
        val playButton = rootView!!.findViewById<Button>(R.id.btn_play)
        val title = rootView!!.findViewById<TextView>(R.id.text_title)
        sectionChoose = rootView!!.findViewById<Button>(R.id.section_choose)
        episodeChoose = rootView!!.findViewById<TextView>(R.id.episode_choose)
        selectedSection = 0

        rootView!!.visibility = View.GONE
        if (onFinishLoad != null) onFinishLoad!!.run()
        else loadFinished = true

        imageMediaCover.loadPicture(bangumi!!.info.cover_horizontal, true)

        title.text = bangumi!!.info.title
        // section selector setting.
        val adapter = ChoiceAdapter()

        adapter.setOnItemClickListener { choice: Choice? ->
            selectedEpisode = choice!!.index
            refreshReplies()
        }

        val indexShow = rootView!!.findViewById<TextView>(R.id.indexShow)
        indexShow.text = bangumi!!.info.indexShow

        if (bangumi!!.sectionList.isEmpty()) {
            sectionChoose!!.text = "敬请期待"
            playButton.visibility = View.GONE
            rootView!!.findViewById<View?>(R.id.episodes).visibility = View.GONE
            val activity: Activity = requireActivity()
            if (activity is VideoInfoActivity) {
                activity.replyFragment.refreshing = false
            }
            return
        }

        sectionChoose!!.text = bangumi!!.sectionList[0].title + " 点击切换"
        sectionChoose!!.setOnClickListener(View.OnClickListener { v: View? -> this.sectionChooseDialog.show() })
        episodeChoose!!.setOnClickListener(View.OnClickListener { v: View? -> this.eposideChooseDialog.show() })

        adapter.setData(
            bangumi!!.sectionList[0].episodeList.stream()
                .map<Choice?> { item: Bangumi.Episode? ->
                    Choice(
                        item!!.title, item.id.toString()
                    )
                }.collect(Collectors.toList())
        )
        episodeRecyclerView!!.setLayoutManager(
            CustomLinearManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        episodeRecyclerView!!.setAdapter(adapter)

        // play button setting
        playButton.setOnClickListener(View.OnClickListener { v: View? ->
            val episode =
                bangumi!!.sectionList[selectedSection].episodeList.get(selectedEpisode)
            Glide.get(requireContext()).clearMemory()
            val intent = Intent(v!!.context, JumpToPlayerActivity::class.java)
            intent.putExtra("cid", episode.cid)
            intent.putExtra("title", episode.title)
            intent.putExtra("aid", episode.aid)
            intent.putExtra("html5", false)
            startActivity(intent)
        })
        playButton.setOnLongClickListener(View.OnLongClickListener { v: View? ->
            val intent = Intent(v!!.context, SettingPlayerChooseActivity::class.java)
            startActivity(intent)
            true
        })

        refreshReplies()
    }

    @get:SuppressLint("SetTextI18n")
    private val sectionChooseDialog: Dialog
        get() {
            val choices =
                arrayOfNulls<String>(bangumi!!.sectionList.size)
            for (i in bangumi!!.sectionList.indices) {
                choices[i] = bangumi!!.sectionList.get(i).title
            }

            val builder =
                AlertDialog.Builder(requireContext())
            builder.setSingleChoiceItems(
                choices,
                selectedSection,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    selectedSection = which
                    selectedEpisode = 0

                    refreshReplies()
                    val section = bangumi!!.sectionList[which]
                    sectionChoose!!.text = section.title + " 点击切换"
                    val adapter = episodeRecyclerView!!.adapter as ChoiceAdapter?
                    if (adapter != null) {
                        adapter.setData(
                            bangumi!!.sectionList[which].episodeList.stream()
                                .map<Choice?> { item: Bangumi.Episode? ->
                                    Choice(
                                        item!!.title,
                                        item.id.toString()
                                    )
                                }.collect(Collectors.toList())
                        )
                        episodeRecyclerView!!.scrollToPosition(0)
                    }
                    episodeChoose!!.setOnClickListener(View.OnClickListener { v: View? -> this.eposideChooseDialog.show() })
                    dialog!!.dismiss()
                })
            dialog = builder.create()

            return dialog!!
        }

    private val eposideChooseDialog: Dialog
        get() {
            val episodeList =
                bangumi!!.sectionList[selectedSection].episodeList

            val choices =
                arrayOfNulls<String>(episodeList.size)
            for (i in episodeList.indices) {
                val episode = episodeList[i]
                choices[i] = episode.title + "." + episode.title_long
            }

            val builder =
                AlertDialog.Builder(requireContext())
            builder.setSingleChoiceItems(
                choices,
                selectedEpisode,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    selectedEpisode = which
                    refreshReplies()

                    val adapter = episodeRecyclerView!!.adapter as ChoiceAdapter?
                    if (adapter != null) {
                        adapter.setSelectedItemIndex(which)
                        episodeRecyclerView!!.scrollToPosition(which)
                    }
                    dialog!!.dismiss()
                })
            dialog = builder.create()

            return dialog!!
        }

    private fun refreshReplies() {
        val activity: Activity? = getActivity()
        if (activity is VideoInfoActivity) {
            activity.setCurrentAid(
                bangumi!!.sectionList.get(selectedSection).episodeList.get(
                    selectedEpisode
                ).aid
            )
        }
    }

    fun setOnFinishLoad(onFinishLoad: Runnable) {
        if (loadFinished) onFinishLoad.run()
        else this.onFinishLoad = onFinishLoad
    }

    companion object {
        @JvmStatic
        fun newInstance(mediaId: Long): BangumiInfoFragment {
            val args = Bundle()
            args.putLong("media_id", mediaId)
            val fragment = BangumiInfoFragment()
            fragment.setArguments(args)
            return fragment
        }
    }
}
