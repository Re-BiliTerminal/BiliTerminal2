package com.huanli233.biliterminal2.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.SettingsAdapter;
import com.huanli233.biliterminal2.bean.SettingSection;

import java.util.ArrayList;
import java.util.List;

public class SettingInfoActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("详情页设置");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "收藏夹单选", "fav_single", getString(R.string.desc_fav_single), "false"));
            add(new SettingSection("switch", "收藏成功提示", "fav_notice", getString(R.string.desc_fav_notice), "false"));
            add(new SettingSection("switch", "显示视频标签", "tags_enable", getString(R.string.desc_tags_enable), "true"));
            add(new SettingSection("switch", "视频相关推荐", "related_enable", getString(R.string.desc_related_enable), "true"));
            add(new SettingSection("switch", "点击封面播放", "cover_play_enable", getString(R.string.desc_cover_play_enable), "false"));
            add(new SettingSection("switch", "以游客方式观看直播", "live_by_guest", getString(R.string.desc_live_by_guest), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);

    }

}