package com.huanli233.biliterminal2.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.player.PlayerActivity
import com.huanli233.biliterminal2.activity.settings.SettingPlayerChooseActivity
import com.huanli233.biliterminal2.activity.video.JumpToPlayerActivity
import com.huanli233.biliterminal2.contextNotNull
import com.huanli233.biliterminal2.service.DownloadService
import com.huanli233.biliterminal2.util.network.NetWorkUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliterminal2.util.Preferences.getLong
import com.huanli233.biliterminal2.util.Preferences.getString
import com.huanli233.biliwebapi.bean.video.VideoInfo
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable
import java.util.Objects

object PlayerApi {
    @JvmStatic
    fun startGettingUrl(context: Context, videoInfo: VideoInfo, page: Int, progress: Int) {
        val mid = try {
            getLong(Preferences.MID, 0)
        } catch (ignored: Throwable) {
            0
        }
        val intent = Intent()
            .setClass(context, JumpToPlayerActivity::class.java)
            .putExtra(
                "title",
                (if (videoInfo.pages.size == 1) videoInfo.title else videoInfo.pages[page].part)
            )
            .putExtra("bvid", videoInfo.bvid)
            .putExtra("aid", videoInfo.aid)
            .putExtra("cid", videoInfo.pages[page].cid)
            .putExtra("mid", mid)
            .putExtra("progress", progress)
        context.startActivity(intent)
    }

    @JvmStatic
    fun startDownloading(videoInfo: VideoInfo, page: Int, qn: Int) {
        if (getBoolean("dev_download_old", false)) {
            val context = contextNotNull

            val intent = Intent(
                context,
                JumpToPlayerActivity::class.java
            )
                .putExtra("aid", videoInfo.aid)
                .putExtra("bvid", videoInfo.bvid)
                .putExtra("cid", videoInfo.pages[page].cid)
                .putExtra(
                    "title",
                    (if (videoInfo.pages.size == 1) videoInfo.title else videoInfo.pages[page].part)
                )
                .putExtra("download", (if (videoInfo.pages.size == 1) 1 else 2))
                .putExtra("cover", videoInfo.pic)
                .putExtra("parent_title", videoInfo.title)
                .putExtra("qn", qn)
                .putExtra("mid", videoInfo.staff[0].mid)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
            return
        }

        if (videoInfo.pages.size == 1) DownloadService.startDownload(
            videoInfo.title,
            videoInfo.aid, videoInfo.pages[0].cid,
            ("https://comment.bilibili.com/" + videoInfo.pages[0].cid + ".xml"),
            videoInfo.pic,
            qn
        )
        else DownloadService.startDownload(
            videoInfo.title, videoInfo.pages[page].part,
            videoInfo.aid, videoInfo.pages[page].cid,
            ("https://comment.bilibili.com/" + videoInfo.pages[page].cid + ".xml"),
            videoInfo.pic,
            qn
        )
    }

    /**
     * @param aid      aid
     * @param cid      cid
     * @param qn       qn
     * @param download 是否下载
     * @return 视频url与完整返回信息
     */
    @JvmStatic
    @Throws(JSONException::class, IOException::class)
    fun getVideo(aid: Long, cid: Long, qn: Int, download: Boolean): Pair<String, String> {
        val html5 = !download && getString(Preferences.PLAYER, "") == "mtvPlayer"

        // html5方式现在已经仅对小电视播放器保留了
        var url = ("https://api.bilibili.com/x/player/wbi/playurl?"
                + "avid=" + aid
                + "&cid=" + cid
                + (if (html5) "&high_quality=1&qn=$qn" else "&qn=$qn")
                + "&platform=" + (if (html5) "html5" else "pc"))

        url = ConfInfoApi.signWBI(url)

        val response = NetWorkUtil.get(url, NetWorkUtil.webHeaders)

        val body = Objects.requireNonNull(response.body).string()
        val body1 = JSONObject(body)
        val data = body1.getJSONObject("data")
        val durl = data.getJSONArray("durl")
        val video_url = durl.getJSONObject(0)
        val videourl = video_url.getString("url")
        return Pair(videourl, body)
    }

    @JvmStatic
    fun jumpToPlayer(
        context: Context,
        videourl: String?,
        danmakuurl: String?,
        subtitleurl: String?,
        title: String?,
        local: Boolean,
        aid: Long,
        bvid: String?,
        cid: Long,
        mid: Long,
        progress: Int,
        liveMode: Boolean
    ): Intent {
        val intent = Intent()
        when (getString(Preferences.PLAYER, "null")) {
            "terminalPlayer" -> {
                intent.setClass(context, PlayerActivity::class.java)
                intent.putExtra("url", videourl)
                intent.putExtra("danmaku", danmakuurl)
                intent.putExtra("subtitle", subtitleurl)
                intent.putExtra("title", title)
                intent.putExtra("aid", aid)
                intent.putExtra("bvid", bvid)
                intent.putExtra("cid", cid)
                intent.putExtra("mid", mid)
                intent.putExtra("progress", progress)
                intent.putExtra("live_mode", liveMode)
            }

            "mtvPlayer" -> {
                intent.setClassName(
                    context.getString(R.string.player_mtv_package),
                    "com.xinxiangshicheng.wearbiliplayer.cn.player.PlayerActivity"
                )
                intent.setAction(Intent.ACTION_VIEW)
                intent.putExtra("cookie", getString(Preferences.COOKIES, ""))
                intent.putExtra("mode", (if (local) "2" else "0"))
                intent.putExtra("url", videourl)
                intent.putExtra("danmaku", danmakuurl)
                intent.putExtra("title", title)
            }

            "aliangPlayer" -> {
                intent.setClassName(
                    context.getString(R.string.player_aliang_package),
                    "com.aliangmaker.media.PlayVideoActivity"
                )
                intent.putExtra("name", title)
                intent.putExtra("danmaku", danmakuurl)
                intent.putExtra("live_mode", liveMode)

                intent.setData(Uri.parse(videourl))

                if (!local) {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["Cookie"] =
                        getString(Preferences.COOKIES, "")
                    headers["Referer"] = "https://www.bilibili.com/"
                    intent.putExtra("cookie", headers as Serializable)
                    intent.putExtra("agent", NetWorkUtil.USER_AGENT_WEB)
                    intent.putExtra("progress", progress * 1000L)
                }
                intent.setAction(Intent.ACTION_VIEW)
            }

            else -> intent.setClass(context, SettingPlayerChooseActivity::class.java)
        }
        return intent
    }
}
