package com.huanli233.biliterminal2.activity.settings.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.activity.settings.login.LoginActivity;
import com.huanli233.biliterminal2.util.SharedPreferencesUtil;

public class IntroductionActivity extends BaseActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_introduction);

        MaterialCardView confirm = findViewById(R.id.confirm);

        confirm.setOnClickListener(view -> {
            SharedPreferencesUtil.putBoolean("setup", true);

            Intent intent = new Intent();
            intent.putExtra("from_setup", true);
            intent.setClass(IntroductionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

}