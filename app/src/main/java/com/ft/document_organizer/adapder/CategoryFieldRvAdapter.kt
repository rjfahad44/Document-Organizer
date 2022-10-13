package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.interFace.FieldDeleteListener
import com.ft.document_organizer.model.CategoryFieldModel

class CategoryFieldRvAdapter(
    private val context: Context,
    private val deleteItemClickListener: FieldDeleteListener
): RecyclerView.Adapter<CategoryFieldRvAdapter.CategoricalViewHolder>() {


    private val categoryList = ArrayList<CategoryFieldModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoricalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_field_rv, parent, false)
        return CategoricalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoricalViewHolder, position: Int) {
        val model = categoryList[position]
        holder.descriptionTitleTV.text = model.fieldTitle
        holder.deleteImgV.setOnClickListener {
            deleteItemClickListener.onDeleteItem(model)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<CategoryFieldModel>){
        categoryList.clear()
        categoryList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    class CategoricalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var descriptionTitleTV = itemView.findViewById<TextView>(R.id.descriptionTitleTV)
        var deleteImgV = itemView.findViewById<ImageView>(R.id.deleteImgV)
    }
}