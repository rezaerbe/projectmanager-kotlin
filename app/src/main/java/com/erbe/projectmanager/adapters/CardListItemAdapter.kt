package com.erbe.projectmanager.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erbe.projectmanager.R
import com.erbe.projectmanager.TaskListActivity
import com.erbe.projectmanager.models.Card
import com.erbe.projectmanager.models.SelectedMembers
import kotlinx.android.synthetic.main.item_card.view.*
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.android.synthetic.main.item_task.view.card_name_et

open class CardListItemAdapter (private val context : Context, private var list : ArrayList<Card>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if (holder is MyViewHolder) {

            holder.itemView.card_name_tv.text = model.name

            if (model.labelColor.isNotEmpty()) {
                holder.itemView.view_label_color.visibility = View.VISIBLE
                holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
            }
            else {
                holder.itemView.view_label_color.visibility = View.GONE
            }

            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {

                val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()
                val assignedMembers = context.mAssignedMembersDetailList

                for (i in assignedMembers.indices) {
                    for (j in model.assignedTo) {
                        if (assignedMembers[i].id == j) {
                            val selectedMembers = SelectedMembers(assignedMembers[i].id, assignedMembers[i].image)
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.itemView.card_selected_members_list_rv.visibility = View.GONE
                    }
                    else {
                        holder.itemView.card_selected_members_list_rv.visibility = View.VISIBLE
                        holder.itemView.card_selected_members_list_rv.layoutManager = GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemAdapter(context, selectedMembersList, false)
                        holder.itemView.card_selected_members_list_rv.adapter = adapter

                        adapter.setOnClickListener(object : CardMemberListItemAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }
                else {
                    holder.itemView.card_selected_members_list_rv.visibility = View.GONE
                }
            }
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener : OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    private class MyViewHolder(view : View) : RecyclerView.ViewHolder(view) {}
}