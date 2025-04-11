package com.huanli233.biliterminal2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.helper.TutorialHelper;
import com.huanli233.biliterminal2.bean.Tutorial;
import com.huanli233.biliterminal2.util.view.AsyncLayoutInflaterX;
import com.huanli233.biliterminal2.util.Preferences;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class TutorialActivity extends BaseActivity {
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_tutorial, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();

            Intent intent = getIntent();

            Tutorial tutorial = Objects.requireNonNull(TutorialHelper.loadTutorial(getResources().getXml(intent.getIntExtra("xml_id", R.xml.tutorial_recommend))));

            ((TextView) findViewById(R.id.text_title)).setText(tutorial.name);
            ((TextView) findViewById(R.id.content)).setText(TutorialHelper.loadText(tutorial.content));

            try {
                if (tutorial.imgid != null) {
                    @SuppressLint("DiscouragedApi") int indentify = getResources().getIdentifier(getPackageName() + ":" + tutorial.imgid, null, null);
                    if (indentify > 0)
                        ((ImageFilterView) findViewById(R.id.image_view)).setImageDrawable(getResources().getDrawable(indentify));
                } else findViewById(R.id.image_view).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MaterialButton close_btn = findViewById(R.id.close_btn);
            close_btn.setOnClickListener(view -> {
                Preferences.putInt("tutorial_ver_" + intent.getStringExtra("tag"), intent.getIntExtra("version", -1));
                finish();
            });
        });
    }

    @Override
    public void onBackPressed() {
    }
}
