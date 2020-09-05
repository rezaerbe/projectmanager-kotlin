package com.erbe.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.erbe.projectmanager.R
import com.erbe.projectmanager.models.User
import com.erbe.projectmanager.utils.Constants
import kotlinx.android.synthetic.main.item_member.view.*

class MemberItemAdapter(private val context: Context, private var list : ArrayList<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_member, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.member_name_tv.text = model.name
            holder.itemView.member_email_tv.text = model.email

            Glide.with(context)
                .load(model.image)
                .fitCenter()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.member_image_iv)

            if (model.selected) {
                holder.itemView.selected_member_iv.visibility = View.VISIBLE
            }
            else {
                holder.itemView.selected_member_iv.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    }
                    else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position : Int, user : User, action : String)
    }

    private class MyViewHolder(view : View) : RecyclerView.ViewHolder(view) {}
}