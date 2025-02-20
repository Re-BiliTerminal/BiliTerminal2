package com.huanli233.biliterminal2.activity.user;

import android.os.Bundle;
import android.util.Log;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter;
import com.huanli233.biliterminal2.api.BangumiApi;
import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

//追番列表
//2024-06-07

public class FollowingBangumisActivity extends RefreshListActivity {

    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("追番列表");

        recyclerView.setHasFixedSize(true);

        videoList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            try {
                int result = BangumiApi.getFollowingList(page, videoList);
                if (result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);
                    setOnLoadMoreListener(this::continueLoading);
                    setRefreshing(false);
                    setAdapter(videoCardAdapter);

                    if (result == 1) {
                        setBottom(true);
                    }
                }

            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                List<VideoCard> list = new ArrayList<>();
                int result = BangumiApi.getFollowingList(page, list);
                if (result != -1) {
                    runOnUiThread(() -> {
                        videoList.addAll(list);
                        videoCardAdapter.notifyItemRangeInserted(videoList.size() - list.size(), list.size());
                    });
                    if (result == 1) {
                        setBottom(true);
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}