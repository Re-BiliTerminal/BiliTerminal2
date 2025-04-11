package com.huanli233.biliterminal2.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.adapter.article.ArticleCardAdapter;
import com.huanli233.biliterminal2.api.SearchApi;
import com.huanli233.biliterminal2.bean.ArticleCard;
import com.huanli233.biliterminal2.util.ThreadManager;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchArticleFragment extends SearchFragment {

    private ArrayList<ArticleCard> articleCardList = new ArrayList<>();
    private ArticleCardAdapter articleCardAdapter;

    public SearchArticleFragment() {
    }

    public static SearchArticleFragment newInstance() {
        return new SearchArticleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        articleCardList = new ArrayList<>();
        articleCardAdapter = new ArticleCardAdapter(requireContext(), articleCardList);
        setAdapter(articleCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                JSONArray result = (JSONArray) SearchApi.searchType(keyword, page, "article");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    ArrayList<ArticleCard> list = new ArrayList<>();
                    SearchApi.getArticlesFromSearchResult(result, list);
                    if (list.isEmpty()) setBottom(true);
                    ThreadManager.runOnUiThread(() -> {
                        int lastSize = articleCardList.size();
                        articleCardList.addAll(list);
                        articleCardAdapter.notifyItemRangeInserted(lastSize + 1, articleCardList.size() - lastSize);
                    });
                } else setBottom(true);
            } catch (Exception e) {
                report(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        ThreadManager.runOnUiThread(() -> {
            page = 1;
            if (this.articleCardAdapter == null)
                this.articleCardAdapter = new ArticleCardAdapter(this.requireContext(), this.articleCardList);
            int size_old = this.articleCardList.size();
            this.articleCardList.clear();
            if (size_old != 0) this.articleCardAdapter.notifyItemRangeRemoved(0, size_old);
            ThreadManager.run(() -> continueLoading(page));
        });
    }
}
