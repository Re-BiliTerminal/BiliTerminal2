package com.huanli233.biliterminal2.model;

public class OpusCard {
    public static final int TYPE_DYNAMIC = 1;
    public static final int TYPE_ARTICLE = 2;

    public String content;
    public String cover;
    public long opusId;
    public String timeText;
    public String title;

    public int type;
    public long parsedId;

    public OpusCard(int type, long id) {
        this.type = type;
        this.parsedId = id;
    }

    public OpusCard() {

    }
}
