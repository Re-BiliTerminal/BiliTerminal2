package com.huanli233.biliterminal2.ui.widget.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.google.android.material.textview.MaterialTextView;
import com.huanli233.biliterminal2.data.setting.DataStore;

public class MarqueeTextView extends MaterialTextView {
    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMarquee();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMarquee();
    }

    public MarqueeTextView(Context context) {
        super(context);
        setMarquee();
    }

    public void setMarquee() {
        if (!isInEditMode()) {
            if (DataStore.INSTANCE.getAppSettings().getMarqueeEnabled()) {
                setSelected(true);
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
                setSingleLine();
                setMarqueeRepeatLimit(-1);
                setFocusable(true);
                setFocusableInTouchMode(true);
            }
        } else {
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setSingleLine();
            setMarqueeRepeatLimit(-1);
        }
    }
}
