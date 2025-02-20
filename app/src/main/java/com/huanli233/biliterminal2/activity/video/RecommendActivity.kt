package com.huanli233.biliterminal2.activity.video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshMainActivity;
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter;
import com.huanli233.biliterminal2.api.RecommendApi;
import com.huanli233.biliterminal2.helper.TutorialHelper;
import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

public class RecommendActivity extends RefreshMainActivity {

    private List<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuClick();
        Log.e("debug", "进入推荐页");

        setOnRefreshListener(this::refreshRecommend);
        setOnLoadMoreListener(page -> addRecommend());

        setPageName("推荐");

        recyclerView.setHasFixedSize(true);

        TutorialHelper.showTutorialList(this, R.array.tutorial_recommend, 0);

        refreshRecommend();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshRecommend() {
        Log.e("debug", "刷新");
        if (firstRefresh) {
            videoCardList = new ArrayList<>();
        } else {
            int last = videoCardList.size();
            videoCardList.clear();
            videoCardAdapter.notifyItemRangeRemoved(0, last);
        }

        addRecommend();
    }

    private void addRecommend() {
        Log.e("debug", "加载下一页");
        CenterThreadPool.run(() -> {
            try {
                List<VideoCard> list = new ArrayList<>();
                RecommendApi.getRecommend(list);
                setRefreshing(false);

                runOnUiThread(() -> {
                    videoCardList.addAll(list);
                    if (firstRefresh) {
                        firstRefresh = false;
                        videoCardAdapter = new VideoCardAdapter(this, videoCardList);
                        setAdapter(videoCardAdapter);
                    } else {
                        videoCardAdapter.notifyItemRangeInserted(videoCardList.size() - list.size(), list.size());
                    }
                });
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}