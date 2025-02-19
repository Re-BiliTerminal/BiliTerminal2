package com.huanli233.biliterminal2.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.activity.reply.ReplyFragment;
import com.huanli233.biliterminal2.adapter.viewpager.ViewPagerFragmentAdapter;
import com.huanli233.biliterminal2.api.ReplyApi;
import com.huanli233.biliterminal2.event.ReplyEvent;
import com.huanli233.biliterminal2.helper.TutorialHelper;
import com.huanli233.biliterminal2.util.AnimationUtils;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.TerminalContext;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ArticleInfoActivity extends BaseActivity {
    private long cvid;

    private ReplyFragment replyFragment;
    private long seek_reply;

    private ImageView loadingView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        cvid = intent.getLongExtra("cvid", 114514);
        this.seek_reply = getIntent().getLongExtra("seekReply", -1);

        setPageName("专栏详情");
        loadingView = findViewById(R.id.loading);

        TutorialHelper.showTutorialList(this, R.array.tutorial_article, 7);

        ViewPager viewPager = findViewById(R.id.viewPager);

        TerminalContext.getInstance().getArticleInfoByCvId(cvid)
            .observe(this, (result) -> result.onSuccess((articleInfo)-> {
                List<Fragment> fragmentList = new ArrayList<>();
                ArticleInfoFragment articleInfoFragment = ArticleInfoFragment.newInstance(cvid);
                fragmentList.add(articleInfoFragment);
                replyFragment = ReplyFragment.newInstance(cvid, ReplyApi.REPLY_TYPE_ARTICLE, seek_reply, articleInfo != null ? articleInfo.upInfo.mid : -1);
                replyFragment.setSource(articleInfo);
                fragmentList.add(replyFragment);
                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                viewPager.setAdapter(vpfAdapter);
                View view;
                if ((view = articleInfoFragment.getView()) != null)
                    view.setVisibility(View.GONE);
                if (seek_reply != -1) viewPager.setCurrentItem(1);
                articleInfoFragment.setOnFinishLoad(() -> AnimationUtils.crossFade(loadingView, articleInfoFragment.getView()));
                TutorialHelper.showPagerTutorial(this,2);
            }).onFailure((error) -> {
                loadingView.setImageResource(R.mipmap.loading_2233_error);
                MsgUtil.err(error);
            }));
    }

    @Override
    protected boolean eventBusEnabled() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true, priority = 1)
    public void onEvent(ReplyEvent event) {
        replyFragment.notifyReplyInserted(event);
    }

    @Override
    protected void onDestroy() {
        TerminalContext.getInstance().leaveDetailPage();
        super.onDestroy();
    }
}
