package com.erbe.projectmanager.adapters

import android.bluetooth.BluetoothAssignedNumbers
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.erbe.projectmanager.R
import com.erbe.projectmanager.models.SelectedMembers
import kotlinx.android.synthetic.main.item_card_selected_member.view.*

class CardMemberListItemAdapter (private val context: Context, private var list : ArrayList<SelectedMembers>, private val assignedMembers: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card_selected_member, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if (holder is MyViewHolder) {
            if (position == list.size-1 && assignedMembers) {
                holder.itemView.add_member_iv.visibility = View.VISIBLE
                holder.itemView.selected_member_image_iv.visibility = View.GONE
            }
            else {
                holder.itemView.add_member_iv.visibility = View.GONE
                holder.itemView.selected_member_image_iv.visibility = View.VISIBLE

                Glide.with(context)
                    .load(model.image)
                    .fitCenter()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.selected_member_image_iv)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
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
        fun onClick()
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

}