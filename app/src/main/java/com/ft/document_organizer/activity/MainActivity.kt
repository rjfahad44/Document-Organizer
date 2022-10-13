package com.ft.document_organizer.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.CategoryHomeRvAdapter
import com.ft.document_organizer.adapder.DocRvAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.interFace.LongItemClickListener
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.model.DocModel
import com.ft.document_organizer.utils.DocUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), LongItemClickListener {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel
    private lateinit var docDao: DocDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryFieldDao: CategoryFieldDao

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var plusImgBtn: ImageView
    private lateinit var changeRvList: ImageView
    private lateinit var addDoc: RelativeLayout

    //Recyclerview show from Category
    private lateinit var categoricalAdapter: CategoryHomeRvAdapter
    private lateinit var categoricalRV: RecyclerView
    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var noDocYetTv: TextView

    //RecyclerView show from Documents
    private lateinit var docRv: RecyclerView
    private lateinit var docRvAdapter: DocRvAdapter
    private lateinit var docList: ArrayList<DocModel>

    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeWidget()
    }

    private fun initializeWidget() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DocViewModel::class.java]

        docDao = DocDatabase.getDatabase(this).getDao()
        categoryDao = DocDatabase.getDatabase(this).getCategoryDao()
        categoryFieldDao = DocDatabase.getDatabase(this).getCategoryFieldDao()

        auth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Open menu dialog method//
        findViewById<ImageView>(R.id.menuImgV).setOnClickListener { showMenu() }

        //Add new Document method//
        plusImgBtn = findViewById(R.id.plusImgBtn)
        plusImgBtn.setOnClickListener { addDoc() }
        addDoc = findViewById(R.id.addDoc)
        addDoc.setOnClickListener { addDoc() }

        findViewById<LinearLayout>(R.id.addCategoryLL).setOnClickListener {
            startActivity(Intent(this@MainActivity, AddNewCategoryActivity::class.java))
        }

        changeRvList = findViewById(R.id.changeRvList)
        //Change RecyclerView List or Grid View//
        changeRvList.setOnClickListener { changeRvItemListView() }

        findViewById<ImageView>(R.id.searchImgV).setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    DocSearchActivity::class.java
                )
            )
        }

        categoricalRV = findViewById(R.id.category_rv)
        categoryList = ArrayList()
        categoricalAdapter = CategoryHomeRvAdapter(this)
        categoricalRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoricalRV.adapter = categoricalAdapter
        //Load Category List//
        loadCategories()

        noDocYetTv = findViewById(R.id.noDocYetTv)
        docRv = findViewById(R.id.rvDoc)
        docList = ArrayList()
        docRvAdapter = DocRvAdapter(this, this)

        progressDialog = Dialog(this)

        loadDocRvLayout()
        loadData()
    }

    private fun loadDocRvLayout() {
        val sp = getSharedPreferences("SET_VIEW_TYPE", MODE_PRIVATE)
        if (sp.getString("VIEW", "LIST").equals("LIST")) {
            changeRvList.setImageResource(R.drawable.ic_list_view)
            docRvAdapter.setViewType("LIST")
            docRv.layoutManager = LinearLayoutManager(this)
            docRv.adapter = docRvAdapter
        } else if (sp.getString("VIEW", "COMPACT").equals("COMPACT")) {
            changeRvList.setImageResource(R.drawable.ic_compact)
            docRvAdapter.setViewType("COMPACT")
            docRv.layoutManager = LinearLayoutManager(this)
            docRv.adapter = docRvAdapter
        } else {
            changeRvList.setImageResource(R.drawable.ic_grid_view)
            docRvAdapter.setViewType("GRID")
            docRv.layoutManager = GridLayoutManager(this, 3)
            docRv.adapter = docRvAdapter
        }
    }

    private fun loadData() {
        viewModel.allDoc.observe(this) {
            if (it.isEmpty()) {
                noDocYetTv.visibility = View.VISIBLE
                addDoc.visibility = View.VISIBLE
            } else {
                noDocYetTv.visibility = View.GONE
                addDoc.visibility = View.GONE
            }
            docRvAdapter.update(it as ArrayList<DocModel>)
        }
    }

    private fun changeRvItemListView() {
        val viewChangeDialog = Dialog(this)
        viewChangeDialog.setContentView(R.layout.dialog_item_view_change)
        viewChangeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val listRadioButton = viewChangeDialog.findViewById<RadioButton>(R.id.listRadioButton)
        val compactRadioButton = viewChangeDialog.findViewById<RadioButton>(R.id.compactRadioButton)
        val gridRadioButton = viewChangeDialog.findViewById<RadioButton>(R.id.gridRadioButton)
        val listTv = viewChangeDialog.findViewById<TextView>(R.id.listTv)
        val compactTv = viewChangeDialog.findViewById<TextView>(R.id.compactTv)
        val gridTv = viewChangeDialog.findViewById<TextView>(R.id.gridTv)

        val location = IntArray(2)
        changeRvList.getLocationOnScreen(location)

        val wmlp = viewChangeDialog.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.START
        wmlp.x = location[0] - 100
        wmlp.y = location[1] + 20

        val sp = getSharedPreferences("SET_VIEW_TYPE", MODE_PRIVATE)
        val saveViewTypeSP = getSharedPreferences("USER_CLICK", MODE_PRIVATE)

        if (saveViewTypeSP.getString("VIEW", "LIST").equals("LIST")) {
            listRadioButton?.isChecked = true
            changeRvList.setImageResource(R.drawable.ic_list_view)
            listTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else if (saveViewTypeSP.getString("VIEW", "COMPACT").equals("COMPACT")) {
            compactRadioButton?.isChecked = true
            changeRvList.setImageResource(R.drawable.ic_compact)
            compactTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else {
            gridRadioButton?.isChecked = true
            changeRvList.setImageResource(R.drawable.ic_grid_view)
            gridTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }

        listRadioButton?.setOnClickListener {
            listRadioButton.isChecked = true
            listTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
            compactRadioButton?.isChecked = false
            compactTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            gridRadioButton?.isChecked = false
            gridTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            sp.edit().putString("VIEW", "LIST").apply()
            loadDocRvLayout()
            saveViewTypeSP.edit().putString("VIEW", "LIST").apply()
            viewChangeDialog.dismiss()
        }
        compactRadioButton?.setOnClickListener {
            compactRadioButton.isChecked = true
            compactTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
            listRadioButton?.isChecked = false
            listTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            gridRadioButton?.isChecked = false
            gridTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            sp.edit().putString("VIEW", "COMPACT").apply()
            loadDocRvLayout()
            saveViewTypeSP.edit().putString("VIEW", "COMPACT").apply()
            viewChangeDialog.dismiss()
        }
        gridRadioButton?.setOnClickListener {
            gridRadioButton.isChecked = true
            gridTv?.setTextColor(ContextCompat.getColor(this, R.color.blue))
            listRadioButton?.isChecked = false
            listTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            compactRadioButton?.isChecked = false
            compactTv?.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            sp.edit().putString("VIEW", "GRID").apply()
            loadDocRvLayout()
            saveViewTypeSP.edit().putString("VIEW", "GRID").apply()
            viewChangeDialog.dismiss()
        }

        viewChangeDialog.show()
    }

    private fun showMenu() {
        val menuDialog = Dialog(this)
        menuDialog.setContentView(R.layout.dialog_menu_view)
        menuDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        menuDialog.setCancelable(true)

        //DocUtils.rotateView(findViewById(R.id.menuImgV), 0f, 90f)
        menuDialog.findViewById<RelativeLayout>(R.id.categoryRl)?.setOnClickListener {
            startActivity(Intent(this@MainActivity, CategoriesActivity::class.java))
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }
        menuDialog.findViewById<RelativeLayout>(R.id.syncRl)?.setOnClickListener {
            startActivity(Intent(this@MainActivity, SyncActivity::class.java))
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }
        menuDialog.findViewById<RelativeLayout>(R.id.settingsRl)?.setOnClickListener {
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }
        menuDialog.findViewById<RelativeLayout>(R.id.aboutRl)?.setOnClickListener {
            startActivity(Intent(this@MainActivity, AboutActivity::class.java))
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }
        menuDialog.findViewById<RelativeLayout>(R.id.signOutRl)?.setOnClickListener {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                auth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
                        val backupDate = simpleDateFormat.format(Date())
                        val sp = getSharedPreferences("BACKUP_DATE_SIZE", MODE_PRIVATE)
                        sp.edit().putString("DATE", backupDate).putString("SIZE", "0.0KB").apply()
                        GlobalScope.launch {
                            categoryDao.allCategory().forEach { cModel ->
                                cModel.counter = 0
                                viewModel.updateData(cModel)
                            }
                            docDao.deleteAllDoc()
                            File(
                                DocUtils.createDirectory(applicationContext)
                            ).deleteRecursively()
                        }
                        showSnackBar("Sign Out")
                    } else {
                        showSnackBar(it.result.toString())
                    }
                }
            } else {
                showSnackBar("Already Sign Out.")
            }
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }

        menuDialog.findViewById<ImageView>(R.id.closeImgV)?.setOnClickListener {
            //DocUtils.rotateView(findViewById(R.id.menuImgV), 90f, 0f)
            menuDialog.dismiss()
        }
        menuDialog.show()
    }

    private fun addDoc() {
        startActivity(Intent(this@MainActivity, AddNewDocActivity::class.java))
    }

    private fun loadCategories() {
        viewModel.allCategory.observe(this) {
            categoricalAdapter.update(it as ArrayList<CategoryModel>)
        }
    }


    override fun onLongClick(model: DocModel) {
        val deleteDialog = Dialog(this)
        val categoryDao = DocDatabase.getDatabase(this).getCategoryDao()
        deleteDialog.setContentView(R.layout.dialog_delete_doc)
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        deleteDialog.findViewById<TextView>(R.id.cancelBtnTv)
            ?.setOnClickListener { deleteDialog.dismiss() }
        deleteDialog.findViewById<TextView>(R.id.deleteBtnTv)?.setOnClickListener {
            GlobalScope.launch {
                showSnackBar("Delete Successfully")
                try {
                    GlobalScope.launch {
                        val categoryModel = categoryDao.findByCategoryName(model.docCategoryName)
                        if (categoryModel.counter > 0) {
                            categoryModel.counter = categoryModel.counter.minus(1)
                        }
                        viewModel.updateData(categoryModel)
                    }

                    try {
                        //Storage Data Delete From Firebase//
                        firebaseStorage.reference.child("${auth.currentUser!!.uid}/${model.docName}")
                            .delete()
                            .addOnCompleteListener {
                                Log.i("DELETE", "DELETE Firebase Storage Data SUCCESSFULLY")
                            }.addOnFailureListener {
                                Log.i("DELETE", "Firebase Storage : ${it.message}")
                            }

                        //Firestore Database Data Delete From Firebase//
                        Firebase.firestore.document("${auth.currentUser!!.uid}/${model.docName}")
                            .delete()
                            .addOnSuccessListener {
                                Log.i("DELETE", "DELETE Firestore Database data SUCCESSFULLY")
                            }.addOnFailureListener {
                                Log.i("DELETE", "Firestore Database : ${it.message}")
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //External Data Delete//
                    File(model.filePath).delete()

                    //Delete data from Database//
                    viewModel.deleteData(model)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            deleteDialog.dismiss()
        }
        deleteDialog.show()
    }

    private fun showSnackBar(message: String) {
        val view = findViewById<View>(android.R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}