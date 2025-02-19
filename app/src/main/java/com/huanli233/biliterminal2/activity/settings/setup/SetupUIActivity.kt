package com.huanli233.biliterminal2.activity.settings.setup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import com.google.android.material.switchmaterial.SwitchMaterial
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.settings.UIPreviewActivity
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.SharedPreferencesUtil

class SetupUIActivity : BaseActivity() {
    private lateinit var uiScaleInput: EditText
    private lateinit var uiPaddingH: EditText
    private lateinit var uiPaddingV: EditText

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_ui)

        uiScaleInput = findViewById(R.id.ui_scale_input)
        uiScaleInput.setText(SharedPreferencesUtil.getFloat("dpi", 1.0f).toString())

        uiPaddingH = findViewById(R.id.ui_padding_horizontal)
        uiPaddingH.setText(SharedPreferencesUtil.getInt("paddingH_percent", 0).toString())
        uiPaddingV = findViewById(R.id.ui_padding_vertical)
        uiPaddingV.setText(SharedPreferencesUtil.getInt("paddingV_percent", 0).toString())

        val round = findViewById<SwitchMaterial>(R.id.switch_round)
        round.isChecked = SharedPreferencesUtil.getBoolean("player_ui_round", false)
        round.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                uiPaddingH.setText("11")
                uiPaddingV.setText("11")
                SharedPreferencesUtil.putBoolean("player_ui_round", true)
            } else {
                uiPaddingH.setText("0")
                uiPaddingV.setText("0")
                SharedPreferencesUtil.putBoolean("player_ui_round", false)
            }
        }

        findViewById<View>(R.id.preview).setOnClickListener { view: View? ->
            save()
            val intent = Intent()
            intent.setClass(this@SetupUIActivity, UIPreviewActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.confirm).setOnClickListener { view: View? ->
            save()
            val intent = Intent()
            intent.setClass(this@SetupUIActivity, IntroductionActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.reset).setOnClickListener { view: View? ->
            SharedPreferencesUtil.putInt("paddingH_percent", 0)
            SharedPreferencesUtil.putInt("paddingV_percent", 0)
            SharedPreferencesUtil.putFloat("dpi", 1.0f)
            SharedPreferencesUtil.putBoolean("player_ui_round", false)
            uiScaleInput.setText("1.0")
            uiPaddingH.setText("0")
            uiPaddingV.setText("0")
            round.isChecked = false
            MsgUtil.showMsg(getString(R.string.restore_success))
        }
    }

    private fun save() {
        if (uiScaleInput.text.toString().isNotEmpty()) {
            val dpiTimes = uiScaleInput.text.toString().toFloat()
            if (dpiTimes in 0.1f..10.0f) SharedPreferencesUtil.putFloat(
                "dpi",
                dpiTimes
            )
            Log.e("dpi", uiScaleInput.text.toString())
        }

        if (uiPaddingH.text.toString().isNotEmpty()) {
            val paddingH = uiPaddingH.text.toString().toInt()
            if (paddingH <= 30) SharedPreferencesUtil.putInt("paddingH_percent", paddingH)
            Log.e("paddingH", uiPaddingH.text.toString())
        }

        if (uiPaddingV.text.toString().isNotEmpty()) {
            val paddingV = uiPaddingV.text.toString().toInt()
            if (paddingV <= 30) SharedPreferencesUtil.putInt("paddingV_percent", paddingV)
            Log.e("paddingV", uiPaddingV.text.toString())
        }
    }
}