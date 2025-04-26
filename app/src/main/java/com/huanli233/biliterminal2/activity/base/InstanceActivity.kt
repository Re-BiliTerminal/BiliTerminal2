package com.huanli233.biliterminal2.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.MenuActivity

open class InstanceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_layout) // 确保设置正确的布局
        BiliTerminal.setInstance(this)
        setMenuClick()
    }

    private fun setMenuClick() {
        findViewById<View>(R.id.top_bar)?.setOnClickListener {
            menuClick.run()
        }
    }

    private val menuClick = Runnable {
        Intent(this@InstanceActivity, MenuActivity::class.java).apply {
            val fromExtra = this@InstanceActivity.intent.getStringExtra("from")
            if (!fromExtra.isNullOrEmpty()) {
                putExtra("from", fromExtra)
            }
            startActivity(this)
            overridePendingTransition(R.anim.anim_activity_in_down, 0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menuClick.run()
            return true // 消费事件
        }
        return super.onKeyDown(keyCode, event)
    }
}
