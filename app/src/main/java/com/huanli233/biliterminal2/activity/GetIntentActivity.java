package com.huanli233.biliterminal2.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.util.MsgUtil;

public class GetIntentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        if (type != null) switch (intent.getStringExtra("type")) {
            case "video_av":
                BiliTerminal.jumpToVideo(this, intent.getLongExtra("content", 0));
                break;
            case "video_bv":
                BiliTerminal.jumpToVideo(this, intent.getStringExtra("content"));
                break;
            case "article":
                BiliTerminal.jumpToArticle(this, intent.getLongExtra("content", 0));
                break;
            case "user":
                BiliTerminal.jumpToUser(this, intent.getLongExtra("content", 0));
                break;
            default:
                MsgUtil.showMsgLong("不支持打开：" + type);
                break;
        }

        Uri uri = intent.getData();
        if (uri != null) {
            String host = uri.getHost();

            switch (host) {
                case "video":
                    BiliTerminal.jumpToVideo(this, Long.parseLong(uri.getLastPathSegment()));
                    break;
                case "article":
                    BiliTerminal.jumpToArticle(this, Long.parseLong(uri.getLastPathSegment()));
                    break;
                default:
                    MsgUtil.showMsgLong("不支持打开：" + host);
                    break;
            }
        }

        finish();
    }
}
