package com.huanli233.biliterminal2.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.activity.base.RefreshListFragment;
import com.huanli233.biliterminal2.adapter.video.UserVideoAdapter;
import com.huanli233.biliterminal2.api.UserInfoApi;
import com.huanli233.biliterminal2.bean.VideoCard;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;
import java.util.List;

public class UserVideoFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<VideoCard> videoList;
    private UserVideoAdapter adapter;

    public static UserVideoFragment newInstance(long mid) {
        UserVideoFragment fragment = new UserVideoFragment();
        Bundle args = new Bundle();
        args.putLong("mid", mid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mid = getArguments().getLong("mid");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoList = new ArrayList<>();
        onLoadMore(this::continueLoading);

        ThreadManager.run(() -> {
            try {
                bottomReached = (UserInfoApi.getUserVideos(mid, page, "", videoList) == 1);
                if (isAdded()) {
                    setRefreshing(false);
                    adapter = new UserVideoAdapter(requireContext(), mid, videoList);
                    setAdapter(adapter);
                    if (bottomReached && videoList.isEmpty()) showEmptyView();
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                List<VideoCard> list = new ArrayList<>();
                int result = UserInfoApi.getUserVideos(mid, page, "", list);
                if (result != -1) {
                    runOnUiThread(() -> {
                        videoList.addAll(list);
                        adapter.notifyItemRangeInserted(videoList.size() - list.size(), list.size());
                    });
                    if (result == 1) {
                        bottomReached = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}