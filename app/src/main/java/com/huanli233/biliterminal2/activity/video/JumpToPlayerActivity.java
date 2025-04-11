package com.huanli233.biliterminal2.activity.video;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.DownloadActivity;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.api.BangumiPlayInfo;
import com.huanli233.biliterminal2.api.HistoryApi;
import com.huanli233.biliterminal2.api.PlayerApi;
import com.huanli233.biliterminal2.api.VideoInfoApi;
import com.huanli233.biliterminal2.bean.PlayerData;
import com.huanli233.biliterminal2.bean.PlayerDataKt;
import com.huanli233.biliterminal2.player.PlayerManager;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.ThreadManager;

import org.json.JSONException;

import java.io.IOException;

public class JumpToPlayerActivity extends BaseActivity {
    String title;
    TextView textView;

    PlayerData playerData;

    int download;

    final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            int code = o.getResultCode();
            Intent result = o.getData();
            if(code == RESULT_OK && result != null){
                int progress = result.getIntExtra("progress",0);

                ThreadManager.run(() -> {
                    if (playerData.getMid() != 0 && playerData.getAid() != 0) try {
                        HistoryApi.reportHistory(playerData.getAid(), playerData.getCid(), playerData.getMid(), progress / 1000);
                    } catch (Exception ignored) {}
                });
            }
            finish();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_jump);

        textView = findViewById(R.id.text_title);

        Intent intent = getIntent();
        download = intent.getIntExtra("download", 0);

        playerData = (PlayerData) intent.getParcelableExtra("data");
        playerData.setQn(playerData.getQn() != -1 ? playerData.getQn() : Preferences.getInt("play_qn", 16));
        title = playerData.getTitle();
        requestVideo();
    }

    @SuppressLint("SetTextI18n")
    private void requestVideo() {
        ThreadManager.run(() -> {
            try {
                if (download == 0 && playerData.getProgress() == -1) {
                    Pair<Long, Integer> progressPair = VideoInfoApi.getWatchProgress(playerData.getAid());
                    playerData.setProgress(progressPair.first == playerData.getCid() ? progressPair.second : 0);
                }

                if (playerData.getType() == PlayerDataKt.TYPE_BANGUMI) {
                    BangumiPlayInfo playInfo = PlayerApi.getBangumi(playerData.getAid(), playerData.getCid(), playerData.getQn());
                    playerData.setUrlVideo(playInfo.getVideoUrl());
                    playerData.setUrlDanmaku(playInfo.getDanmakuUrl());
                    playerData.setQnStrList(playInfo.getQnStrList());
                    playerData.setQnValueList(playInfo.getQnValueList());
                } else PlayerApi.getVideo(playerData.getAid(), playerData.getCid(), playerData.getQn(), download != 0);

                try {
                    if (download != 0) {
                        jump();
                        return;
                    }
                    jump();
                } catch (Exception e){
                    MsgUtil.showMsg("没有获取到字幕");
                    jump();
                    e.printStackTrace();
                }
            } catch (IOException e) {
                setClickExit("网络错误！\n请检查你的网络连接是否正常");
                e.printStackTrace();
            } catch (JSONException e) {
                setClickExit("视频获取失败！\n可能的原因：\n1.本视频仅大会员可播放\n2.视频获取接口失效");
                e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                setClickExit("跳转失败！\n请安装对应的播放器\n或在设置中选择正确的播放器\n或将哔哩终端和播放器同时更新到最新版本");
                e.printStackTrace();
            }
        });
    }

    private void jump(){
        if(isDestroyed()) return;
        if (download == 0) {
            Intent intent = PlayerManager.playerIntent(this, playerData);
            launcher.launch(intent);
            setClickExit("等待退出播放后上报进度\n（点击跳过）");
        }
        else {
            Intent intent = new Intent();
            intent.setClass(this, DownloadActivity.class);
            intent.putExtra("type", download);
            intent.putExtra("link", playerData.getUrlVideo());
            intent.putExtra("danmaku", playerData.getUrlDanmaku());
            intent.putExtra("title", title);
            intent.putExtra("cover", getIntent().getStringExtra("cover"));
            if (download == 2)
                intent.putExtra("parent_title", getIntent().getStringExtra("parent_title"));
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setClickExit(String reason) {
        runOnUiThread(()->{
            textView.setText(reason);
            textView.setOnClickListener((view) -> finish());
        });
    }
}