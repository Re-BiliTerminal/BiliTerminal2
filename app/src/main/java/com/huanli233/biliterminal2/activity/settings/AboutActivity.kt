package com.huanli233.biliterminal2.activity.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.util.AsyncLayoutInflaterX
import com.huanli233.biliterminal2.util.GlideUtil
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliterminal2.util.ToolsUtil

class AboutActivity : BaseActivity() {
    var eggClickAuthorWords: Int = 0
    var eggClickToUncle: Int = 0
    var eggClickDev: Int = 0

    @SuppressLint("MissingInflatedId", "SetTextI18n", "InflateParams")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        AsyncLayoutInflaterX(this).inflate(
            R.layout.activity_setting_about, null
        ) { layoutView: View?, _: Int, _: ViewGroup? ->
            setContentView(layoutView)
            setTopbarExit()

            try {
                val versionStr = SpannableString(
                    "版本名\n" + packageManager.getPackageInfo(
                        packageName, 0
                    ).versionName
                )
                versionStr.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    3,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                (findViewById<View>(R.id.app_version) as TextView).text =
                    versionStr

                val versionCodeStr = SpannableString(
                    "版本号\n" + packageManager.getPackageInfo(
                        packageName, 0
                    ).versionCode
                )
                versionCodeStr.setSpan(StyleSpan(Typeface.BOLD), 0, 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                (findViewById<View>(R.id.app_version_code) as TextView).text =
                    versionCodeStr
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val developerAvaterViews: List<ImageView> = listOf(
                findViewById(R.id.robinAvatar),
                findViewById(R.id.huanliAvatar)
            )
            val developerAvaters: List<Int> = listOf(R.mipmap.avatar_robin, R.mipmap.avatar_huanli)
            val developerCardList: List<MaterialCardView> = listOf(findViewById(R.id.robin_card), findViewById(R.id.huanli_card))
            val developerUidList: List<Long> = listOf(646521226, 673815151)

            for (i in developerAvaterViews.indices) {
                if (developerAvaters[i] != -1) kotlin.runCatching {
                    Glide.with(this).load(developerAvaters[i])
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.akari)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(developerAvaterViews[i])
                }


                developerCardList[i].setOnClickListener { view: View? ->
                    val intent = Intent()
                        .setClass(this, UserInfoActivity::class.java)
                        .putExtra("mid", developerUidList[i])
                    startActivity(intent)
                }
            }

            findViewById<View>(R.id.toUncle).setOnClickListener {
                eggClickToUncle++
                if (eggClickToUncle == 7) {
                    eggClickToUncle = 0
                    MsgUtil.showText(
                        "给叔叔",
                        "\"你指尖跃动的电光，是我此生不灭的信仰。\"<extra_insert>{\"type\":\"video\",\"content\":\"BV157411v76Z\",\"title\":\"【B站入站曲】\"}"
                    )
                }
            }

            findViewById<View>(R.id.icon_license_list).setOnClickListener { v: View? ->
                val str = StringBuilder(getString(R.string.desc_icon_license))
                val logItems = resources.getStringArray(R.array.icon_license)
                for (i in logItems.indices) str.append('\n').append((i + 1)).append('.')
                    .append(logItems[i])
                MsgUtil.showText(getString(R.string.info_open_source_icons), str.toString())
            }

            if (!ToolsUtil.isDebugBuild()) findViewById<View>(R.id.debug_tip).visibility =
                View.GONE
        }
    }
}
