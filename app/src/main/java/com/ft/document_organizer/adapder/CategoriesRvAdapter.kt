package com.ft.document_organizer.adapder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.interFace.CategoryMenuListener
import com.ft.document_organizer.model.CategoryModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoriesRvAdapter(
    private val context: Activity,
    private val onItemClickListener: CategoryMenuListener
) : RecyclerView.Adapter<CategoriesRvAdapter.CategoricalViewHolder>() {

    private val categoryList = ArrayList<CategoryModel>()
    private var categoryFieldDao = DocDatabase.getDatabase(context).getCategoryFieldDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoricalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category_field_rv, parent, false)
        return CategoricalViewHolder(view)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: CategoricalViewHolder, position: Int) {
        val model = categoryList[position]
        holder.iconAndNameContainerRl.backgroundTintList = ColorStateList.valueOf(model.colorBg)
        holder.categoryIconImgV.setImageResource(model.icon)
        holder.categoryName.text = model.title

        context.runOnUiThread {
            GlobalScope.launch {
                val builder = SpannableStringBuilder()
                categoryFieldDao.searchFields(model.title).forEach{ item ->
                    Log.i("FIELDS", "Field Title : ${item.fieldTitle}")
                    builder.append(
                        "   "+item.fieldTitle + "\n",
                        BulletSpan(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                context.runOnUiThread {
                    holder.fieldsTv.text = builder
                }
            }
        }

        holder.fieldsTv.text = model.title

        holder.menuImgV.setOnClickListener {
            onItemClickListener.onMenuItem(model, holder.menuImgV)
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
        var iconAndNameContainerRl: RelativeLayout = itemView.findViewById(R.id.iconAndNameContainerRl)
        var categoryIconImgV: ImageView = itemView.findViewById(R.id.categoryIconImgV)
        var categoryName: TextView = itemView.findViewById(R.id.categoryName)
        var fieldsTv: TextView = itemView.findViewById(R.id.fieldsTv)
        val menuImgV: ImageView = itemView.findViewById(R.id.menuImgV)
    }
}