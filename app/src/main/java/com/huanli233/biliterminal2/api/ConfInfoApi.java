package com.huanli233.biliterminal2.api;

import android.net.Uri;
import android.util.Log;

import com.huanli233.biliterminal2.util.FileUtil;
import com.huanli233.biliterminal2.util.NetWorkUtil;
import com.huanli233.biliterminal2.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.HttpUrl;

public class ConfInfoApi {

    private static final int[] MIXIN_KEY_ENC_TAB = {46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
            33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
            61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
            36, 20, 34, 44, 52};

    public static String getWBIRawKey() throws IOException, JSONException {
        JSONObject getJson = NetWorkUtil.getJson("https://api.bilibili.com/x/web-interface/nav");
        JSONObject wbi_img = getJson.getJSONObject("data").getJSONObject("wbi_img");
        String img_key = FileUtil.getFileFirstName(FileUtil.getFileNameFromLink(wbi_img.getString("img_url")));
        String sub_key = FileUtil.getFileFirstName(FileUtil.getFileNameFromLink(wbi_img.getString("sub_url")));

        return img_key + sub_key;  //相连
    }

    public static String getWBIMixinKey(String raw_key) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            key.append(raw_key.charAt(MIXIN_KEY_ENC_TAB[i]));
        }

        return key.toString();
    }

    public static String signWBI(String url_query) throws JSONException, IOException {
        String mixin_key;
        int curr = getDateCurr();
        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.WBI_LAST_UPDATED, 0) < curr) {
            SharedPreferencesUtil.putLong(SharedPreferencesUtil.WBI_LAST_UPDATED, curr);

            mixin_key = ConfInfoApi.getWBIMixinKey(ConfInfoApi.getWBIRawKey());
            SharedPreferencesUtil.putString(SharedPreferencesUtil.WBI_MIXIN_KEY, mixin_key);
        } else mixin_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.WBI_MIXIN_KEY, "");

        String wts = String.valueOf(System.currentTimeMillis() / 1000);
        String calc_str = sortUrlParams(Uri.encode(url_query, "@#&=*+-_.,:!?()/~'%") + "&wts=" + wts) + mixin_key;
        Log.e("calc_str", calc_str);

        String w_rid = md5(calc_str);

        return Objects.requireNonNull(HttpUrl.parse(url_query)).newBuilder().addQueryParameter("w_rid", w_rid).addQueryParameter("wts", wts).build().toString();
    }

    public static String sortUrlParams(String url) {
        String encodedParam = Objects.requireNonNull(HttpUrl.parse(url)).encodedQuery();
        if (encodedParam == null) encodedParam = "";
        // 解析URL参数
        Map<String, String> paramMap = new HashMap<>();
        String[] params = encodedParam.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                paramMap.put(keyValue[0], "");
            }
        }

        // 使用TreeMap对参数进行排序
        Map<String, String> sortedMap = new TreeMap<>(paramMap);

        // 构建排序后的URL
        StringBuilder sortedUrl = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            if (!isFirst) {
                sortedUrl.append("&");
            } else {
                isFirst = false;
            }
            sortedUrl.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return sortedUrl.toString();
    }

    private static String md5(String plainText) {
        byte[] secretBytes;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }


    public static int getDateCurr() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);
    }
}
