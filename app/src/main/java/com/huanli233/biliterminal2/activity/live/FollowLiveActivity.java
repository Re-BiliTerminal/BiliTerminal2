package com.huanli233.biliterminal2.activity.live;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.LiveCardAdapter;
import com.huanli233.biliterminal2.api.LiveApi;
import com.huanli233.biliterminal2.bean.LiveRoom;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;
import java.util.List;

public class FollowLiveActivity extends RefreshListActivity {
    private List<LiveRoom> roomList;
    private LiveCardAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("我关注的直播");

        recyclerView.setHasFixedSize(true);

        roomList = new ArrayList<>();

        ThreadManager.run(() -> {
            try {
                roomList = LiveApi.getFollowed(page);
                adapter = new LiveCardAdapter(this, roomList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);
                if (roomList.isEmpty()) showEmptyView();
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                List<LiveRoom> list;
                list = LiveApi.getFollowed(page);
                runOnUiThread(() -> {
                    if (list != null) {
                        roomList.addAll(list);
                        adapter.notifyItemRangeInserted(roomList.size() - list.size(), list.size());
                    }
                });
                if (list != null && list.isEmpty()) {
                    setBottom(true);
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}
