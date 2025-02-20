package com.huanli233.biliterminal2.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.util.AsyncLayoutInflaterX;
import com.huanli233.biliterminal2.util.SharedPreferencesUtil;

public class SettingMenuActivity extends BaseActivity {

    private SwitchMaterial menu_popular, menu_live, menu_precious;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_menu, null, (layoutView, resId, parent) -> {
            setContentView(R.layout.activity_setting_menu);
            setTopbarExit();

            menu_popular = findViewById(R.id.menu_popular);
            menu_popular.setChecked(SharedPreferencesUtil.getBoolean("menu_popular", true));

            menu_live = findViewById(R.id.menu_live);
            menu_live.setChecked(SharedPreferencesUtil.getBoolean("menu_live", false));

            menu_precious = findViewById(R.id.menu_precious);
            menu_precious.setChecked(SharedPreferencesUtil.getBoolean("menu_precious", false));

            MaterialButton sort_btn = findViewById(R.id.sort);
            sort_btn.setOnClickListener(view -> {
                Intent intent = new Intent(SettingMenuActivity.this, SortSettingActivity.class);
                startActivity(intent);
            });
        });
    }

    private void save() {
        SharedPreferencesUtil.putBoolean("menu_popular", menu_popular.isChecked());
        SharedPreferencesUtil.putBoolean("menu_precious", menu_precious.isChecked());
        SharedPreferencesUtil.putBoolean("menu_live", menu_live.isChecked());
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}
