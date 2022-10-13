package com.ft.document_organizer.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.CategoriesRvAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.interFace.CategoryMenuListener
import com.ft.document_organizer.model.CategoryModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class CategoriesActivity : AppCompatActivity(), CategoryMenuListener {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel
    private lateinit var categoryDao: CategoryDao

    private lateinit var searchView: SearchView
    private lateinit var dialog: Dialog

    //Category List Rv
    private lateinit var categoricalAdapter: CategoriesRvAdapter
    private lateinit var categoricalRV: RecyclerView
    private lateinit var categoryList: ArrayList<CategoryModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        initializeWidget()
    }

    private fun initializeWidget() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DocViewModel::class.java]

        categoryDao = DocDatabase.getDatabase(this).getCategoryDao()

        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.addCategoryImgV).setOnClickListener { addCategory() }
        searchView = findViewById(R.id.searchView)
        searchCategory()

        categoricalRV = findViewById(R.id.rvFieldCategory)
        categoryList = ArrayList()
        categoricalAdapter = CategoriesRvAdapter(this, this)
        categoricalRV.layoutManager = LinearLayoutManager(this)
        categoricalRV.adapter = categoricalAdapter

        loadData()
    }

    private fun loadData() {
        viewModel.allCategory.observe(this) {
            categoricalAdapter.update(it as ArrayList<CategoryModel>)
        }
    }

    private fun searchCategory() {
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searching(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                searching(query)
                return false
            }
        })
    }

    private fun searching(query: String?) {
        if (query!!.isEmpty()) {
            loadData()
        } else {
            categoryDao.searchCategory("%$query%").observe(this) {
                categoricalAdapter.update(it as ArrayList<CategoryModel>)
            }
        }
    }

    private fun addCategory() {
        startActivity(
            Intent(
                this@CategoriesActivity,
                AddNewCategoryActivity::class.java
            )
        )
    }

    override fun onMenuItem(model: CategoryModel, view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.popup_menu_categories)
        popupMenu.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.editPopupMenuId ->{
                    val intent = Intent(this@CategoriesActivity, CategoryEditActivity::class.java)
                    intent.putExtra("id", model.id)
                    intent.putExtra("title", model.title)
                    intent.putExtra("icon", model.icon)
                    intent.putExtra("colorBg", model.colorBg)
                    intent.putExtra("counter", model.counter)
                    startActivity(intent)
                    true
                }
                R.id.deletePopupMenuId->{
                    dialog = Dialog(this)
                    dialog.setContentView(R.layout.dialog_delete_doc)
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.findViewById<TextView>(R.id.cancelBtnTv).setOnClickListener { dialog.dismiss() }
                    dialog.findViewById<TextView>(R.id.deleteBtnTv).setOnClickListener {
                        Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show()
                        viewModel.deleteData(model)
                        dialog.dismiss()
                    }
                    dialog.show()
                    true
                }
                R.id.markPopupMenuId->{
                    Toast.makeText(this, "Mark", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.markAllPopupMenuId->{
                    Toast.makeText(this, "Mark All", Toast.LENGTH_SHORT).show()
                    true
                }
                else ->{
                    false
                }
            }
        }
        popupMenu.show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}