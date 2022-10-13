package com.ft.document_organizer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.EditTextAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.model.DocModel
import com.ft.document_organizer.utils.DocUtils
import com.ft.document_organizer.utils.Variables
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddNewDocActivity : AppCompatActivity() {

    private lateinit var viewModel: DocViewModel
    private lateinit var docDao: DocDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryFieldDao: CategoryFieldDao


    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var dialog: Dialog? = null

    private lateinit var saveDocTv: TextView
    private lateinit var docNameET: TextView
    private lateinit var cvDocContainer: CardView
    private lateinit var selectedDocPreview: ImageView
    private lateinit var noDocSelected: TextView
    private lateinit var spinnerForSelectCategory: Spinner
    private lateinit var spinnerList: ArrayList<String>
    private lateinit var spinnerAdapter: ArrayAdapter<*>
    private lateinit var lL: LinearLayout
    private lateinit var categoryIconImgV: ImageView

    private lateinit var from: File
    private lateinit var to: String
    private lateinit var date: String
    private var categoryModel: CategoryModel? = null

    private lateinit var addFieldCategoryRV: RecyclerView
    private lateinit var editTextAdapter: EditTextAdapter
    private lateinit var editTextList: ArrayList<String>

    private val REQUEST_CODE = 100
    private val REQUEST_OPEN_FILE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_doc)

        initializeWidget()
    }

    private fun initializeWidget() {
        dialog = Dialog(this)

        firebaseStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        docDao = DocDatabase.getDatabase(this).getDao()
        categoryDao = DocDatabase.getDatabase(this).getCategoryDao()
        categoryFieldDao = DocDatabase.getDatabase(this).getCategoryFieldDao()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DocViewModel::class.java]

        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }

        saveDocTv = findViewById(R.id.saveDocTv)
        saveDocTv.setOnClickListener { saveDocument() }

        selectedDocPreview = findViewById(R.id.selectedDocPreview)
        noDocSelected = findViewById(R.id.noDocSelected)

        docNameET = findViewById(R.id.docNameET)

        findViewById<RelativeLayout>(R.id.addDocSelectFromRl).setOnClickListener { openSelectMenu() }
        findViewById<RelativeLayout>(R.id.addDocByCameraRl).setOnClickListener {
            addDocByCamera(ScanConstants.OPEN_CAMERA)
        }
        cvDocContainer = findViewById(R.id.cvDocContainer)

        lL = findViewById(R.id.lL)
        categoryIconImgV = findViewById(R.id.categoryIconImgV)


        spinnerForSelectCategory = findViewById(R.id.spinnerForSelectCategory)
        spinnerDropdownList()

        //add edit text field dynamically//
        addFieldCategoryRV = findViewById(R.id.addFieldCategoryRV)
        editTextAdapter = EditTextAdapter(this)
        addFieldCategoryRV.layoutManager =
            LinearLayoutManager(this)
        addFieldCategoryRV.adapter = editTextAdapter
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveDocument() {
        showSnackBar("Saved Successfully")
        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
        date = simpleDateFormat.format(Date())

        GlobalScope.launch {
            try {
                categoryModel = Variables.categoryName?.let { categoryDao.findByCategoryName(it) }
                categoryModel?.counter = categoryModel?.counter?.plus(1)!!
                viewModel.updateData(categoryModel!!)

                var i = 0
                Variables.categoryName?.let {
                    categoryFieldDao.searchFields(it).forEach {
                        //val position = editTextAdapter.itemCount
                        it.description =
                            addFieldCategoryRV[i].findViewById<EditText>(R.id.descriptionET).text.toString()
                        viewModel.updateData(it)
                        i++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            to = "${Variables.dirPath}/${docNameET.text}${Variables.extension}"
            //from.copyTo(File(to))
            //DocUtils.copyFile(from, File(to))
            Variables.uri?.let {
                DocUtils.copyFileToInternalStorage(
                    applicationContext,
                    it,
                    File(to)
                )
            }
            viewModel.addData(
                DocModel(
                    false,
                    Variables.MimeType!!,
                    "${docNameET.text}${Variables.extension}",
                    Variables.uri.toString(),
                    to,
                    Variables.categoryName.toString(),
                    categoryModel!!.icon,
                    categoryModel!!.colorBg,
                    date
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (Variables.MimeType.equals("image/jpeg")) {
            try {
                contentResolver.delete(Variables.uri!!, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        finish()
        startActivity(Intent(this@AddNewDocActivity, MainActivity::class.java))
    }

    private fun categoryFields(cName: String) {
        editTextList = ArrayList()
        GlobalScope.launch {
            categoryFieldDao.searchFields(cName).forEach {
                editTextList.add("${it.fieldTitle} [Optional]")
            }
            runOnUiThread {
                editTextAdapter.update(editTextList)
            }
        }
    }

    private fun spinnerDropdownList() {
        spinnerList = arrayListOf()

        viewModel.allCategory.observe(this) { clistModel ->
            clistModel.forEach { cModel ->
                spinnerList.add(cModel.title)
            }
            spinnerAdapter = ArrayAdapter(this, R.layout.custom_spinner_view, spinnerList)
            spinnerForSelectCategory.adapter = spinnerAdapter

            intent.extras?.getString("CATEGORY_NAME")?.let { categoryTitle ->
                if (categoryTitle.isNotEmpty()) {
                    Variables.categoryName = categoryTitle
                    for (i in 0 until spinnerForSelectCategory.count) {
                        if (spinnerForSelectCategory.getItemAtPosition(i).toString() == categoryTitle) {
                            spinnerForSelectCategory.setSelection(i)
                            break
                        }
                    }
                    Log.i("CATEGORY_NAME", "CATEGORY_NAME : $categoryTitle")
                } else {
                    spinnerForSelectCategory.setSelection(0)
                }
            }
        }


        spinnerForSelectCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                Variables.categoryName = spinnerList[position]
                categoryFields(Variables.categoryName!!)

                GlobalScope.launch {
                    categoryModel = categoryDao.findByCategoryName(Variables.categoryName!!)
                    runOnUiThread {
                        lL.setBackgroundColor(categoryModel!!.colorBg)
                        categoryIconImgV.setImageResource(categoryModel!!.icon)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun startScan(preference: Int) {
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            Variables.dirPath = DocUtils.createDirectory(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                val uri = data!!.extras!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
                Variables.uri = uri
                Log.i("CAMERA_PROVIDER", "CAMERA URI => $uri")
                noDocSelected.visibility = View.GONE
                cvDocContainer.visibility = View.VISIBLE
                val contentResolver = this.contentResolver
                val stringMimeType = uri?.let { contentResolver.getType(it) }
                Variables.MimeType = stringMimeType
                val fileName =
                    uri.let { it?.let { it1 -> DocumentFile.fromSingleUri(this, it1) } }?.name
                Variables.documentName = fileName?.substring(0, fileName.lastIndexOf("."))
                Variables.extension = fileName?.substring(fileName.lastIndexOf("."))
                selectedDocPreview.setImageURI(uri)
                saveDocTv.isEnabled = true
                saveDocTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
                saveDocTv.compoundDrawables.getOrNull(2)
                    ?.setTint(ContextCompat.getColor(this, R.color.blue))
                val path = uri?.let { DocUtils.getPath(this, it) }?.let { File(it) }
                from = File(path!!.path)
                docNameET.text = Variables.documentName
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == REQUEST_OPEN_FILE && resultCode == RESULT_OK) {
            try {
                val uri = data?.data
                Variables.uri = uri
                noDocSelected.visibility = View.GONE
                cvDocContainer.visibility = View.VISIBLE
                saveDocTv.isEnabled = true
                val contentResolver = this.contentResolver
                val stringMimeType = contentResolver.getType(uri!!)
                Variables.MimeType = stringMimeType
                val fileName = uri.let { DocumentFile.fromSingleUri(this, it) }!!.name
                Variables.documentName = fileName?.substring(0, fileName.lastIndexOf("."))
                Variables.extension = fileName?.substring(fileName.lastIndexOf("."))
                saveDocTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
                saveDocTv.compoundDrawables.getOrNull(2)
                    ?.setTint(ContextCompat.getColor(this, R.color.blue))
                val path = DocUtils.getPath(this, uri)
                from = path?.let { File(it) }!!
                docNameET.text = Variables.documentName

                when (stringMimeType) {
                    "image/jpeg" -> {
                        getSharedPreferences(ScanConstants.SELECT_FOLDER_IMG, MODE_PRIVATE)
                            .edit()
                            .putString(ScanConstants.SELECT_FOLDER_IMG_URI, "$uri")
                            .apply()
                        startScan(ScanConstants.OPEN_FOLDER)
                    }
                    "application/pdf" -> {
                        selectedDocPreview.setImageResource(R.drawable.pdf)
                    }
                    "application/msword" -> {
                        selectedDocPreview.setImageResource(R.drawable.word)
                    }
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                        selectedDocPreview.setImageResource(R.drawable.word)
                    }
                    "application/vnd.ms-excel" -> {
                        selectedDocPreview.setImageResource(R.drawable.xls)
                    }
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                        selectedDocPreview.setImageResource(R.drawable.xls)
                    }
                    "application/vnd.ms-powerpoint" -> {
                        selectedDocPreview.setImageResource(R.drawable.powerpoint)
                    }
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                        selectedDocPreview.setImageResource(R.drawable.powerpoint)
                    }
                    "text/plain" -> {
                        selectedDocPreview.setImageResource(R.drawable.text)
                    }
                    "text/css" -> {
                        selectedDocPreview.setImageResource(R.drawable.css)
                    }
                    "text/csv" -> {
                        selectedDocPreview.setImageResource(R.drawable.csv)
                    }
                    "text/html" -> {
                        selectedDocPreview.setImageResource(R.drawable.html)
                    }
                    "application/zip" -> {
                        selectedDocPreview.setImageResource(R.drawable.zip)
                    }
                    "application/x-rar", "application/vnd.rar" -> {
                        selectedDocPreview.setImageResource(R.drawable.rar)
                    }
                    else -> {
                        selectedDocPreview.setImageResource(R.drawable.document)
                    }
                }

            } catch (e: Exception) {
                saveDocTv.isEnabled = false
                saveDocTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
                saveDocTv.compoundDrawables.getOrNull(2)
                    ?.setTint(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openSelectMenu() {
        dialog?.setContentView(R.layout.dialog_file_select_option)
        dialog?.window?.attributes?.gravity = Gravity.BOTTOM
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.findViewById<LinearLayout>(R.id.selectGalleryLL)?.setOnClickListener {
            dialog?.dismiss()
            addDocByGallery(ScanConstants.OPEN_MEDIA)
        }
        dialog?.findViewById<LinearLayout>(R.id.folderSelectLL)?.setOnClickListener {
            addDocByFile()
            dialog?.dismiss()
        }

        dialog?.show()
    }

    private fun addDocByFile() {
        if (isFileReadWritePermission()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"

            //only these mimetypes can be added//
            val mimeTypes = arrayOf(
                "image/jpeg",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "text/css",
                "text/csv",
                "text/html",
                "application/zip",
                "application/x-rar",
                "application/vnd.rar"
            )

            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, REQUEST_OPEN_FILE)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    private fun addDocByCamera(openCamera: Int) {
        if (isCameraPermission()) {
            startScan(openCamera)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ), 2
            )
        }
    }

    private fun isCameraPermission(): Boolean {
        return if (SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("TAG", "Permission is granted")
                true
            } else {
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.d("TAG", "Permission is automatically granted")
            true
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun addDocByGallery(openMedia: Int) {
        if (isFileReadWritePermission()) {
            startScan(openMedia)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 3
            )
        }
    }

    private fun isFileReadWritePermission(): Boolean {
        return if (SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("TAG", "Permission is granted")
                true
            } else {
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.d("TAG", "Permission is automatically granted")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, REQUEST_OPEN_FILE)
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan(ScanConstants.OPEN_CAMERA)
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan(ScanConstants.OPEN_MEDIA)
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
        }
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