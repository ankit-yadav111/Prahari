package com.smart.hbalert.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smart.hbalert.ContactActivity
import com.smart.hbalert.R

class ContactAdapter (private val context: Context, private val listener: ContactActivity ) : RecyclerView.Adapter<ContactAdapter.ContViewHolder>(){

    private val allCont = ArrayList<contact>()

    inner class ContViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textNumber: TextView= itemView.findViewById(R.id.textNumber)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContViewHolder {
        val viewHolder = ContViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_element,parent, false))
        viewHolder.deleteButton.setOnClickListener{
            listener.onItemClicked(allCont[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ContViewHolder, position: Int) {
        val currentContact =allCont[position]
        holder.textName.text = currentContact.name
        holder.textNumber.text=currentContact.number
    }

    override fun getItemCount(): Int {
        return allCont.size
    }

    fun updateList(newList: List<contact>){
        allCont.clear()
        allCont.addAll(newList)

        notifyDataSetChanged()
    }
}