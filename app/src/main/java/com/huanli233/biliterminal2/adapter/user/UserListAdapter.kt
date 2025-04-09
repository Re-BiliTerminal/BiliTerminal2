package com.huanli233.biliterminal2.adapter.user

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.util.GlideUtil
import com.huanli233.biliterminal2.util.GlideUtil.loadFace
import com.huanli233.biliwebapi.bean.user.UserInfo

open class UserListAdapter(@JvmField val context: Context, val userList: List<UserInfo>) :
    RecyclerView.Adapter<UserListAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.cell_user_list, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = userList[position].name
        userList[position].vip.nicknameColor.let { color ->
            if (color.isNotEmpty()) holder.name.setTextColor(Color.parseColor(color))
        }
        holder.desc.text = userList[position].sign

        if (userList[position].face.isEmpty()) {
            holder.avatar.visibility = View.GONE
            holder.desc.isSingleLine = false
        } else {
            holder.avatar.loadFace(userList[position].face)
            holder.avatar.visibility = View.VISIBLE
            holder.desc.isSingleLine = true
        }

        if (userList[position].mid != -1L) {
            holder.itemView.setOnClickListener { view: View? ->
                val intent = Intent()
                    .setClass(context, UserInfoActivity::class.java)
                    .putExtra("mid", userList[position].mid)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView =
            itemView.findViewById(R.id.userName)
        val desc: TextView =
            itemView.findViewById(R.id.userDesc)
        val avatar: ImageView =
            itemView.findViewById(R.id.userAvatar)
    }
}
