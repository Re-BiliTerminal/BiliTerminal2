package com.huanli233.biliterminal2.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.api.CreativeCenterApi;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class CreativeCenterActivity extends BaseActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asyncInflate(R.layout.activity_creative_center, (view, id) -> ThreadManager.run(() -> {
            try {
                JSONObject stats = CreativeCenterApi.getVideoStat();
                runOnUiThread(() -> {
                    try {
                        setStatsText(R.id.totalFans_number, stats, "total_fans", "incr_fans");
                        setStatsText(R.id.totalClick_number, stats, "total_click", "incr_click");
                        setStatsText(R.id.totalLike_number, stats, "total_like", "inc_like");
                        setStatsText(R.id.totalCoin_number, stats, "total_coin", "inc_coin");
                        setStatsText(R.id.totalFavourite_number, stats, "total_fav", "inc_fav");
                        setStatsText(R.id.totalShare_number, stats, "total_share", "inc_share");
                        setStatsText(R.id.totalReply_number, stats, "total_reply", "incr_reply");
                        setStatsText(R.id.totalDm_number, stats, "total_dm", "incr_dm");
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.error(e));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.error(e));
            }

        }));
    }

    @SuppressLint("SetTextI18n")
    private void setStatsText(int viewId, JSONObject jsonObject, String totalKey, String incrKey) throws JSONException {
        TextView textView = findViewById(viewId);
        int totalValue = jsonObject.getInt(totalKey);
        int incrValue = jsonObject.getInt(incrKey);

        String totalText = Utils.toWan(totalValue);
        String incrSymbol = (incrValue < 0) ? "" : "+";
        String incrText = Utils.toWan(incrValue);

        textView.setText(totalText + incrSymbol + incrText);
    }
}
