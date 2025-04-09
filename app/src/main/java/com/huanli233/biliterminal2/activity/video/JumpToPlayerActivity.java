package com.huanli233.biliterminal2.activity.video;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.DownloadActivity;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.api.HistoryApi;
import com.huanli233.biliterminal2.api.PlayerApi;
import com.huanli233.biliterminal2.api.VideoInfoApi;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;

import org.json.JSONException;

import java.io.IOException;

public class JumpToPlayerActivity extends BaseActivity {
    String videourl;
    String danmakuurl;
    String subtitleurl = "";
    String title;
    TextView textView;

    String bvid;
    long aid, cid, mid;

    int qn;

    int download;

    int progress;

    final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            int code = o.getResultCode();
            Intent result = o.getData();
            if (code == RESULT_OK && result != null) {
                int progress = result.getIntExtra("progress", 0);

                ThreadManager.run(() -> {
                    if (mid != 0 && aid != 0) try {
                        HistoryApi.reportHistory(aid, cid, mid, progress / 1000);
                    } catch (Exception e) {
                        MsgUtil.error("进度上报：", e);
                    }
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
        bvid = intent.getStringExtra("bvid");
        aid = intent.getLongExtra("aid", 0);
        cid = intent.getLongExtra("cid", 0);
        mid = intent.getLongExtra("mid", 0);
        progress = intent.getIntExtra("progress", -1);

        title = intent.getStringExtra("title");
        download = intent.getIntExtra("download", 0);

        qn = intent.getIntExtra("qn", -1);

        danmakuurl = "https://comment.bilibili.com/" + cid + ".xml";

        requestVideo(qn != -1 ? qn : Preferences.getInt("play_qn", 16));
    }

    @SuppressLint("SetTextI18n")
    private void requestVideo(int qn) {
        ThreadManager.run(() -> {
            try {
                if (download == 0 && progress == -1) {
                    Pair<Long, Integer> progressPair = VideoInfoApi.getWatchProgress(aid);
                    progress = progressPair.first == cid ? progressPair.second : 0;
                }
                Pair<String, String> video = PlayerApi.getVideo(aid, cid, qn, download != 0);
                videourl = video.first;

                try {
                    if (download != 0) {
                        jump();
                        return;
                    }
                    jump();
                } catch (Exception e) {
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

    private void jump() {
        if (isDestroyed()) return;
        if (download != 0) {
            Intent intent = new Intent();
            intent.setClass(this, DownloadActivity.class);
            intent.putExtra("type", download);
            intent.putExtra("link", videourl);
            intent.putExtra("danmaku", danmakuurl);
            intent.putExtra("title", title);
            intent.putExtra("cover", getIntent().getStringExtra("cover"));
            if (download == 2)
                intent.putExtra("parent_title", getIntent().getStringExtra("parent_title"));
            startActivity(intent);
        } else {
            Intent intent = PlayerApi.jumpToPlayer(this, videourl, danmakuurl, subtitleurl, title, false, aid, bvid, cid, mid, progress, false);
            launcher.launch(intent);
        }
        setClickExit("等待退出播放后上报进度\n（点一下返回）");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setClickExit(String reason) {
        runOnUiThread(() -> {
            textView.setText(reason);
            textView.setOnClickListener((view) -> finish());
        });
    }
}