package com.huanli233.biliterminal2.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanli233.biliterminal2.adapter.user.UserListAdapter;
import com.huanli233.biliterminal2.api.SearchApi;
import com.huanli233.biliterminal2.model.UserInfo;
import com.huanli233.biliterminal2.util.ThreadManager;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends SearchFragment {

    private List<UserInfo> userInfoList = new ArrayList<>();
    private UserListAdapter userInfoAdapter;

    public SearchUserFragment() {
    }

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userInfoList = new ArrayList<>();
        userInfoAdapter = new UserListAdapter(requireContext(), userInfoList);
        setAdapter(userInfoAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        ThreadManager.run(() -> {
            try {
                JSONArray result = (JSONArray) SearchApi.searchType(keyword, page, "bili_user");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    List<UserInfo> list = new ArrayList<>();
                    SearchApi.getUsersFromSearchResult(result, list);
                    if (list.isEmpty()) setBottom(true);
                    ThreadManager.runOnUiThread(() -> {
                        int lastSize = userInfoList.size();
                        userInfoList.addAll(list);
                        userInfoAdapter.notifyItemRangeInserted(lastSize + 1, userInfoList.size() - lastSize);
                    });
                } else setBottom(true);
            } catch (Exception e) {
                loadFail(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        ThreadManager.runOnUiThread(() -> {
            page = 1;
            if (this.userInfoAdapter == null)
                this.userInfoAdapter = new UserListAdapter(this.requireContext(), this.userInfoList);
            int size_old = this.userInfoList.size();
            this.userInfoList.clear();
            if (size_old != 0) this.userInfoAdapter.notifyItemRangeRemoved(0, size_old);
            ThreadManager.run(() -> continueLoading(page));
        });
    }


}
