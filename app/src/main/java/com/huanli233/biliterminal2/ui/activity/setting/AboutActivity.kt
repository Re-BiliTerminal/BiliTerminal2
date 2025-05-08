package com.huanli233.biliterminal2.ui.activity.setting

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.huanli233.biliterminal2.BuildConfig
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityAboutBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.dialog.Dialogs
import com.huanli233.biliterminal2.utils.extensions.startActivityOrMsg

const val URL_QQ_CHANNEL = "https://pd.qq.com/s/fdti2l61d"
const val ID_QQ_GROUP = "719041250"

class AboutActivity: BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    @OptIn(ExperimentalBadgeUtils::class)
    fun initUi() {
        binding.versionNameChip.apply {
            text = context.getString(R.string.version_name_format, BuildConfig.VERSION_NAME)
            if (BuildConfig.DEBUG) {
                chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.icon_bug_report, theme)
            }
        }
        binding.goToRepoButton.setOnClickListener {
            startActivityOrMsg(Intent(Intent.ACTION_VIEW, getString(R.string.repo_url).toUri()))
        }
        binding.qqChannel.setOnClickListener {
            Dialogs.textAction(originalViewContext, URL_QQ_CHANNEL) {
                startActivityOrMsg(Intent(Intent.ACTION_VIEW, URL_QQ_CHANNEL.toUri()))
            }
        }
        binding.qqGroup.setOnClickListener {
            Dialogs.text(originalViewContext, getString(R.string.group_id, ID_QQ_GROUP))
        }
    }

}