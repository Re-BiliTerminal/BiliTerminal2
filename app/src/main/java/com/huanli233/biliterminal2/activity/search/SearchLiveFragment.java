package com.huanli233.biliterminal2.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.adapter.LiveCardAdapter;
import com.huanli233.biliterminal2.api.LiveApi;
import com.huanli233.biliterminal2.api.SearchApi;
import com.huanli233.biliterminal2.model.LiveRoom;
import com.huanli233.biliterminal2.util.ThreadManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchLiveFragment extends SearchFragment {

    private ArrayList<LiveRoom> roomList = new ArrayList<>();
    private LiveCardAdapter liveCardAdapter;

    public SearchLiveFragment() {
    }

    public static SearchLiveFragment newInstance() {
        return new SearchLiveFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomList = new ArrayList<>();
        liveCardAdapter = new LiveCardAdapter(requireContext(), roomList);
        setAdapter(liveCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                Object result = SearchApi.searchType(keyword, page, "live");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    JSONArray jsonArray = null;
                    if (result instanceof JSONObject)
                        jsonArray = ((JSONObject) result).optJSONArray("live_room");
                    else if (result instanceof JSONArray) jsonArray = (JSONArray) result;

                    List<LiveRoom> list = new ArrayList<>();
                    if (jsonArray != null) list.addAll(LiveApi.analyzeLiveRooms(jsonArray));
                    if (list.isEmpty()) setBottom(true);
                    else ThreadManager.runOnUiThread(() -> {
                        int lastSize = roomList.size();
                        roomList.addAll(list);
                        liveCardAdapter.notifyItemRangeInserted(lastSize + 1, roomList.size() - lastSize);
                    });
                } else setBottom(true);
            } catch (Exception e) {
                report(e);
            }
            setRefreshing(false);
            if (bottom && roomList.isEmpty()) {
                showEmptyView(true);
            }
        });
    }

    public void refreshInternal() {
        ThreadManager.runOnUiThread(() -> {
            page = 1;
            if (this.liveCardAdapter == null)
                this.liveCardAdapter = new LiveCardAdapter(this.requireContext(), this.roomList);
            int size_old = this.roomList.size();
            this.roomList.clear();
            if (size_old != 0) this.liveCardAdapter.notifyItemRangeRemoved(0, size_old);
            ThreadManager.run(() -> continueLoading(page));
        });
    }

}
