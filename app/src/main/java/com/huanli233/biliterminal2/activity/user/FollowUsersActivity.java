package com.huanli233.biliterminal2.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.user.UserListAdapter;
import com.huanli233.biliterminal2.api.FollowApi;
import com.huanli233.biliterminal2.bean.UserInfo;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

//关注列表
//2023-07-22
//2024-05-01

public class FollowUsersActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<UserInfo> userList;
    private UserListAdapter adapter;
    private int mode;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getIntent().getIntExtra("mode", 0);
        mid = getIntent().getLongExtra("mid", -1);

        if (mode < 0 || mode > 1 || mid == -1) {
            finish();
            return;
        }

        setPageName(mode == 0 ? "关注列表" : "粉丝列表");

        recyclerView.setHasFixedSize(true);

        userList = new ArrayList<>();

        ThreadManager.run(() -> {
            try {
                int result = mode == 0 ? FollowApi.getFollowingList(mid, page, userList) : FollowApi.getFollowerList(mid, page, userList);
                adapter = new UserListAdapter(this, userList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);

                if (result == 1) {
                    setBottom(true);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && (e.getMessage().startsWith("22115") || e.getMessage().startsWith("22118"))) {
                    finish();
                    MsgUtil.showMsg(e.getMessage());
                } else {
                    loadFail(e);
                }
            }
        });
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                List<UserInfo> list = new ArrayList<>();
                int result = mode == 0 ? FollowApi.getFollowingList(mid, page, list) : FollowApi.getFollowerList(mid, page, list);
                runOnUiThread(() -> {
                    userList.addAll(list);
                    adapter.notifyItemRangeInserted(userList.size() - list.size(), list.size());
                });
                if (result == 1) {
                    setBottom(true);
                }
                setRefreshing(false);
            } catch (Exception e) {
                if (e.getMessage() != null && (e.getMessage().startsWith("22115") || e.getMessage().startsWith("22118"))) {
                    finish();
                    MsgUtil.showMsg(e.getMessage());
                } else {
                    loadFail(e);
                }
            }
        });
    }
}