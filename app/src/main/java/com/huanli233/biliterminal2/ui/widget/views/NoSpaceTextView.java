package com.huanli233.biliterminal2.ui.widget.views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

@SuppressLint("AppCompatCustomView")
public class NoSpaceTextView extends AppCompatTextView {

    private boolean reMeasure = false;

    public NoSpaceTextView(Context context) {
        super(context);
    }

    public NoSpaceTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSpaceTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        removeSpace(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        reMeasure = true;
    }

    private void removeSpace(int widthSpec, int heightSpec) {

        int paddingTop;
        String[] linesText = getLines();
        TextPaint paint = getPaint();
        Rect rect = new Rect();
        String text = linesText[0];
        paint.getTextBounds(text, 0, text.length(), rect);

        Paint.FontMetricsInt fontMetricsInt = new Paint.FontMetricsInt();
        paint.getFontMetricsInt(fontMetricsInt);

        paddingTop = (fontMetricsInt.top - rect.top);

        setPadding(getLeftPaddingOffset()
                , paddingTop + getTopPaddingOffset()
                , getRightPaddingOffset()
                , getBottomPaddingOffset());

        String endText = linesText[linesText.length - 1];
        paint.getTextBounds(endText, 0, endText.length(), rect);

        setMeasuredDimension(getMeasuredWidth()
                , getMeasuredHeight() - (fontMetricsInt.bottom - rect.bottom));

        if (reMeasure) {
            reMeasure = false;
            measure(widthSpec, heightSpec);
        }
    }

    private String[] getLines() {
        int start = 0;
        int end;
        String[] texts = new String[getLineCount()];
        String text = getText().toString();
        Layout layout = getLayout();
        for (int i = 0; i < getLineCount(); i++) {
            end = layout.getLineEnd(i);
            String line = text.substring(start, end);
            start = end;
            texts[i] = line;
        }
        return texts;
    }
}