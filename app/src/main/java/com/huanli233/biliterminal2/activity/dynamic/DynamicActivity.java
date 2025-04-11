package com.huanli233.biliterminal2.activity.dynamic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.activity.base.RefreshMainActivity;
import com.huanli233.biliterminal2.adapter.dynamic.DynamicAdapter;
import com.huanli233.biliterminal2.adapter.dynamic.DynamicHolder;
import com.huanli233.biliterminal2.api.DynamicApi;
import com.huanli233.biliterminal2.helper.TutorialHelper;
import com.huanli233.biliterminal2.bean.Dynamic;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//动态页面
//2023-09-17

public class DynamicActivity extends RefreshMainActivity {

    private ArrayList<Dynamic> dynamicList;
    private DynamicAdapter dynamicAdapter;
    private long offset = 0;
    private boolean firstRefresh = true;
    private String type = "all";
    private static final Map<String, String> typeNameMap = Map.of(
            "全部", "all",
            "视频投稿", "video",
            "追番", "pgc",
            "专栏", "article"
    );
    public final ActivityResultLauncher<Intent> selectTypeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null && data.getStringExtra("item") != null) {
            String type = typeNameMap.get(data.getStringExtra("item"));
            if (type != null) {
                if (isRefreshing) {
                    MsgUtil.showMsg("还在加载中OvO");
                } else {
                    this.type = type;
                    setRefreshing(true);
                    refreshDynamic();
                }
            }
        }
    });

    public ActivityResultLauncher<Intent> writeDynamicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null) {
            String text = data.getStringExtra("text");
            ThreadManager.run(() -> {
                try {
                    long dynId;
                    Map<String, Long> atUids = new HashMap<>();
                    Pattern pattern = Pattern.compile("@(\\S+)\\s");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String matchedString = matcher.group(1);
                        long uid;
                        if ((uid = DynamicApi.mentionAtFindUser(matchedString)) != -1) {
                            atUids.put(matchedString, uid);
                        }
                    }
                    if (atUids.isEmpty()) {
                        dynId = DynamicApi.publishTextContent(text);
                    } else {
                        dynId = DynamicApi.publishTextContent(text, atUids);
                    }
                    if (!(dynId == -1)) {
                        runOnUiThread(() -> MsgUtil.showMsg("发送成功~"));
                        ThreadManager.run(() -> {
                            try {
                                Dynamic dynamic = DynamicApi.getDynamic(dynId);
                                dynamicList.add(0, dynamic);
                                runOnUiThread(() -> {
                                    if (type.equals("all")) {
                                        dynamicAdapter.notifyItemInserted(0);
                                        dynamicAdapter.notifyItemRangeChanged(0, dynamicList.size());
                                    }
                                });
                            } catch (Exception e) {
                                MsgUtil.error(e);
                            }
                        });
                    } else {
                        runOnUiThread(() -> MsgUtil.showMsg("发送失败"));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> MsgUtil.error(e));
                }
            });
        }
    });

    public static ActivityResultLauncher<Intent> getRelayDynamicLauncher(BaseActivity activity) {
        return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
            int code = result.getResultCode();
            Intent data = result.getData();
            if (code == RESULT_OK && data != null) {
                String text = data.getStringExtra("text");
                if (TextUtils.isEmpty(text)) text = "转发动态";
                long dynamicId = data.getLongExtra("dynamicId", -1);
                String finalText = text;
                ThreadManager.run(() -> {
                    try {
                        long dynId;
                        Map<String, Long> atUids = new HashMap<>();
                        Pattern pattern = Pattern.compile("@(\\S+)\\s");
                        Matcher matcher = pattern.matcher(finalText);
                        while (matcher.find()) {
                            String matchedString = matcher.group(1);
                            long uid;
                            if ((uid = DynamicApi.mentionAtFindUser(matchedString)) != -1) {
                                atUids.put(matchedString, uid);
                            }
                        }
                        dynId = DynamicApi.relayDynamic(finalText, (atUids.isEmpty() ? null : atUids), dynamicId);
                        if (!(dynId == -1)) {
                            activity.runOnUiThread(() -> MsgUtil.showMsg("转发成功~"));
                        } else {
                            activity.runOnUiThread(() -> MsgUtil.showMsg("转发失败"));
                        }
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> MsgUtil.error(e));
                    }
                });
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMenuClick();

        setOnRefreshListener(this::refreshDynamic);
        setOnLoadMoreListener(page -> addDynamic(type));

        setPageName("动态");

        TutorialHelper.showTutorialList(this, R.array.tutorial_dynamic, 6);

        refreshDynamic();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDynamic() {
        if (firstRefresh) {
            dynamicList = new ArrayList<>();
        } else {
            offset = 0;
            bottomReached = false;
            dynamicList.clear();
            dynamicAdapter.notifyDataSetChanged();
        }

        addDynamic(type, true);
    }

    private void addDynamic(String type) {
        addDynamic(type, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addDynamic(String type, boolean refresh) {
        ThreadManager.run(() -> {
            try {
                List<Dynamic> list = new ArrayList<>();
                offset = DynamicApi.getDynamicList(list, offset, 0, type);
                bottomReached = (offset == -1);
                setRefreshing(false);

                runOnUiThread(() -> {
                    dynamicList.addAll(list);
                    if (firstRefresh) {
                        firstRefresh = false;
                        dynamicAdapter = new DynamicAdapter(this, dynamicList, recyclerView);
                        setAdapter(dynamicAdapter);
                    } else {
                        if (refresh) {
                            dynamicAdapter.notifyDataSetChanged();
                        } else {
                            dynamicAdapter.notifyItemRangeInserted(dynamicList.size() - list.size() + 1, list.size());
                        }
                    }
                });

            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DynamicHolder.GO_TO_INFO_REQUEST && resultCode == RESULT_OK) {
            try {
                if (data != null && !isRefreshing) {
                    DynamicHolder.removeDynamicFromList(dynamicList, data.getIntExtra("position", 0) - 1, dynamicAdapter);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}