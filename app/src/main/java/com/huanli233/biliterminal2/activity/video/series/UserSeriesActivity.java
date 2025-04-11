package com.huanli233.biliterminal2.activity.video.series;

import android.os.Bundle;

import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.video.SeriesCardAdapter;
import com.huanli233.biliterminal2.api.SeriesApi;
import com.huanli233.biliterminal2.bean.Series;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;

public class UserSeriesActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<Series> seriesList;
    private SeriesCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("投稿的系列");

        seriesList = new ArrayList<>();
        mid = getIntent().getLongExtra("mid", 0);

        setOnLoadMoreListener(this::continueLoading);

        ThreadManager.run(() -> {
            try {
                bottom = (SeriesApi.getUserSeries(mid, page, seriesList) == 1);
                setRefreshing(false);
                adapter = new SeriesCardAdapter(this, seriesList);
                setAdapter(adapter);
                if (bottom && seriesList.isEmpty()) showEmptyView();
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                int last = seriesList.size();
                int result = SeriesApi.getUserSeries(mid, page, seriesList);
                if (result != -1) {
                    runOnUiThread(() -> adapter.notifyItemRangeInserted(last, seriesList.size() - last));
                    if (result == 1) {
                        bottom = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}