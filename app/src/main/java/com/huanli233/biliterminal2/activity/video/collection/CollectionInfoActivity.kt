package com.huanli233.biliterminal2.activity.video.collection

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.RefreshListActivity
import com.huanli233.biliterminal2.adapter.video.VideoCardHolder
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.bean.VideoCardKt
import com.huanli233.biliterminal2.bean.toVideoCard
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.TerminalContext
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.extensions.smoothScrollTo
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import com.huanli233.biliwebapi.bean.video.UgcSeason
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch


class CollectionInfoActivity : RefreshListActivity() {
    private var ugcSeason: UgcSeason? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val aid = intent.getLongExtra("fromVideo", -1)
        setPageName(getString(R.string.collection_detail))

        lifecycleScope.launch {
            bilibiliApi.api(IVideoApi::class.java) {
                getVideoInfo(aid)
            }.apiResultNonNull()
                .onSuccess {
                    ugcSeason = it.ugcSeason?.also { ugcSeason ->
                        ugcSeason.sections?.let { sections ->
                            SectionAdapter(this@CollectionInfoActivity, ugcSeason, recyclerView)
                            var pos = 1
                            for (section in sections) {
                                pos++
                                val episodes = section.episodes
                                for (episode in episodes) {
                                    pos++
                                    if (episode.aid == aid) {
                                        recyclerView.smoothScrollTo(--pos)
                                    }
                                }
                            }
                        } ?: finish()
                    }
                }
        }
    }

    internal class SectionAdapter(
        val context: Context,
        val ugcSeason: UgcSeason,
        val recyclerView: RecyclerView
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val data = ugcSeason.sections!!
        private val _types: MutableList<Int> = ArrayList()

        override fun getItemViewType(position: Int): Int {
            if (position == 0) return -1
            return getTypes()[position - 1]
        }

        private fun getTypes(): List<Int> {
            synchronized(this) {
                _types.clear()
                for (section in data) {
                    _types.add(1)
                    for (i in section.episodes.indices) {
                        _types.add(0)
                    }
                }
                return _types
            }
        }

        private fun getSectionPos(pos: Int): Int {
            val list = getTypes()
            var sectionPos = -1
            for (i in 0..pos) {
                if (list[i] == 1) sectionPos++
            }
            return sectionPos
        }

        private fun getEpisodePos(pos: Int): Int {
            val list = getTypes()
            var episodePos = -1
            for (i in pos downTo 0) {
                if (list[i] == 1) return episodePos
                episodePos++
            }
            return 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == -1) {
                val view = LayoutInflater.from(this.context)
                    .inflate(R.layout.cell_collection_info, parent, false)
                return CollectionInfoHolder(view)
            } else if (viewType == 0) {
                val view = LayoutInflater.from(this.context)
                    .inflate(R.layout.cell_video_list, parent, false)
                return VideoCardHolder(view)
            } else {
                return SectionHolder(TextView(context))
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var realPosition = position
            if (holder is SectionHolder) {
                realPosition--
                holder.item.text = data[getSectionPos(realPosition)].title
            } else if (holder is VideoCardHolder) {
                realPosition--
                val videoInfo: VideoInfo =
                    data[getSectionPos(realPosition)].episodes[getEpisodePos(realPosition)]
                val videoCard: VideoCardKt = videoInfo.toVideoCard()
                holder.itemView.setOnClickListener {
                    TerminalContext.getInstance().enterVideoDetailPage(
                        context, videoCard.aid, videoCard.bvid
                    )
                }
                holder.bindData(videoCard, context)
            } else if (holder is CollectionInfoHolder) {
                holder.name.text = ugcSeason.title
                holder.desc.text =
                    if (TextUtils.isEmpty(ugcSeason.intro)) context.getString(R.string.no_desc) else ugcSeason.intro
                holder.playTimes.text = context.getString(
                    R.string.total_count,
                    Utils.toWan(ugcSeason.stat.view.toLong())
                )
                holder.cover.loadPicture(ugcSeason.cover, setClick = true)
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            for (section in data) {
                count++
                count += section.episodes.size
            }
            return ++count
        }

        internal class SectionHolder(val item: TextView) : RecyclerView.ViewHolder(item) {
            init {
                item.left = 5
            }
        }

        internal class CollectionInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView =
                itemView.findViewById(R.id.name)
            val desc: TextView =
                itemView.findViewById(R.id.desc)
            val playTimes: TextView =
                itemView.findViewById(R.id.playTimes)

            val cover: ImageView =
                itemView.findViewById(R.id.img_cover)
        }
    }
}