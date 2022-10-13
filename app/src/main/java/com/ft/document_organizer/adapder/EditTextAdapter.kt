package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R


class EditTextAdapter(private val context: Context) :
    RecyclerView.Adapter<EditTextAdapter.EditTextViewHolder>() {

    private val edtList = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditTextViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_edit_text, parent, false)
        return EditTextViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditTextViewHolder, position: Int) {
        holder.editText.hint = edtList[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<String>) {
        edtList.clear()
        edtList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = edtList.size

    override fun getItemId(position: Int) = position.toLong()

    class EditTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val editText: EditText = view.findViewById(R.id.descriptionET)
    }
}
