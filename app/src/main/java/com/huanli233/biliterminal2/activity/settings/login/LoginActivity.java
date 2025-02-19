package com.huanli233.biliterminal2.activity.settings.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.adapter.viewpager.ViewPagerFragmentAdapter;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        setPageName("登录");

        boolean from_setup = getIntent().getBooleanExtra("from_setup", false);

        ViewPager viewPager = findViewById(R.id.viewPager);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(QRLoginFragment.newInstance(from_setup));

        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(vpfAdapter);

        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}