package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R


class CategoryDetailsAdapter(private val context: Context) :
    RecyclerView.Adapter<CategoryDetailsAdapter.CategoryDetailsViewHolder>() {

    private val edtList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_text_view_rv, parent, false)
        return CategoryDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryDetailsViewHolder, position: Int) {
        holder.tvDetails.text = edtList[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<String>) {
        edtList.clear()
        edtList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = edtList.size

    override fun getItemId(position: Int) = position.toLong()
    class CategoryDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
    }
}
