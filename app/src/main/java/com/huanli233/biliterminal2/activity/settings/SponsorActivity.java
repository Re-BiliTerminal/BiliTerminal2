package com.huanli233.biliterminal2.activity.settings;

import android.os.Bundle;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.user.UserListAdapter;
import com.huanli233.biliterminal2.api.AppInfoApi;
import com.huanli233.biliterminal2.model.UserInfo;
import com.huanli233.biliterminal2.util.CenterThreadPool;
import com.huanli233.biliterminal2.util.MsgUtil;

import java.util.ArrayList;

public class SponsorActivity extends RefreshListActivity {

    private ArrayList<UserInfo> userList;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("捐赠列表");

        userList = new ArrayList<>();
        userList.add(new UserInfo(-1, getString(R.string.donate_title), "",
                getString(R.string.donate_desc),
                -1, -1, 6, true, "", 0, "", 0));

        CenterThreadPool.run(() -> {
            try {
                int result = AppInfoApi.getSponsors(userList, this.page);
                adapter = new UserListAdapter(this, userList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);

                if (result == 1) setBottom(true);
            } catch (Exception e) {
                report(e);
                runOnUiThread(() -> MsgUtil.showMsg("连接到哔哩终端接口时发生错误"));
                setRefreshing(false);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                int lastSize = userList.size();
                int result = AppInfoApi.getSponsors(userList, page);
                runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, userList.size() - lastSize));
                setRefreshing(false);

                if (result == 1) setBottom(true);
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.showMsg("连接到哔哩终端接口时发生错误"));
                loadFail(e);
            }
        });
    }
}
