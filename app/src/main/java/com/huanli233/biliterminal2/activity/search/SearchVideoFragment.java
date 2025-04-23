package com.huanli233.biliterminal2.activity.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter;
import com.huanli233.biliterminal2.api.SearchApi;
import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.ThreadManager;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchVideoFragment extends SearchFragment {
    private ArrayList<VideoCardKt> videoCardList = new ArrayList<>();
    private VideoCardAdapter videoCardAdapter;

    public SearchVideoFragment() {
    }

    public static SearchVideoFragment newInstance() {
        return new SearchVideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoCardList = new ArrayList<>();
        videoCardAdapter = new VideoCardAdapter(requireContext(), videoCardList);
        setAdapter(videoCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                JSONArray result = SearchApi.search(keyword, page);
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    ArrayList<VideoCardKt> list = new ArrayList<>();
                    SearchApi.getVideosFromSearchResult(result, list, page == 1);
                    if (list.isEmpty()) setBottom(true);
                    else ThreadManager.runOnUiThread(() -> {
                        int lastSize = videoCardList.size();
                        videoCardList.addAll(list);
                        videoCardAdapter.notifyItemRangeInserted(lastSize + 1, videoCardList.size() - lastSize);
                    });
                } else setBottom(true);
            } catch (Exception e) {
                e.printStackTrace();
                loadFail(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        ThreadManager.runOnUiThread(() -> {
            page = 1;
            if (this.videoCardAdapter == null)
                this.videoCardAdapter = new VideoCardAdapter(this.requireContext(), this.videoCardList);
            int size_old = this.videoCardList.size();
            this.videoCardList.clear();
            if (size_old != 0) this.videoCardAdapter.notifyItemRangeRemoved(0, size_old);
            ThreadManager.run(() -> continueLoading(page));
        });
    }
}
