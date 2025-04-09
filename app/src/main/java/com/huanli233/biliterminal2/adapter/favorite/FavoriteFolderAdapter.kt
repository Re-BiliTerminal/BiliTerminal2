package com.huanli233.biliterminal2.adapter.favorite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.user.favorite.FavoriteVideoListActivity
import com.huanli233.biliterminal2.activity.user.favorite.FavouriteOpusListActivity
import com.huanli233.biliterminal2.adapter.favorite.FavoriteFolderAdapter.FavoriteHolder
import com.huanli233.biliterminal2.model.FavoriteFolder
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.GlideUtil.transitionOptions
import com.huanli233.biliterminal2.util.Utils

class FavoriteFolderAdapter(
    val context: Context,
    val folderList: List<FavoriteFolder>,
    val mid: Long
) :
    RecyclerView.Adapter<FavoriteHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder {
        val view = LayoutInflater.from(this.context)
            .inflate(R.layout.cell_favorite_folder_list, parent, false)
        return FavoriteHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FavoriteHolder, position: Int) {
        if (position == folderList.size) {
            holder.name.text = "图文收藏夹"
            holder.count.text = ""
            Glide.with(context).asDrawable()
                .load(Utils.getDrawable(context, R.drawable.article_fav_cover))
                .transition(transitionOptions)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dp2px(5f))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.cover)
            holder.itemView.setOnClickListener { v: View? ->
                val intent = Intent(
                    context,
                    FavouriteOpusListActivity::class.java
                )
                context.startActivity(intent)
            }
        } else {
            holder.name.text = Utils.htmlToString(folderList[position].name)
            holder.count.text =
                folderList[position].videoCount.toString() + "/" + folderList[position].maxCount
            holder.cover.loadPicture(folderList[position].cover)
            holder.itemView.setOnClickListener {
                val intent = Intent()
                intent.setClass(context, FavoriteVideoListActivity::class.java)
                intent.putExtra("fid", folderList[position].id)
                intent.putExtra("mid", mid)
                intent.putExtra("name", folderList[position].name)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return folderList.size + 1
    }

    class FavoriteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView =
            itemView.findViewById(R.id.text_title)
        val count: TextView =
            itemView.findViewById(R.id.text_itemcount)
        val cover: ImageView =
            itemView.findViewById(R.id.img_cover)
    }
}
