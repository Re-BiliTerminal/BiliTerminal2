package com.huanli233.biliterminal2.activity.video.info;

import android.content.Intent;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.favorite.FolderChooseAdapter;
import com.huanli233.biliterminal2.api.FavoriteApi;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;

import java.util.ArrayList;

//添加收藏
//2023-08-28

public class AddFavoriteActivity extends RefreshListActivity {
    FolderChooseAdapter adapter;
    ArrayList<String> folderList = new ArrayList<>();
    ArrayList<Boolean> stateList = new ArrayList<>();
    ArrayList<Long> fidList = new ArrayList<>();
    long aid;
    int RESULT_ADDED = 1;
    int RESULT_DELETED = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("添加收藏");

        Intent intent = getIntent();
        aid = intent.getLongExtra("aid", 0);

        if (Preferences.getLong(Preferences.MID, 0) == 0) {
            MsgUtil.showMsg("还没有登录喵~");
            finish();
            return;
        }

        ThreadManager.run(() -> {
            try {
                FavoriteApi.getFavoriteState(aid, folderList, fidList, stateList);

                adapter = new FolderChooseAdapter(this, folderList, fidList, stateList, aid);

                setAdapter(adapter);

                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @Override
    public void finish() {
        if (adapter != null) {
            if (adapter.added) {
                setResult(RESULT_ADDED);
            } else if (adapter.isAllDeleted()) {
                setResult(RESULT_DELETED);
            }
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            if (Preferences.getBoolean("fav_notice", false)) {
                if (adapter.added) MsgUtil.showMsg("添加成功");
                else if (adapter.changed) MsgUtil.showMsg("更改成功");
            }
        }

        super.onDestroy();

    }
}