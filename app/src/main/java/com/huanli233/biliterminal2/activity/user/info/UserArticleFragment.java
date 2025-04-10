package com.huanli233.biliterminal2.activity.user.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.activity.base.RefreshListFragment;
import com.huanli233.biliterminal2.adapter.article.ArticleCardAdapter;
import com.huanli233.biliterminal2.api.UserInfoApi;
import com.huanli233.biliterminal2.bean.ArticleCard;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;
import java.util.List;

public class UserArticleFragment extends RefreshListFragment {

    private long mid;
    private ArrayList<ArticleCard> articleList;
    private ArticleCardAdapter adapter;

    public UserArticleFragment() {
    }

    public static UserArticleFragment newInstance(long mid) {
        UserArticleFragment fragment = new UserArticleFragment();
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

        articleList = new ArrayList<>();
        onLoadMore(this::continueLoading);

        ThreadManager.run(() -> {
            try {
                bottomReached = (UserInfoApi.getUserArticles(mid, page, articleList) == 1);
                if (isAdded()) {
                    adapter = new ArticleCardAdapter(requireContext(), articleList);
                    setAdapter(adapter);
                    setRefreshing(false);
                    if (bottomReached && articleList.isEmpty()) showEmptyView();
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
                List<ArticleCard> list = new ArrayList<>();
                int result = UserInfoApi.getUserArticles(mid, page, list);
                if (result != -1) {
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        articleList.addAll(list);
                        adapter.notifyItemRangeInserted(articleList.size() - list.size(), list.size());
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