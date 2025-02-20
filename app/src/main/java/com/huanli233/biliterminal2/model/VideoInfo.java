package com.huanli233.biliterminal2.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.huanli233.biliterminal2.util.ToolsUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoInfo implements Parcelable, Serializable {

    public static final int COPYRIGHT_SELF = 1;
    public static final int COPYRIGHT_REPRINT = 2;

    public String bvid;
    public long aid;
    public String title;
    public ArrayList<UserInfo> staff = new ArrayList<>();
    public String cover;
    public String description;
    public String duration;
    public Stats stats;
    public String timeDesc;
    public ArrayList<String> pagenames = new ArrayList<>();
    public ArrayList<Long> cids = new ArrayList<>();
    public List<At> descAts = new ArrayList<>();

    public boolean upowerExclusive;
    public String argueMsg;
    public boolean isCooperation;
    public boolean isSteinGate;
    public boolean is360;

    public long epid;
    public int copyright;
    public Collection collection;

    public VideoInfo() {
    }


    protected VideoInfo(Parcel in) {
        bvid = in.readString();
        aid = in.readLong();
        title = in.readString();
        staff = in.createTypedArrayList(UserInfo.CREATOR);
        cover = in.readString();
        description = in.readString();
        duration = in.readString();
        stats = in.readParcelable(Stats.class.getClassLoader());
        timeDesc = in.readString();
        pagenames = in.createStringArrayList();
        in.readList(cids, Long.class.getClassLoader());
        descAts = in.createTypedArrayList(At.CREATOR);
        upowerExclusive = in.readByte() != 0;
        argueMsg = in.readString();
        isCooperation = in.readByte() != 0;
        isSteinGate = in.readByte() != 0;
        is360 = in.readByte() != 0;
        epid = in.readLong();
        copyright = in.readInt();
        collection = in.readParcelable(Collection.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bvid);
        dest.writeLong(aid);
        dest.writeString(title);
        dest.writeTypedList(staff);
        dest.writeString(cover);
        dest.writeString(description);
        dest.writeString(duration);
        dest.writeParcelable(stats, flags);
        dest.writeString(timeDesc);
        dest.writeStringList(pagenames);
        dest.writeList(cids);
        dest.writeTypedList(descAts);
        dest.writeByte((byte) (upowerExclusive ? 1 : 0));
        dest.writeString(argueMsg);
        dest.writeByte((byte) (isCooperation ? 1 : 0));
        dest.writeByte((byte) (isSteinGate ? 1 : 0));
        dest.writeByte((byte) (is360 ? 1 : 0));
        dest.writeLong(epid);
        dest.writeInt(copyright);
        dest.writeParcelable(collection, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoInfo> CREATOR = new Creator<>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    public VideoCard toCard() {
        VideoCard card = new VideoCard();
        card.setTitle(title);
        card.setUploader(staff.get(0).name);
        card.setView(ToolsUtil.toWan(stats.view));
        card.setCover(cover);
        card.setAid(aid);
        card.setBvid(bvid);
        return card;
    }
}
