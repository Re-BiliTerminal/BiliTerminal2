package com.huanli233.biliterminal2.activity.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.adapter.video.PageChooseAdapter;
import com.huanli233.biliterminal2.api.PlayerApi;
import com.huanli233.biliterminal2.bean.VideoInfo;
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager;
import com.huanli233.biliterminal2.util.FileUtil;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.TerminalContext;

import java.io.File;

//分页视频选集
//2023-07-17

public class MultiPageActivity extends BaseActivity {
    private static final String TAG = "MultiPageActivity";

    boolean play_clicked;
    VideoInfo videoInfo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top_bar).setOnClickListener(view -> finish());

        TextView textView = findViewById(R.id.page_name);
        textView.setText("请选择分页");

        Intent intent = getIntent();
        long aid = intent.getLongExtra("aid", 0);
        String bvid = intent.getStringExtra("bvid");
        TerminalContext.getInstance().getVideoInfoByAidOrBvId(aid, bvid).observe(this, result -> result.onSuccess((videoInfo -> {
            this.videoInfo = videoInfo;
            PageChooseAdapter adapter = new PageChooseAdapter(this, videoInfo.pagenames);

            if (intent.getIntExtra("download", 0) == 1) {    //下载模式
                adapter.setOnItemClickListener(position -> {
                    File rootPath = new File(FileUtil.getDownloadPath(), FileUtil.stringToFile(videoInfo.title));
                    File downPath = new File(rootPath, FileUtil.stringToFile(videoInfo.pagenames.get(position)));
                    if (downPath.exists()) {
                        File file_sign = new File(downPath, ".DOWNLOADING");
                        MsgUtil.showMsg(file_sign.exists() ? "已在下载队列" : "已下载完成");
                    } else {
                        startActivity(
                                new Intent()
                                        .putExtra("page", position)
                                        .setClass(this, QualityChooserActivity.class)
                                        .putExtra("aid", videoInfo.aid)
                                        .putExtra("bvid", videoInfo.bvid)
                        );
                    }
                });
            } else {        //普通播放模式
                int progress = intent.getIntExtra("progress", -1);
                long progress_cid = intent.getLongExtra("progress_cid", 0);
                adapter.setOnItemClickListener(position -> {
                    long cid = videoInfo.cids.get(position);
                    PlayerApi.startGettingUrl(this, videoInfo, position, (progress_cid == cid && !play_clicked) ? progress : -1);
                    play_clicked = true;
                });
            }

            recyclerView.setLayoutManager(new CustomLinearManager(this));
            recyclerView.setAdapter(adapter);
        })));

    }

}