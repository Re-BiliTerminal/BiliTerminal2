package com.huanli233.biliterminal2.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.SettingsAdapter;
import com.huanli233.biliterminal2.bean.SettingSection;
import com.huanli233.biliterminal2.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SettingRepliesActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("评论区设置");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "众生平等的名称颜色", Preferences.NO_VIP_COLOR, getString(R.string.desc_no_vip_color), "false"));
            add(new SettingSection("switch", "不想看见铭牌", Preferences.NO_MEDAL, getString(R.string.desc_no_medal), "false"));
            add(new SettingSection("switch", "跑马灯展示名称", Preferences.REPLY_MARQUEE_NAME, getString(R.string.desc_reply_marquee_name), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);

    }

}