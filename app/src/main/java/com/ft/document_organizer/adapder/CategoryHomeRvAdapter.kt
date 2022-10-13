package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.activity.CategoryDetailsActivity
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.model.CategoryModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryHomeRvAdapter(
    private val context: Context
) : RecyclerView.Adapter<CategoryHomeRvAdapter.CategoricalViewHolder>() {

    private val categoryList = ArrayList<CategoryModel>()
    private val dao = DocDatabase.getDatabase(context).getDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoricalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category_rv, parent, false)
        return CategoricalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoricalViewHolder, position: Int) {
        val model = categoryList[position]
        holder.relativeLayout.backgroundTintList = ColorStateList.valueOf(model.colorBg)
        holder.image.setImageResource(model.icon)
        holder.title.text = model.title
        holder.counterTv.text = model.counter.toString()
        holder.counterTv.setTextColor(ColorStateList.valueOf(model.colorBg-10000))
        if (model.counter > 0){
            holder.counterTv.visibility = View.VISIBLE
        }else{
            holder.counterTv.visibility = View.GONE
        }

        holder.relativeLayout.setOnClickListener {
            GlobalScope.launch {
                val intent = Intent(context, CategoryDetailsActivity::class.java)
                intent.putExtra("categoryTitle", model.title)
                val isEmpty = dao.isFindOrNot(model.title).isEmpty()
                intent.putExtra("isEmptyOrNot", isEmpty)
                context.startActivity(intent)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(arrayList: ArrayList<CategoryModel>) {
        categoryList.clear()
        categoryList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    class CategoricalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var relativeLayout = itemView.findViewById<RelativeLayout>(R.id.Rl)
        var image = itemView.findViewById<ImageView>(R.id.category_icon)
        var title = itemView.findViewById<TextView>(R.id.category_name)
        var counterTv = itemView.findViewById<TextView>(R.id.counterTv)
    }
}