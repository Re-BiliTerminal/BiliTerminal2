package com.huanli233.biliterminal2.bean

import android.os.Parcel
import android.os.Parcelable
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliwebapi.bean.video.VideoInfo
import java.io.Serializable

open class VideoCard() : Parcelable, Serializable {
    var title: String? = null
    var uploader: String? = null
    var view: String? = null
    var cover: String? = null
    var type: String = "video"
    var aid: Long = 0
    var bvid: String = ""
    var cid: Long = 0

    protected constructor(`in`: Parcel) : this() {
        title = `in`.readString()
        uploader = `in`.readString()
        view = `in`.readString()
        cover = `in`.readString()
        type = `in`.readString().orEmpty()
        aid = `in`.readLong()
        bvid = `in`.readString().orEmpty()
        cid = `in`.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(title)
        parcel.writeString(uploader)
        parcel.writeString(view)
        parcel.writeString(cover)
        parcel.writeString(type)
        parcel.writeLong(aid)
        parcel.writeString(bvid)
        parcel.writeLong(cid)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VideoCard> = object : Parcelable.Creator<VideoCard> {
            override fun createFromParcel(`in`: Parcel): VideoCard {
                return VideoCard(`in`)
            }

            override fun newArray(size: Int): Array<VideoCard?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic @JvmOverloads
        fun of(
            title: String,
            uploader: String,
            view: String,
            cover: String,
            aid: Long,
            bvid: String,
            type: String = "video",
            cid: Long = 0
        ): VideoCard {
            return VideoCard().also {
                it.title = title
                it.uploader = uploader
                it.view = view
                it.cover = cover
                it.type = type
                it.aid = aid
                it.bvid = bvid
                it.cid = cid
            }
        }
    }
}

fun VideoInfo.toVideoCard(): VideoCard = VideoCard().also {
    it.title = title
    it.bvid = bvid
    it.cid = cid
    it.uploader = owner.name
    it.aid = aid
    it.cover = pic
    it.view = Utils.toWan(stat.view.toLong())
}