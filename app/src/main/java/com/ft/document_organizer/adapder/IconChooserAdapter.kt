package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.interFace.IconClickListener

class IconChooserAdapter(
    private val context: Context,
    private val iconClickListener: IconClickListener
): RecyclerView.Adapter<IconChooserAdapter.CategoricalViewHolder>() {


    private val categoryList = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoricalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_icon_view, parent, false)
        return CategoricalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoricalViewHolder, position: Int) {
        holder.iconImgV.setImageResource(categoryList[position])
        holder.iconImgV.setOnClickListener {
            iconClickListener.iconClick(categoryList[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<Int>){
        categoryList.clear()
        categoryList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    class CategoricalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconImgV = itemView.findViewById<ImageView>(R.id.iconImgV)
    }
}