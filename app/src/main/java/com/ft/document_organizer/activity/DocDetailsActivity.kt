package com.ft.document_organizer.activity

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ft.document_organizer.BuildConfig
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.CategoryDetailsAdapter
import com.ft.document_organizer.adapder.CategoryFieldEditAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.helper.TouchImageView
import com.ft.document_organizer.model.CategoryFieldModel
import com.ft.document_organizer.utils.DocUtils
import com.ft.document_organizer.utils.Variables
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class DocDetailsActivity : AppCompatActivity() {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel
    private lateinit var docDao: DocDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryFieldDao: CategoryFieldDao

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private lateinit var backImgV: ImageView
    private lateinit var cloudImaV: ImageView
    private lateinit var docTitleTv: TextView
    private lateinit var cvDetails: CardView
    private lateinit var editImgV: ImageView
    private lateinit var docTouchImgV: TouchImageView
    private lateinit var docImgV: ImageView
    private lateinit var folderIcon: ImageView
    private lateinit var docNameTv: TextView
    private lateinit var categoryNameTv: TextView
    private lateinit var fileSizeTv: TextView
    private lateinit var imagePixelsTv: TextView
    private lateinit var dateTv: TextView
    private lateinit var lL: LinearLayout
    private lateinit var rvCategoryFields: RecyclerView
    private lateinit var categoryDetailsAdapter: CategoryDetailsAdapter
    private lateinit var fieldsDetails: ArrayList<String>

    private lateinit var rvUpdateCategoryFields: RecyclerView
    private lateinit var categoryDetailsUpdateAdapter: CategoryFieldEditAdapter
    private lateinit var updateFieldList: ArrayList<String>

    private lateinit var progressDialog: ProgressDialog
    private var isShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_details)

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

        //Firebase related all variables//
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        backImgV = findViewById(R.id.backImgV)
        backImgV.setOnClickListener { finish() }

        cloudImaV = findViewById(R.id.cloudImaV)
        cloudImaV.setOnClickListener { signIn() }

        findViewById<CardView>(R.id.cvDetailsButton).setOnClickListener { showDetailsCardView() }
        cvDetails = findViewById(R.id.cvDetails)

        editImgV = findViewById(R.id.editImgV)
        editImgV.setOnClickListener { edit() }

        docTouchImgV = findViewById(R.id.docTouchImgV)
        docImgV = findViewById(R.id.docImgV)

        docNameTv = findViewById(R.id.docNameTv)
        docNameTv.text = intent.extras?.getString("docName")

        docTitleTv = findViewById(R.id.docTitleTv)
        val name = intent.extras?.getString("docName")
        docTitleTv.text = name?.substring(0, name.lastIndexOf('.'))

        categoryNameTv = findViewById(R.id.categoryNameTv)
        categoryNameTv.text = intent.extras?.getString("docCategoryName")

        //get file size//
        fileSizeTv = findViewById(R.id.fileSizeTv)
        val filePath = intent.extras?.getString("filePath").toString()
        fileSizeTv.text = Html.fromHtml("<font color=#42435A><b>Size : </b></font>${DocUtils.getFileSize(File(filePath).length())}")

        imagePixelsTv = findViewById(R.id.imagePixelsTv)

        lL = findViewById(R.id.lL)
        lL.setBackgroundColor(intent.extras?.getInt("docCategoryIconBg")!!)

        folderIcon = findViewById(R.id.folderIcon)
        folderIcon.setImageResource(intent.extras?.getInt("docCategoryIcon")!!)

        dateTv = findViewById(R.id.dateTv)
        dateTv.text = intent.extras?.getString("date")


        rvCategoryFields = findViewById(R.id.rvCategoryFields)
        categoryDetailsAdapter = CategoryDetailsAdapter(this)
        rvCategoryFields.layoutManager = LinearLayoutManager(this)
        rvCategoryFields.adapter = categoryDetailsAdapter
        loadDetails()

        //catch intent data//
        getIntentDataFromDocRvAdapter()

        //synced checker//
        val docName = intent.extras?.getString("docName")
        docName?.let { isCloudSync(it) }

        progressDialog = ProgressDialog(this)
    }

    private fun getIntentDataFromDocRvAdapter() {
        when (intent.extras?.getString("mimType")) {
            "image/jpeg" -> {
                val filePath = intent.extras?.getString("filePath").toString()
                //get image pixels//
                val uri: Uri = getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val imageWidth: Int = bitmap.width
                val imageHeight: Int = bitmap.height
                imagePixelsTv.text = Html.fromHtml("<font color=#42435A><b>(W x H) : </b></font>$imageWidth X $imageHeight Px")
                Log.i("IMAGE_PX", "IMAGE ASPECT RATIO (W:H) : $imageWidth X $imageHeight px")

                docTouchImgV.visibility = View.VISIBLE
                docImgV.visibility = View.GONE
                docTouchImgV.setImageBitmap(BitmapFactory.decodeFile(filePath))
            }
            "application/pdf" -> {
                docImgV.setImageResource(R.drawable.pdf)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/msword" -> {
                docImgV.setImageResource(R.drawable.word)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                docImgV.setImageResource(R.drawable.word)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/vnd.ms-excel" -> {
                docImgV.setImageResource(R.drawable.xls)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                docImgV.setImageResource(R.drawable.xls)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/vnd.ms-powerpoint" -> {
                docImgV.setImageResource(R.drawable.powerpoint)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                docImgV.setImageResource(R.drawable.powerpoint)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "text/plain" -> {
                docImgV.setImageResource(R.drawable.text)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "text/css" -> {
                docImgV.setImageResource(R.drawable.css)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "text/csv" -> {
                docImgV.setImageResource(R.drawable.csv)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "text/html" -> {
                docImgV.setImageResource(R.drawable.html)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/zip" -> {
                docImgV.setImageResource(R.drawable.zip)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            "application/x-rar", "application/vnd.rar" -> {
                docImgV.setImageResource(R.drawable.rar)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
            else -> {
                docImgV.setImageResource(R.drawable.document)
                docImgV.visibility = View.VISIBLE
                docTouchImgV.visibility = View.GONE
                docImgV.setOnClickListener { openFileByPassingIntent() }
            }
        }
    }

    private fun openFileByPassingIntent() {
        try {
            GlobalScope.launch {
                val id = intent.extras?.getInt("id")
                val dModel = docDao.findById(id!!)
                val mimType = intent.extras?.getString("mimType")
                val filePath = dModel.filePath
                val uri: Uri = getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", File(filePath!!))
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, mimType)
                intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
                startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            showSnackBar("Error! : ${e.message}")
        }
    }

    private fun loadDetails() {
        val docCategoryName = intent.extras?.getString("docCategoryName")
        fieldsDetails = ArrayList()
        categoryFieldDao.getCategoryFields(docCategoryName!!).observe(this) { model ->
            model.forEach {
                fieldsDetails.add("${it.fieldTitle} : ${it.description}")
            }
            categoryDetailsAdapter.update(fieldsDetails)
            fieldsDetails.clear()
        }
    }

    private fun isCloudSync(docName: String) {
        GlobalScope.launch {
            if (docDao.isSync(docName)) {
                cloudImaV.setImageResource(R.drawable.ic_cloud_sync_done)
            } else {
                cloudImaV.setImageResource(R.drawable.ic_cloud_sync)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun edit() {
        val editDialog = Dialog(this)
        editDialog.setContentView(R.layout.dialog_rename)
        editDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val id = intent.extras?.getInt("id")
        val docCategoryName = intent.extras?.getString("docCategoryName")
        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
        val date = simpleDateFormat.format(Date())

        val renameDoc = editDialog.findViewById<EditText>(R.id.renameET)

        //update/edit data//
        rvUpdateCategoryFields = editDialog.findViewById(R.id.editFieldsRv)
        rvUpdateCategoryFields.layoutManager = LinearLayoutManager(this)
        categoryDetailsUpdateAdapter = CategoryFieldEditAdapter(this)
        rvUpdateCategoryFields.adapter = categoryDetailsUpdateAdapter
        updateFieldList = arrayListOf()

        GlobalScope.launch {
            //update category field//
            categoryFieldDao.searchFields(docCategoryName!!).forEach {
                if (it.description.isEmpty()){
                    updateFieldList.add("${it.fieldTitle} [Optional]")
                }else{
                    updateFieldList.add(it.description)
                }
            }
            categoryDetailsUpdateAdapter.update(updateFieldList)
        }

        try {
            GlobalScope.launch {
                val dModel = docDao.findById(id!!)
                val parentFilePath = dModel.filePath.substring(0, dModel.filePath.lastIndexOf("/"))
                val oldDocName = dModel.filePath.substring(
                    dModel.filePath.lastIndexOf("/") + 1,
                    dModel.filePath.length
                )
                val docName = oldDocName.substring(0, oldDocName.lastIndexOf("."))
                val extension = oldDocName.substring(oldDocName.lastIndexOf("."))
                renameDoc.setText(docName)
                editDialog.findViewById<TextView>(R.id.saveBtnTv).setOnClickListener {
                    if (renameDoc.text.isEmpty()) {
                        showSnackBar("Name is Empty")
                    } else {
                        GlobalScope.launch {
                            val newFilePath = "$parentFilePath/${renameDoc.text}${extension}"
                            dModel.filePath.let { fromFile ->
                                File(fromFile).renameTo(
                                    File(
                                        newFilePath
                                    )
                                )
                            }
                            val uri: Uri = getUriForFile(
                                applicationContext,
                                BuildConfig.APPLICATION_ID + ".provider",
                                File(newFilePath)
                            )
                            //update document name, date//
                            //Variables.isRename = true
                            Variables.dirPath = newFilePath
                            try {
                                Log.i("RENAME_DOC", "old name : ${oldDocName}")
                                Log.i("RENAME_DOC", "new name : ${renameDoc.text}")
                                Log.i("RENAME_DOC", "old file path : ${dModel.filePath}")
                                Log.i("RENAME_DOC", "new file path : ${newFilePath}")

                                val docModel = docDao.findById(id)
                                docModel.docName = "${renameDoc.text}${extension}"
                                docModel.docUri = uri.toString()
                                docModel.filePath = newFilePath
                                docModel.date = date
                                viewModel.updateData(docModel)

                                var i = 0
                                categoryFieldDao.searchFields(docCategoryName!!).forEach {
                                    it.description =
                                        rvUpdateCategoryFields[i].findViewById<EditText>(R.id.descriptionET).text.toString()
                                    viewModel.updateData(it)
                                    i++
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        Variables.documentName = renameDoc.text.toString()
                        docNameTv.text = "${renameDoc.text}${extension}"
                        docTitleTv.text = renameDoc.text
                        showSnackBar("Renamed")
                    }
                    editDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        editDialog.findViewById<TextView>(R.id.cancelBtnTv).setOnClickListener { editDialog.dismiss() }
        editDialog.show()
    }

    private fun showDetailsCardView() {
        if (!isShow) {
            isShow = !isShow
            cvDetails.visibility = View.VISIBLE
        } else {
            isShow = !isShow
            cvDetails.visibility = View.GONE
        }
    }

    private fun signIn() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            syncToCloud()
        } else {
            startActivity(Intent(this@DocDetailsActivity, SyncActivity::class.java))
        }
    }

    private fun syncToCloud() {
        val isSync = intent.extras?.getBoolean("isSyncOrNot")!!
        val mimType = intent.extras?.getString("mimType")
        val docName = intent.extras?.getString("docName")
        val docUri = intent.extras?.getString("docUri")
        val filePath = intent.extras?.getString("filePath")
        val docCategoryName = intent.extras?.getString("docCategoryName")
        val docCategoryIcon = intent.extras?.getInt("docCategoryIcon")
        val docCategoryIconBg = intent.extras?.getInt("docCategoryIconBg")
        val date = intent.extras?.getString("date")
        var model: CategoryFieldModel? = null
        val db = Firebase.firestore.document("${auth.currentUser!!.uid}/$docName")
        if (isSync) {
            showSnackBar("Already synced this document")
        } else {
            showProgressDialog()
            GlobalScope.launch {
                model = categoryFieldDao.findByCategoryFieldName(docCategoryName!!)
            }
            storageRef = firebaseStorage.reference.child(auth.currentUser!!.uid)
            storageRef.child(docName!!)
                .putFile(Uri.fromFile(File(filePath.toString())))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        hideProgressDialog()
                        firebaseStorage.reference.child(auth.currentUser!!.uid)
                            .child(docName).downloadUrl.addOnCompleteListener { uri ->
                                val hashMap = hashMapOf<String, Any>(
                                    "docName" to docName,
                                    "uriPath" to uri.result,
                                    "filePath" to filePath!!,
                                    "mimType" to mimType!!,
                                    "docCategoryName" to docCategoryName!!,
                                    "docCategoryIcon" to docCategoryIcon!!,
                                    "docCategoryIconBg" to docCategoryIconBg!!,
                                    "date" to date!!,
                                    "fieldTitle" to model!!.fieldTitle,
                                    "description" to model!!.description
                                )
                                db.set(hashMap)
                            }

                        GlobalScope.launch {
                            try {
                                val docModel = docDao.findByName(docName)
                                docModel.isSynced = true
                                viewModel.updateData(docModel)
                                cloudImaV.setImageResource(R.drawable.ic_cloud_sync_done)
                            } catch (e: Exception) {
                                hideProgressDialog()
                                showSnackBar(e.message.toString())
                            }
                        }
                    } else {
                        hideProgressDialog()
                        showSnackBar(it.exception?.message.toString())
                    }
                }
                .addOnFailureListener {
                    hideProgressDialog()
                    showSnackBar("Error!!")
                }
        }
    }

    private fun showProgressDialog() {
        progressDialog.setTitle("Syncing..")
        progressDialog.setMessage("Wait while Syncing.")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideProgressDialog() {
        progressDialog.dismiss()
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