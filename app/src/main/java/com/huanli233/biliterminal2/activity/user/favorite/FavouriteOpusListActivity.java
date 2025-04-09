package com.huanli233.biliterminal2.activity.user.favorite;

import android.os.Bundle;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.article.OpusCardAdapter;
import com.huanli233.biliterminal2.api.FavoriteApi;
import com.huanli233.biliterminal2.model.OpusCard;
import com.huanli233.biliterminal2.util.ThreadManager;

import java.util.ArrayList;

public class FavouriteOpusListActivity extends RefreshListActivity {
    ArrayList<OpusCard> list;
    OpusCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName(getString(R.string.opus_favorite_collection));

        list = new ArrayList<>();

        ThreadManager.run(() -> {
            try {
                FavoriteApi.getFavouriteOpus(list, page);
                adapter = new OpusCardAdapter(this, list);
                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });

        setOnLoadMoreListener(this::loadMore);
    }

    public void loadMore(int page) {
        ThreadManager.run(() -> {
            try {
                int lastSize = list.size();
                setBottom(!FavoriteApi.getFavouriteOpus(list, page));
                runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, list.size() - lastSize));
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }

        });
    }
}
