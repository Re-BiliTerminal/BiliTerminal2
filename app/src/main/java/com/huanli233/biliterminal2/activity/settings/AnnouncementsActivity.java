package com.huanli233.biliterminal2.activity.settings;

import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.AnnouncementAdapter;
import com.huanli233.biliterminal2.api.AppInfoApi;
import com.huanli233.biliterminal2.model.Announcement;
import com.huanli233.biliterminal2.util.CenterThreadPool;
import com.huanli233.biliterminal2.util.MsgUtil;

import java.util.ArrayList;

//公告列表
//2024-02-23

public class AnnouncementsActivity extends RefreshListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("公告列表");

        CenterThreadPool.run(() -> {
            try {
                ArrayList<Announcement> announcements = AppInfoApi.getAnnouncementList();
                setRefreshing(false);

                AnnouncementAdapter adapter = new AnnouncementAdapter(this, announcements);

                setAdapter(adapter);

            } catch (Exception e) {
                report(e);
                runOnUiThread(() -> MsgUtil.showMsg("连接到哔哩终端接口时发生错误"));
            }
        });
    }

}