package com.huanli233.biliterminal2.activity.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.listener.OnLoadMoreListener;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.view.ImageAutoLoadScrollListener;

public class RefreshMainActivity extends InstanceActivity {
    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;
    public OnLoadMoreListener listener;
    public boolean bottomReached = false;
    public int page = 1;
    public long lastLoadTimestamp;
    protected boolean isRefreshing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (listener != null && !recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing() && newState == RecyclerView.SCROLL_STATE_DRAGGING && !bottomReached) {
                    goOnLoad();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (listener != null) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert manager != null;
                    int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                    int itemCount = manager.getItemCount();
                    if (lastItemPosition >= (itemCount - 3) && dy > 0 && !swipeRefreshLayout.isRefreshing() && !isRefreshing && !bottomReached) {// 滑动到倒数第三个就可以刷新了
                        goOnLoad();
                    }
                }
            }
        });
        ImageAutoLoadScrollListener.install(recyclerView);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        runOnUiThread(() -> recyclerView.setAdapter(adapter));
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        swipeRefreshLayout.setOnRefreshListener(listener);
        swipeRefreshLayout.setEnabled(true);
    }

    public void setRefreshing(boolean bool) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
        isRefreshing = bool;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMore) {
        listener = loadMore;
    }

    private void goOnLoad() {
        synchronized (this) {
            long timeCurrent = System.currentTimeMillis();
            if (timeCurrent - lastLoadTimestamp > 100) {
                swipeRefreshLayout.setRefreshing(true);
                page++;
                listener.onLoad(page);
                lastLoadTimestamp = timeCurrent;
            }
        }
    }

    public void setBottomReached(boolean bool) {
        bottomReached = bool;
    }

    public void loadFail() {
        page--;
        MsgUtil.showMsgLong("加载失败");
        setRefreshing(false);
    }

    public void loadFail(Throwable e) {
        page--;
        report(e);
        setRefreshing(false);
    }
}
