package com.huanli233.biliterminal2.util;

import android.content.Intent;
import android.view.View;

import com.huanli233.biliterminal2.activity.ImageViewerActivity;

import java.util.ArrayList;
import java.util.List;

public class ImageViewer {
    public static ImageViewer from(String url) {
        return new ImageViewer(new ArrayList<>(List.of(url)));
    }

    public static ImageViewer from(List<String> urls) {
        return new ImageViewer(new ArrayList<>(urls));
    }

    private final ArrayList<String> urlList;

    private ImageViewer(ArrayList<String> urlList) {
        this.urlList = urlList;
    }

    public void into(View view) {
        view.setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), ImageViewerActivity.class);
            intent.putExtra("imageList", urlList);
            v.getContext().startActivity(intent);
        });
    }
}
