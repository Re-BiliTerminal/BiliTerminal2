package com.huanli233.biliterminal2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.main.MainActivity
import com.huanli233.biliterminal2.ui.widget.views.ExpandableTextView
import kotlin.system.exitProcess

class CrashActivity : BaseActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val stackView = findViewById<ExpandableTextView>(R.id.stack)

        val intent = getIntent()
        val stack = intent.getSerializableExtra("stack") as Throwable?

        stackView.setText(stack?.stackTraceToString())

        findViewById<View>(R.id.exit_btn).setOnClickListener {
            exitProcess(-1)
        }

        findViewById<View>(R.id.restart_btn).setOnClickListener {
            finish()
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            Process.killProcess(Process.myPid())
        }
    }

    override fun eventBusEnabled(): Boolean {
        return false
    }
}
