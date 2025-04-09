package com.huanli233.biliterminal2.activity.user.favorite;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.favorite.FavoriteFolderAdapter;
import com.huanli233.biliterminal2.api.FavoriteApi;
import com.huanli233.biliterminal2.model.FavoriteFolder;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.Preferences;

import java.util.ArrayList;

//收藏夹列表
//2023-08-07
//2024-07-25

public class FavoriteFolderListActivity extends RefreshListActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("收藏");

        long mid = Preferences.getLong("mid", 0);

        ThreadManager.run(() -> {
            try {
                ArrayList<FavoriteFolder> folderList = FavoriteApi.getFavoriteFolders(mid);
                FavoriteFolderAdapter adapter = new FavoriteFolderAdapter(this, folderList, mid);
                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });

    }
}