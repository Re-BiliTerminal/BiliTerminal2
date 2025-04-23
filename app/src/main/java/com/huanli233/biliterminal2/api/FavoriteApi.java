package com.huanli233.biliterminal2.api;

import android.util.Pair;

import com.huanli233.biliterminal2.bean.Collection;
import com.huanli233.biliterminal2.bean.FavoriteFolder;
import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//收藏API

public class FavoriteApi {
    // TODO 合集收藏

    public static ArrayList<FavoriteFolder> getFavoriteFolders(long mid) throws IOException, JSONException {
        String url = "https://space.bilibili.com/ajax/fav/getBoxList?mid=" + mid;
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");
        ArrayList<FavoriteFolder> folderList = new ArrayList<>();
        if (data.has("list") && !data.isNull("list")) {
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject folder = list.getJSONObject(i);

                FavoriteFolder favoriteFolder = new FavoriteFolder();

                favoriteFolder.id = folder.getLong("fav_box");
                favoriteFolder.name = folder.getString("name");

                if (!folder.isNull("videos"))
                    favoriteFolder.cover = folder.getJSONArray("videos").getJSONObject(0).getString("pic");
                else favoriteFolder.cover = "";

                favoriteFolder.videoCount = folder.getInt("count");
                favoriteFolder.maxCount = folder.getInt("max_count");
                folderList.add(favoriteFolder);
            }
        }
        return folderList;
    }

    /**
     * 获取收藏的合集
     *
     * @param mid            目标用户
     * @param page           页数
     * @param collectionList collection对象List
     * @return 返回码与has_more
     */
    public static Pair<Integer, Boolean> getFavoritedCollections(long mid, int page, List<Collection> collectionList) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/v3/fav/folder/collected/list" + new NetWorkUtil.FormData()
                .setUrlParam(true)
                .put("platform", "web")
                .put("up_mid", mid)
                .put("pn", page)
                .put("ps", 10);
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.optJSONObject("data");
        boolean has_more = false;
        if (data != null) {
            has_more = data.optBoolean("has_more", false);
            JSONArray list = data.optJSONArray("list");
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    Collection collection = new Collection();
                    collection.id = item.optInt("id", -1);
                    collection.mid = item.optLong("mid", -1);
                    collection.title = item.optString("title");
                    collection.cover = item.optString("cover");
                    collection.intro = item.optString("intro");
                    collection.view = Utils.toWan(item.optInt("view_count", -1));
                    collectionList.add(collection);
                }
            }
        }
        return new Pair<>(result.optInt("code", -1), has_more);
    }

    public static int getFolderVideos(long mid, long fid, int page, ArrayList<VideoCardKt> videoList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/space/fav/arc?vmid=" + mid
                + "&ps=30&fid=" + fid + "&tid=0&keyword=&pn=" + page + "&order=fav_time";
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");
        if (data.has("archives") && !data.isNull("archives")) {
            JSONArray archives = data.getJSONArray("archives");
            if (archives.length() != 0) {
                for (int i = 0; i < archives.length(); i++) {
                    JSONObject video = archives.getJSONObject(i);
                    String title = video.getString("title");
                    String cover = video.getString("pic");
                    long aid = video.getLong("aid");

                    JSONObject owner = video.getJSONObject("owner");
                    String upName = owner.getString("name");

                    JSONObject stat = video.getJSONObject("stat");
                    String view = Utils.toWan(stat.getLong("view")) + "观看";
                    VideoCardKt card = new VideoCardKt();
                    card.setTitle(title);
                    card.setCover(cover);
                    card.setAid(aid);
                    card.setUploader(upName);
                    card.setView(view);
                    videoList.add(VideoCardKt.of(title, upName, view, cover, aid, ""));
                }
                return 0;
            } else return 1;
        } else return -1;
    }

    public static void getFavoriteState(long aid, ArrayList<String> folderList, ArrayList<Long> fidList, ArrayList<Boolean> stateList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&jsonp=jsonp&rid=" + aid + "&up_mid=" + Preferences.getLong("mid", 0);
        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");

        if (data.has("list") && !data.isNull("list")) {
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject folder = list.getJSONObject(i);
                folderList.add(folder.getString("title"));
                fidList.add(folder.getLong("fid"));
                stateList.add(folder.getInt("fav_state") == 1);
            }
        }
    }

    public static int addFavorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(Preferences.getLong("mid", 0));
        String addFid = fid + strMid.substring(strMid.length() - 2);
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/deal";
        String per = "rid=" + aid + "&type=2&add_media_ids=" + addFid + "&del_media_ids=&csrf=" + Preferences.getString(Preferences.CSRF, "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        return result.getInt("code");
    }


    public static int deleteFavorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(Preferences.getLong("mid", 0));
        String delFid = fid + strMid.substring(strMid.length() - 2);
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/batch/del";
        String per = "resources=" + aid + ":2&media_id=" + delFid + "&csrf=" + Preferences.getString(Preferences.CSRF, "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        return result.getInt("code");
    }

}
