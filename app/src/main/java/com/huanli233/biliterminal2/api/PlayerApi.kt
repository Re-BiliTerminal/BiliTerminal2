package com.huanli233.biliterminal2.api

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Pair
import com.huanli233.biliterminal2.activity.video.JumpToPlayerActivity
import com.huanli233.biliterminal2.contextNotNull
import com.huanli233.biliterminal2.service.DownloadService
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliterminal2.util.Preferences.getLong
import com.huanli233.biliterminal2.util.Preferences.getString
import com.huanli233.biliterminal2.util.encode.MD5Util
import com.huanli233.biliterminal2.util.network.NetWorkUtil
import com.huanli233.biliterminal2.util.network.NetWorkUtil.FormData
import com.huanli233.biliwebapi.bean.video.VideoInfo
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
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

    @JvmStatic
    fun getBangumi(
        aid: Long,
        cid: Long,
        qn: Int,
    ): BangumiPlayInfo {
        val reqData = FormData()
            .setUrlParam(true)
            .put("aid", aid)
            .put("cid", cid)
            .put("fnval", 1)
            .put("fnvar", 0)
            .put("qn", qn)
            .put("season_type", 1)
            .put(
                "session",
                MD5Util.md5(java.lang.String.valueOf(System.currentTimeMillis() - SystemClock.currentThreadTimeMillis()))
            )
            .put("platform", "pc")

        val url = "https://api.bilibili.com/pgc/player/web/playurl$reqData"

        val body = NetWorkUtil.getJson(url)

        val data = body.getJSONObject("result")
        val durl = data.getJSONArray("durl")
        val videoUrl = durl.getJSONObject(0)

        val acceptDescription = data.getJSONArray("accept_description")
        val acceptQuality = data.getJSONArray("accept_quality")
        val qnStrList = arrayOfNulls<String>(acceptDescription.length())
        val qnValueList = IntArray(acceptDescription.length())
        for (i in qnStrList.indices) {
            qnStrList[i] = acceptDescription.optString(i)
            qnValueList[i] = acceptQuality.optInt(i)
        }
        return BangumiPlayInfo(videoUrl.getString("url"),
            "https://comment.bilibili.com/$cid.xml", qnStrList.requireNoNulls().toList(), qnValueList.toList())
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
}

data class BangumiPlayInfo(
    val videoUrl: String,
    val danmakuUrl: String,
    val qnStrList: List<String>,
    val qnValueList: List<Int>
)