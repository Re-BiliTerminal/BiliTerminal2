package com.huanli233.biliterminal2.activity.video.series;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter;
import com.huanli233.biliterminal2.api.SeriesApi;
import com.huanli233.biliterminal2.bean.PageInfo;
import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;

public class SeriesInfoActivity extends RefreshListActivity {

    private String type;
    private long mid;
    private int sid;
    private ArrayList<VideoCardKt> videoList;
    private VideoCardAdapter videoCardAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        mid = intent.getLongExtra("mid", 0);
        sid = intent.getIntExtra("sid", 0);
        String name = intent.getStringExtra("name");

        setPageName(name);

        videoList = new ArrayList<>();

        ThreadManager.run(() -> {
            try {
                PageInfo pageInfo = SeriesApi.getSeriesInfo(type, mid, sid, page, videoList);
                if (pageInfo.return_ps != 0) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);

                    setOnLoadMoreListener(this::continueLoading);
                    setAdapter(videoCardAdapter);

                    if (pageInfo.return_ps < pageInfo.require_ps) {
                        setBottom(true);
                    }
                } else showEmptyView();
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                int lastSize = videoList.size();
                PageInfo pageInfo = SeriesApi.getSeriesInfo(type, mid, sid, page, videoList);
                runOnUiThread(() -> videoCardAdapter.notifyItemRangeInserted(lastSize, pageInfo.return_ps));

                if (pageInfo.return_ps < pageInfo.require_ps || pageInfo.return_ps == 0) {
                    setBottom(true);
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}
