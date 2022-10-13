package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.activity.DocDetailsActivity
import com.ft.document_organizer.interFace.LongItemClickListener
import com.ft.document_organizer.model.DocModel

class DocRvAdapter(
    private val context: Context,
    private val longItemClickListener: LongItemClickListener
) :
    RecyclerView.Adapter<DocRvAdapter.DocViewHolder>() {

    private val docArrayList = ArrayList<DocModel>()
    private lateinit var view: View
    private var viewType: String? = null

    fun setViewType(vType: String) {
        this.viewType = vType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        this.view = if (this.viewType == "LIST") {
            LayoutInflater.from(context).inflate(R.layout.item_document_linear_rv, parent, false)
        } else if (this.viewType == "COMPACT") {
            LayoutInflater.from(context).inflate(R.layout.item_document_compact_rv, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.item_document_grid_rv, parent, false)
        }
        return DocViewHolder(this.view)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        val model = docArrayList[position]

        holder.docName.text = model.docName.substring(0, model.docName.lastIndexOf('.'))
        holder.categoryNameTv.text = model.docCategoryName
        holder.categoryIconImgV.setImageResource(model.docCategoryIcon)
        holder.categoryIconBg.setBackgroundColor(model.docCategoryIconBg)
        holder.dateTv.text = model.date

        when (model.mimType) {
            "image/jpeg" -> {
                holder.docImage.setImageBitmap(BitmapFactory.decodeFile(model.filePath))
                Log.i("IMAGE_CHECK", model.filePath)
            }
            "application/pdf" -> {
                holder.docImage.setImageResource(R.drawable.pdf)
            }
            "application/msword" -> {
                holder.docImage.setImageResource(R.drawable.word)
            }
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                holder.docImage.setImageResource(R.drawable.word)
            }
            "application/vnd.ms-excel" -> {
                holder.docImage.setImageResource(R.drawable.xls)
            }
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                holder.docImage.setImageResource(R.drawable.xls)
            }
            "application/vnd.ms-powerpoint" -> {
                holder.docImage.setImageResource(R.drawable.powerpoint)
            }
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                holder.docImage.setImageResource(R.drawable.powerpoint)
            }
            "text/plain" -> {
                holder.docImage.setImageResource(R.drawable.text)
            }
            "text/css" -> {
                holder.docImage.setImageResource(R.drawable.css)
            }
            "text/csv" -> {
                holder.docImage.setImageResource(R.drawable.csv)
            }
            "text/html" -> {
                holder.docImage.setImageResource(R.drawable.html)
            }
            "application/zip" ->{
                holder.docImage.setImageResource(R.drawable.zip)
            }
            "application/x-rar", "application/vnd.rar" ->{
                holder.docImage.setImageResource(R.drawable.rar)
            }
            else -> {
                holder.docImage.setImageResource(R.drawable.document)
            }
        }

        holder.itemCv.setOnClickListener {
            val intent = Intent(context, DocDetailsActivity::class.java)
            intent.putExtra("id", model.id)
            intent.putExtra("isSyncOrNot", model.isSynced)
            intent.putExtra("mimType", model.mimType)
            intent.putExtra("docName", model.docName)
            intent.putExtra("docUri", model.docUri)
            intent.putExtra("filePath", model.filePath)
            intent.putExtra("docCategoryName", model.docCategoryName)
            intent.putExtra("docCategoryIcon", model.docCategoryIcon)
            intent.putExtra("docCategoryIconBg", model.docCategoryIconBg)
            intent.putExtra("date", model.date)
            context.startActivity(intent)
        }
        holder.itemCv.setOnLongClickListener {
            longItemClickListener.onLongClick(model)
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<DocModel>) {
        docArrayList.clear()
        docArrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return docArrayList.size
    }

    class DocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCv: CardView = itemView.findViewById(R.id.itemCv)
        val docImage: ImageView = itemView.findViewById(R.id.docImgV)
        val docName: TextView = itemView.findViewById(R.id.docNameTv)
        val categoryIconBg: LinearLayout = itemView.findViewById(R.id.categoryIconBg)
        val categoryIconImgV: ImageView = itemView.findViewById(R.id.categoryIconImgV)
        val categoryNameTv: TextView = itemView.findViewById(R.id.categoryNameTv)
        val dateTv: TextView = itemView.findViewById(R.id.dateTv)
    }
}