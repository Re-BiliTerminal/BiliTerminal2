package com.huanli233.biliterminal2.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.huanli233.biliterminal2.data.UserPreferences;
import com.huanli233.biliterminal2.utils.Preferences;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {
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
        if (!isInEditMode())
            if (UserPreferences.INSTANCE.getMarqueeEnabled().get()) {
                setSelected(true);
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
                setSingleLine();
                setMarqueeRepeatLimit(-1);
                setFocusable(true);
                setFocusableInTouchMode(true);
            }
    }
}
