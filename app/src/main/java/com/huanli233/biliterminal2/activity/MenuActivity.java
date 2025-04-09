package com.huanli233.biliterminal2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.button.MaterialButton;
import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.activity.dynamic.DynamicActivity;
import com.huanli233.biliterminal2.activity.live.RecommendLiveActivity;
import com.huanli233.biliterminal2.activity.message.MessageActivity;
import com.huanli233.biliterminal2.activity.search.SearchActivity;
import com.huanli233.biliterminal2.activity.settings.SettingMainActivity;
import com.huanli233.biliterminal2.activity.settings.login.LoginActivity;
import com.huanli233.biliterminal2.activity.user.MySpaceActivity;
import com.huanli233.biliterminal2.activity.video.PopularActivity;
import com.huanli233.biliterminal2.activity.video.PreciousActivity;
import com.huanli233.biliterminal2.activity.video.RecommendActivity;
import com.huanli233.biliterminal2.activity.video.local.LocalListActivity;
import com.huanli233.biliterminal2.util.Preferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private String from;

    /**
     * 在排序设置和Splash中使用到的，
     * 需要使用排序，故用了LinkedHashMap
     * 请不要让它的顺序被打乱（
     */
    public static final Map<String, Pair<String, Class<? extends InstanceActivity>>> btnNames = new LinkedHashMap<>() {{
        put("recommend", new Pair<>("推荐", RecommendActivity.class));
        put("popular", new Pair<>("热门", PopularActivity.class));
        put("precious", new Pair<>("入站必刷", PreciousActivity.class));
        put("live", new Pair<>("直播", RecommendLiveActivity.class));
        put("search", new Pair<>("搜索", SearchActivity.class));
        put("dynamic", new Pair<>("动态", DynamicActivity.class));
        put("myspace", new Pair<>("我的", MySpaceActivity.class));
        put("message", new Pair<>("消息", MessageActivity.class));
        put("local", new Pair<>("缓存", LocalListActivity.class));
        put("settings", new Pair<>("设置", SettingMainActivity.class));
    }};

    long time;

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        time = System.currentTimeMillis();

        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        if (from != null) {
            if (btnNames.containsKey(from))
                setPageName(Objects.requireNonNull(btnNames.get(from)).first);
        }

        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<String> btnList;

        String sortConf = Preferences.getString(Preferences.MENU_SORT, "");

        if (!TextUtils.isEmpty(sortConf)) {
            String[] splitName = sortConf.split(";");
            if (splitName.length != btnNames.size()) {
                btnList = getDefaultSortList();
            } else {
                btnList = new ArrayList<>();
                for (String name : splitName) {
                    if (!btnNames.containsKey(name)) {
                        btnList = getDefaultSortList();
                        break;
                    } else {
                        btnList.add(name);
                    }
                }
            }
        } else {
            btnList = getDefaultSortList();
        }

        if (Preferences.getLong(Preferences.MID, 0) == 0) {
            btnList.add(0, "login");
            btnList.remove("dynamic");
            btnList.remove("message");
            btnList.remove("myspace");
        }

        if (!Preferences.getBoolean("menu_popular", true)) btnList.remove("popular");
        if (!Preferences.getBoolean("menu_precious", false)) btnList.remove("precious");
        if (!Preferences.getBoolean("menu_live", false)) btnList.remove("live");

        btnList.add("exit");

        LinearLayout layout = findViewById(R.id.menu_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (String btn : btnList) {
            MaterialButton materialButton = new MaterialButton(this);
            switch (btn) {
                case "exit":
                    materialButton.setText("退出");
                    break;
                case "login":
                    materialButton.setText("登录");
                    break;
                default:
                    materialButton.setText(Objects.requireNonNull(btnNames.get(btn)).first);
                    break;
            }
            materialButton.setOnClickListener(view -> killAndJump(btn));
            layout.addView(materialButton, params);
        }

    }

    private void killAndJump(String name) {
        if (btnNames.containsKey(name) && !Objects.equals(name, from)) {
            InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
            if (instance != null && instance.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED)
                instance.finish();

            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, Objects.requireNonNull(btnNames.get(name)).second);
            intent.putExtra("from", name);
            startActivity(intent);
        } else {
            switch (name) {
                case "exit":
                    InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
                    if (instance != null && !instance.isDestroyed()) instance.finish();
                    Process.killProcess(Process.myPid());
                    break;
                case "login":
                    Intent intent = new Intent();
                    intent.setClass(MenuActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
            }
        }
        finish();
    }

    private List<String> getDefaultSortList() {
        return new ArrayList<>() {{
            add("recommend");
            add("popular");
            add("precious");
            add("live");
            add("search");
            add("dynamic");
            add("myspace");
            add("message");
            add("local");
            add("settings");
        }};
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) finish();
        return super.onKeyDown(keyCode, event);
    }
}

