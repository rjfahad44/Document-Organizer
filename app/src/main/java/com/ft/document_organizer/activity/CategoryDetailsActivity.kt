package com.ft.document_organizer.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.DocRvAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.interFace.LongItemClickListener
import com.ft.document_organizer.model.DocModel
import com.ft.document_organizer.utils.Variables
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class CategoryDetailsActivity : AppCompatActivity(), LongItemClickListener {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var noDocYetTv: TextView

    private lateinit var dialog: Dialog
    private lateinit var searchView: SearchView
    private lateinit var sortTitleTv: TextView
    private lateinit var changeRvList: ImageView
    private lateinit var changeListOrderRL: RelativeLayout

    private var isSortType = 1

    private var docDao: DocDao? = null

    //RecyclerView show from Documents
    private lateinit var rvSearchDoc: RecyclerView
    private lateinit var docRvAdapter: DocRvAdapter
    private lateinit var searchList: ArrayList<DocModel>

    private lateinit var categoryTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        initializeWidget()
    }

    private fun initializeWidget() {
        docDao = DocDatabase.getDatabase(this).getDao()

        categoryTitle = findViewById(R.id.categoryTitleTv)

        noDocYetTv = findViewById(R.id.noDocYetTv)
        findViewById<ImageView>(R.id.plusImgBtn).setOnClickListener {
            val sendIntent = Intent(Intent(this@CategoryDetailsActivity, AddNewDocActivity::class.java))
            intent.extras?.getString("categoryTitle").let {
                sendIntent.putExtra("CATEGORY_NAME", it)
            }
            startActivity(sendIntent)
        }

        sortTitleTv = findViewById(R.id.sortTitleTv)

        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }
        changeRvList = findViewById(R.id.changeRvList)
        changeRvList.setOnClickListener { changeRvItemListView() }
        changeListOrderRL = findViewById(R.id.changeListOrderRL)
        changeListOrderRL.setOnClickListener { changeSortBy() }

        searchView = findViewById(R.id.searchView)
        //initially show all document under current category//
        searDoc()

        rvSearchDoc = findViewById(R.id.rvSearchDoc)
        docRvAdapter = DocRvAdapter(this, this)
        rvSearchDoc.layoutManager = LinearLayoutManager(this)
        searchList = ArrayList()

        //Update Recyclerview Type//
        updateViewType()

        //Update Sort List//
        updateSortList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeSortBy() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_item_sort_by)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val nameRadioButton = dialog.findViewById<RadioButton>(R.id.nameRadioButton)
        val categoryRadioButton = dialog.findViewById<RadioButton>(R.id.categoryRadioButton)
        val categorySortViewRl = dialog.findViewById<RelativeLayout>(R.id.categorySortViewRl)
        categorySortViewRl.isVisible = false
        val dateRadioButton = dialog.findViewById<RadioButton>(R.id.dateRadioButton)
        val sortByNameTv = dialog.findViewById<TextView>(R.id.sortByNameTv)
        val sortByCategoryTv = dialog.findViewById<TextView>(R.id.sortByCategoryTv)
        val sortByDateTv = dialog.findViewById<TextView>(R.id.sortByDateTv)

        val location = IntArray(2)
        changeListOrderRL.getLocationOnScreen(location)

        val wmlp = dialog.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.START
        wmlp.x = location[0] - 100
        wmlp.y = location[1] + 20

        val saveSortTypeSP = getSharedPreferences("SORT_BY", MODE_PRIVATE)

        if (saveSortTypeSP.getString("SORT", "name").equals("name")) {
            nameRadioButton.isChecked = true
            sortByNameTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }
        else {
            dateRadioButton.isChecked = true
            sortByDateTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }

        nameRadioButton.setOnClickListener {
            isSortType = 1
            Variables.searchTxT.let { it?.let { it1 ->
                updateRvWhenSortByUser("%${categoryTitle.text}%", it1, isSortType) } }
            nameRadioButton.isChecked = true
            sortTitleTv.text = "Sort by Name"
            sortByNameTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
            categoryRadioButton.isChecked = false
            sortByCategoryTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            dateRadioButton.isChecked = false
            sortByDateTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            saveSortTypeSP.edit().putString("SORT", "name").apply()
            updateViewType()
            dialog.dismiss()
        }
        dateRadioButton.setOnClickListener {
            isSortType = 2
            Variables.searchTxT.let { it?.let { it1 ->
                updateRvWhenSortByUser("%${categoryTitle.text}%", it1, isSortType) } }
            dateRadioButton.isChecked = true
            sortTitleTv.text = "Sort by Date"
            sortByDateTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
            nameRadioButton.isChecked = false
            sortByNameTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            categoryRadioButton.isChecked = false
            sortByCategoryTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            saveSortTypeSP.edit().putString("SORT", "date").apply()
            updateViewType()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSortList() {
        val saveSortTypeSP = getSharedPreferences("SORT_BY", MODE_PRIVATE)

        if (saveSortTypeSP.getString("SORT", "name").equals("name")) {
            sortTitleTv.text = "Sort by Name"
        }
        else {
            sortTitleTv.text = "Sort by Date"
        }
    }

    private fun updateViewType() {
        val sp = getSharedPreferences("VIEW_SET", MODE_PRIVATE)
        if (sp.getString("VIEW", "LIST").equals("LIST")) {
            changeRvList.setImageResource(R.drawable.ic_list_view)
            docRvAdapter.setViewType("LIST")
            rvSearchDoc.layoutManager = LinearLayoutManager(this)
            rvSearchDoc.adapter = docRvAdapter
        } else if (sp.getString("VIEW", "COMPACT").equals("COMPACT")) {
            changeRvList.setImageResource(R.drawable.ic_compact)
            docRvAdapter.setViewType("COMPACT")
            rvSearchDoc.layoutManager = LinearLayoutManager(this)
            rvSearchDoc.adapter = docRvAdapter
        } else {
            changeRvList.setImageResource(R.drawable.ic_grid_view)
            docRvAdapter.setViewType("GRID")
            rvSearchDoc.layoutManager = GridLayoutManager(this, 3)
            rvSearchDoc.adapter = docRvAdapter
        }
    }

    private fun changeRvItemListView() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_item_view_change)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val listRadioButton = dialog.findViewById<RadioButton>(R.id.listRadioButton)
        val compactRadioButton = dialog.findViewById<RadioButton>(R.id.compactRadioButton)
        val gridRadioButton = dialog.findViewById<RadioButton>(R.id.gridRadioButton)
        val listTv = dialog.findViewById<TextView>(R.id.listTv)
        val compactTv = dialog.findViewById<TextView>(R.id.compactTv)
        val gridTv = dialog.findViewById<TextView>(R.id.gridTv)

        val location = IntArray(2)
        gridRadioButton.getLocationOnScreen(location)

        val wmlp = dialog.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.END
        wmlp.x = location[0] + 50
        wmlp.y = location[1] + 80

        val saveViewTypeSP = getSharedPreferences("VIEW_SET", MODE_PRIVATE)

        if (saveViewTypeSP.getString("VIEW", "LIST").equals("LIST")) {
            listRadioButton.isChecked = true
            listTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else if (saveViewTypeSP.getString("VIEW", "COMPACT").equals("COMPACT")) {
            compactRadioButton.isChecked = true
            compactTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else {
            gridRadioButton.isChecked = true
            gridTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }

        listRadioButton.setOnClickListener {
            saveViewTypeSP.edit().putString("VIEW", "LIST").apply()
            listRadioButton.isChecked = true
            listTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
            compactRadioButton.isChecked = false
            compactTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            gridRadioButton.isChecked = false
            gridTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            updateViewType()
            dialog.dismiss()
        }
        compactRadioButton.setOnClickListener {
            saveViewTypeSP.edit().putString("VIEW", "COMPACT").apply()
            compactRadioButton.isChecked = true
            compactTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
            listRadioButton.isChecked = false
            listTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            gridRadioButton.isChecked = false
            gridTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            updateViewType()
            dialog.dismiss()
        }
        gridRadioButton.setOnClickListener {
            saveViewTypeSP.edit().putString("VIEW", "GRID").apply()
            gridRadioButton.isChecked = true
            gridTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
            listRadioButton.isChecked = false
            listTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            compactRadioButton.isChecked = false
            compactTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            updateViewType()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun searDoc() {
        intent.extras?.getString("categoryTitle").let {
            categoryTitle.text = it
            val empty = intent.extras!!.getBoolean("isEmptyOrNot", false)
            Log.i("TEST", "CategoryModel => $it , $empty")
            if (empty) {
                noDocYetTv.visibility = View.VISIBLE
            } else {
                noDocYetTv.visibility = View.GONE
            }
            searching("")
            //updateRvWhenSortByUser("%$it%","", isSortType)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searching(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noDocYetTv.visibility = View.GONE
                searching(newText)
                return false
            }
        })
    }

    private fun searching(query: String?) {
        if (query!!.isEmpty()) {
            docDao!!.searchDocByCategoryDefault("%${categoryTitle.text}%", isSortType).observe(this) {
                docRvAdapter.update(it as ArrayList<DocModel>)
            }
            //docRvAdapter.update(ArrayList())
        } else {
            Variables.searchTxT = "%$query%"
            updateRvWhenSortByUser("%${categoryTitle.text}%",Variables.searchTxT!!, isSortType)
        }
    }

    private fun updateRvWhenSortByUser(category: String, query: String, sortType: Int) {
        docDao!!.searchDocByCategory(category, query, sortType).observe(this) {
            docRvAdapter.update(it as ArrayList<DocModel>)
        }
    }

    override fun onLongClick(model: DocModel) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_delete_doc)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<TextView>(R.id.cancelBtnTv).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<TextView>(R.id.deleteBtnTv).setOnClickListener {
            showSnackBar("Delete Successfully")
            GlobalScope.launch {
                File(model.filePath).delete()
                viewModel.deleteData(model)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSnackBar(message: String) {
        val view = findViewById<View>(android.R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}