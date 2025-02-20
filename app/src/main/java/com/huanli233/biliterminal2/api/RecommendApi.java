package com.huanli233.biliterminal2.api;

import android.text.TextUtils;

import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.NetWorkUtil;
import com.huanli233.biliterminal2.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecommendApi {


    public static void getPopular(List<VideoCard> videoCardList, int page) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/web-interface/popular?pn=" + page + "&ps=10";

        JSONObject result = NetWorkUtil.getJson(url);  //得到一整个json

        if (result.has("data") && !result.isNull("data")) {
            if (result.getJSONObject("data").has("list")) {
                JSONArray list = result.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject card = list.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.setAid(card.getLong("aid"));
                    videoCard.setCover(card.getString("pic"));
                    videoCard.setTitle(card.getString("title"));
                    videoCard.setUploader(card.getJSONObject("owner").getString("name"));
                    videoCard.setView(ToolsUtil.toWan(card.getJSONObject("stat").getLong("view")) + "观看");
                    videoCardList.add(videoCard);
                }
            }
        }
    }

    public static void getPrecious(List<VideoCard> videoCardList, int page) throws JSONException, IOException {
        //热门接口在携带Cookie时返回的数据的排行是个性化的

        String url = "https://api.bilibili.com/x/web-interface/popular/precious?page=" + page + "&page_size=10";

        JSONObject result = NetWorkUtil.getJson(url);  //得到一整个json

        if (result.has("data") && !result.isNull("data")) {
            if (result.getJSONObject("data").has("list")) {
                JSONArray list = result.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject card = list.getJSONObject(i);
                    VideoCard videoCard = new VideoCard();
                    videoCard.setAid(card.getLong("aid"));
                    videoCard.setCover(card.getString("pic"));
                    videoCard.setTitle(card.getString("title"));
                    videoCard.setUploader(card.getJSONObject("owner").getString("name"));
                    videoCard.setView(ToolsUtil.toWan(card.getJSONObject("stat").getLong("view")) + "观看");
                    videoCardList.add(videoCard);
                }
            }
        }
    }
}
