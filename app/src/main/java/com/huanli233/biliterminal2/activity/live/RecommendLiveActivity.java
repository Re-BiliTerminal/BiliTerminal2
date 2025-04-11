package com.huanli233.biliterminal2.activity.live;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshMainActivity;
import com.huanli233.biliterminal2.adapter.LiveCardAdapter;
import com.huanli233.biliterminal2.api.LiveApi;
import com.huanli233.biliterminal2.bean.LiveRoom;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;
import java.util.List;

public class RecommendLiveActivity extends RefreshMainActivity {
    private List<LiveRoom> roomList;
    private LiveCardAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("推荐直播");

        recyclerView.setHasFixedSize(true);

        roomList = new ArrayList<>();

        setMenuClick();

        ThreadManager.run(() -> {
            try {
                roomList = LiveApi.getRecommend(page);
                adapter = new LiveCardAdapter(this, roomList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                List<LiveRoom> list;
                list = LiveApi.getRecommend(page);
                runOnUiThread(() -> {
                    if (list != null) {
                        roomList.addAll(list);
                        adapter.notifyItemRangeInserted(roomList.size() - list.size(), list.size());
                    }
                });
                if (list != null && list.isEmpty()) {
                    setBottomReached(true);
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}
