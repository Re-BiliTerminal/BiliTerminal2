package com.huanli233.biliterminal2.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.card.MaterialCardView;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.activity.settings.login.LoginActivity;
import com.huanli233.biliterminal2.activity.settings.login.SpecialLoginActivity;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;

public class SettingMainActivity extends InstanceActivity {

    private int eggClick = 0;

    private int refreshTutorialClick = 0;

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        asyncInflate(R.layout.activity_setting_main, ((layoutView, id) -> {

            MaterialCardView login_cookie = findViewById(R.id.login_cookie);
            login_cookie.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SpecialLoginActivity.class);
                intent.putExtra("login", false);
                startActivity(intent);
            });

            MaterialCardView login = findViewById(R.id.login);
            if (Preferences.getLong("mid", 0) == 0) {
                login_cookie.setVisibility(View.GONE);
                login.setVisibility(View.VISIBLE);
                login.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivity.class);
                    startActivity(intent);
                });
            }

            MaterialCardView playerSetting = findViewById(R.id.playerSetting);
            playerSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingPlayerChooseActivity.class);
                startActivity(intent);
            });

            MaterialCardView clientPlayerSetting = findViewById(R.id.terminalPlayerSetting);
            clientPlayerSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingTerminalPlayerActivity.class);
                startActivity(intent);
            });

            MaterialCardView uiSetting = findViewById(R.id.uiSetting);
            uiSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingUIActivity.class);
                startActivity(intent);
            });

            MaterialCardView menuSetting = findViewById(R.id.menuSetting);
            menuSetting.setOnClickListener(view -> startActivity(new Intent(this, SettingMenuActivity.class)));

            MaterialCardView prefSetting = findViewById(R.id.prefSetting);
            prefSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingPrefActivity.class);
                startActivity(intent);
            });

            MaterialCardView repliesSetting = findViewById(R.id.repliesSetting);
            repliesSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingRepliesActivity.class);
                startActivity(intent);
            });

            MaterialCardView infoSetting = findViewById(R.id.infoSetting);
            infoSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingInfoActivity.class);
                startActivity(intent);
            });

            MaterialCardView laboratorySetting = findViewById(R.id.laboratory);
            laboratorySetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingLaboratoryActivity.class);
                startActivity(intent);
            });

            MaterialCardView about = findViewById(R.id.about);
            about.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, AboutActivity.class);
                startActivity(intent);
            });

            String[] eggList = getResources().getStringArray(R.array.eggs);
            about.setOnLongClickListener(view -> {
                MsgUtil.showText("彩蛋", eggList[eggClick]);
                if (eggClick < eggList.length - 1) eggClick++;
                return true;
            });

            MaterialCardView refreshTutorial = findViewById(R.id.refresh_tutorial);
            refreshTutorial.setOnClickListener(view -> {
                if (refreshTutorialClick++ > 0) {
                    refreshTutorialClick = 0;

                    for (int i = 0; i < getResources().getStringArray(R.array.tutorial_list).length; i++) {
                        Preferences.removeValue("tutorial_ver_" + getResources().getStringArray(R.array.tutorial_list)[i]);
                    }

                    MsgUtil.showMsg("教程进度已清除");
                } else MsgUtil.showMsg("再点一次清除");
            });

            MaterialCardView test = findViewById(R.id.test);
            test.setVisibility(Preferences.getBoolean("developer", false) ? View.VISIBLE : View.GONE);
            test.setOnClickListener(view -> {
                Intent intent = new Intent(this, TestActivity.class);
                startActivity(intent);
            });
        }));
    }
}