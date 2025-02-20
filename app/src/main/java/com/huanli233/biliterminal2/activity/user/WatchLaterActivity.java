package com.huanli233.biliterminal2.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.elvishew.xlog.XLog;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter;
import com.huanli233.biliterminal2.api.WatchLaterApi;
import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.CenterThreadPool;
import com.huanli233.biliterminal2.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//稍后再看
//2023-08-17

public class WatchLaterActivity extends RefreshListActivity {

    private int longClickPosition = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("稍后再看");
        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            try {
                ArrayList<VideoCard> videoCardList = WatchLaterApi.getWatchLaterList();
                VideoCardAdapter adapter = new VideoCardAdapter(this, videoCardList);

                adapter.setOnLongClickListener(position -> {
                    if (longClickPosition == position) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = WatchLaterApi.delete(videoCardList.get(position).getAid());
                                longClickPosition = -1;
                                if (result == 0) runOnUiThread(() -> {
                                    MsgUtil.showMsg("删除成功");
                                    videoCardList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, videoCardList.size() - position);
                                });
                                else
                                    runOnUiThread(() -> MsgUtil.showMsg("删除失败，错误码：" + result));
                            } catch (Exception e) {
                                XLog.e(e);
                            }
                        });
                    } else {
                        longClickPosition = position;
                        MsgUtil.showMsg("再次长按删除");
                    }
                });

                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

}