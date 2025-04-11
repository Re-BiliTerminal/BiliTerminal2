package com.huanli233.biliterminal2.bean;

import org.json.JSONObject;

public class PrivateMsgSession {
    public long talkerUid = 0;
    public int unread = 0;
    public int contentType = 0;
    public JSONObject content;

    public PrivateMsgSession() {
    }
}
