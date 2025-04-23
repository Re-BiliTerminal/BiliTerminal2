package com.huanli233.biliterminal2.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.player.PlayerActivity
import com.huanli233.biliterminal2.activity.settings.SettingPlayerChooseActivity
import com.huanli233.biliterminal2.bean.PlayerData
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.Preferences.getString
import com.huanli233.biliterminal2.util.network.NetWorkUtil
import java.io.Serializable
import androidx.core.net.toUri
import com.huanli233.biliterminal2.bean.TYPE_LIVE
import com.huanli233.biliterminal2.bean.TYPE_LOCAL

object PlayerManager {

    fun playerIntent(
        context: Context,
        playerDataBuilder: PlayerData.() -> Unit
    ) = playerIntent(context, PlayerData().apply(playerDataBuilder))

    @JvmStatic
    fun playerIntent(
        context: Context,
        playerData: PlayerData
    ): Intent {
        val intent = Intent()
        when (getString(Preferences.PLAYER, "null")) {
            "terminalPlayer" -> {
                intent.setClass(context, PlayerActivity::class.java)
                intent.putExtra("url", playerData.urlVideo)
                intent.putExtra("danmaku", playerData.urlDanmaku)
                intent.putExtra("subtitle", playerData.urlSubtitle)
                intent.putExtra("title", playerData.title)
                intent.putExtra("aid", playerData.aid)
                intent.putExtra("bvid", playerData.bvid)
                intent.putExtra("cid", playerData.cid)
                intent.putExtra("mid", playerData.mid)
                intent.putExtra("progress", playerData.progress)
                intent.putExtra("live_mode", playerData.type == TYPE_LIVE)
            }

            "mtvPlayer" -> {
                intent.setClassName(
                    context.getString(R.string.player_mtv_package),
                    "com.xinxiangshicheng.wearbiliplayer.cn.player.PlayerActivity"
                )
                intent.setAction(Intent.ACTION_VIEW)
                intent.putExtra("cookie", getString(Preferences.COOKIES, ""))
                intent.putExtra("mode", (if (playerData.type == TYPE_LOCAL) "2" else "0"))
                intent.putExtra("url", playerData.urlVideo)
                intent.putExtra("danmaku", playerData.urlDanmaku)
                intent.putExtra("title", playerData.title)
            }

            "aliangPlayer" -> {
                intent.setClassName(
                    context.getString(R.string.player_aliang_package),
                    "com.aliangmaker.media.PlayVideoActivity"
                )
                intent.putExtra("name", playerData.title)
                intent.putExtra("danmaku", playerData.urlDanmaku)
                intent.putExtra("live_mode", playerData.type == TYPE_LIVE)

                intent.setData(playerData.urlVideo.toUri())

                if (playerData.type != TYPE_LOCAL) {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["Cookie"] =
                        getString(Preferences.COOKIES, "")
                    headers["Referer"] = "https://www.bilibili.com/"
                    intent.putExtra("cookie", headers as Serializable)
                    intent.putExtra("agent", NetWorkUtil.USER_AGENT_WEB)
                    intent.putExtra("progress", playerData.progress * 1000L)
                }
                intent.setAction(Intent.ACTION_VIEW)
            }

            else -> intent.setClass(context, SettingPlayerChooseActivity::class.java)
        }
        return intent
    }

}