package com.huanli233.biliterminal2.activity.live;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.huanli233.biliterminal2.activity.base.RefreshMainActivity;
import com.huanli233.biliterminal2.adapter.LiveCardAdapter;
import com.huanli233.biliterminal2.api.LiveApi;
import com.huanli233.biliterminal2.model.LiveRoom;
import com.huanli233.biliterminal2.util.CenterThreadPool;

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

        CenterThreadPool.run(() -> {
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
        CenterThreadPool.run(() -> {
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
                    setBottom(true);
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}
