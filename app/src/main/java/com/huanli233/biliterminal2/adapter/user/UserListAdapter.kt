package com.huanli233.biliterminal2.adapter.user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.util.GlideUtil.loadFace
import com.huanli233.biliwebapi.bean.user.UserInfo
import androidx.core.graphics.toColorInt

open class UserListAdapter(
    @JvmField val context: Context,
    userList: List<UserInfo>
) : RecyclerView.Adapter<UserListAdapter.Holder>() {
    val data = userList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.cell_user_list, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = data[position].name
        data[position].vip.nicknameColor.let { color ->
            if (color.isNotEmpty()) holder.name.setTextColor(color.toColorInt())
        }
        holder.desc.text = data[position].sign

        if (data[position].face.isEmpty()) {
            holder.avatar.visibility = View.GONE
            holder.desc.isSingleLine = false
        } else {
            holder.avatar.loadFace(data[position].face)
            holder.avatar.visibility = View.VISIBLE
            holder.desc.isSingleLine = true
        }

        if (data[position].mid != -1L) {
            holder.itemView.setOnClickListener { view: View? ->
                val intent = Intent()
                    .setClass(context, UserInfoActivity::class.java)
                    .putExtra("mid", data[position].mid)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView =
            itemView.findViewById(R.id.user_name)
        val desc: TextView =
            itemView.findViewById(R.id.user_desc)
        val avatar: ImageView =
            itemView.findViewById(R.id.user_avatar)
    }
}
