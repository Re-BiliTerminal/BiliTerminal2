package com.huanli233.biliterminal2.activity.base

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.MenuActivity

class InstanceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        BiliTerminal.setInstance(this)
        super.onCreate(savedInstanceState)
    }

    fun setMenuClick() {
        findViewById<View>(R.id.top_bar).setOnClickListener { menuClick.run() }
    }

    val menuClick = Runnable {
        Intent(this@InstanceActivity, MenuActivity::class.java).apply {
            if (this@InstanceActivity.intent.hasExtra("from")) {
                putExtra("from", this@InstanceActivity.intent.getStringExtra("from"))
            }
            startActivity(this)
            overridePendingTransition(R.anim.anim_activity_in_down, 0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) menuClick.run()
        return super.onKeyDown(keyCode, event)
    }
}
