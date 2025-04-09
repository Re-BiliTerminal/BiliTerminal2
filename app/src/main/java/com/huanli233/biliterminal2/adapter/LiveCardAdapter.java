package com.huanli233.biliterminal2.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.adapter.video.VideoCardHolder;
import com.huanli233.biliterminal2.listener.OnItemLongClickListener;
import com.huanli233.biliterminal2.model.LiveRoom;
import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.TerminalContext;
import com.huanli233.biliterminal2.util.Utils;

import java.util.List;
import java.util.Objects;

public class LiveCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {

    final Context context;
    final List<LiveRoom> roomList;
    OnItemLongClickListener longClickListener;

    public LiveCardAdapter(Context context, List<LiveRoom> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        LiveRoom room = roomList.get(position);

        VideoCard videoCard = new VideoCard();
        videoCard.setTitle(Utils.removeHtml(room.title));
        if (!room.user_cover.startsWith("http")) videoCard.setCover("http:" + room.user_cover);
        else videoCard.setCover(room.user_cover);
        if (TextUtils.isEmpty(videoCard.getCover()) || Objects.equals(videoCard.getCover(), "http:"))
            videoCard.setCover(room.cover);
        videoCard.setUploader(room.uname);
        videoCard.setView(Utils.toWan(room.online) + "人观看");
        videoCard.setType("live");

        holder.bindData(videoCard, context);

        holder.itemView.setOnClickListener(view -> TerminalContext.getInstance().enterLiveDetailPage(context, room.roomid));

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;    //必须要true表示事件已处理 不再继续传递，不然上面的点按也会触发
            } else return false;
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

}
