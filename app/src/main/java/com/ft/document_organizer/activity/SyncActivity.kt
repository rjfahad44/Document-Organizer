package com.ft.document_organizer.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ft.document_organizer.R
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
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class SyncActivity : AppCompatActivity() {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel
    private lateinit var docDao: DocDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryFieldDao: CategoryFieldDao

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var googleSignIn: RelativeLayout
    private lateinit var singInTv: TextView
    private lateinit var upperTxtRv: RelativeLayout
    private lateinit var gmailAccountCv: CardView
    private lateinit var profileImg: ImageView
    private lateinit var gmailId: TextView
    private lateinit var googleLogout: RelativeLayout
    private lateinit var syncCv: CardView
    private lateinit var syncIcon: ImageView
    private lateinit var synchronizingRl: RelativeLayout
    private lateinit var aboveAccountTv: TextView
    private lateinit var backupSizeTV: TextView
    private lateinit var lastBackupDateTV: TextView
    private lateinit var sp: SharedPreferences
    private var backupSizeCount: Long = 0
    private lateinit var backupDate: String

    private lateinit var rotation: Animation
    private var firebaseDocListSize = 0

    private val G_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
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
        firestore = FirebaseFirestore.getInstance()

        googleSignIn = findViewById(R.id.googleSignIn)
        singInTv = findViewById(R.id.singInTv)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignIn.setOnClickListener { signInGoogle() }

        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }

        gmailAccountCv = findViewById(R.id.gmailAccountCv)
        upperTxtRv = findViewById(R.id.upperTxtRv)

        profileImg = findViewById(R.id.profileImg)
        gmailId = findViewById(R.id.gmailId)
        aboveAccountTv = findViewById(R.id.aboveAccountTv)

        //synchronizing update part//
        sp = getSharedPreferences("BACKUP_DATE_SIZE", MODE_PRIVATE)
        synchronizingRl = findViewById(R.id.synchronizingRl)
        lastBackupDateTV = findViewById(R.id.lastBackupDateTV)
        backupSizeTV = findViewById(R.id.backupSizeTV)
        lastBackupDateTV.text = sp.getString("DATE", null)
        backupSizeTV.text = sp.getString("SIZE", null)

        googleLogout = findViewById(R.id.googleLogout)
        googleLogout.setOnClickListener {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                auth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
                        backupDate = simpleDateFormat.format(Date())
                        sp.edit().putString("DATE", backupDate).putString("SIZE", "0.0KB").apply()
                        lastBackupDateTV.text = sp.getString("DATE", null)
                        backupSizeTV.text = sp.getString("SIZE", null)
                        updateUiWithSignIn(false)
                        try {
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        //rotate any view by rotate animation//
        rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotation.repeatCount = Animation.INFINITE

        syncCv = findViewById(R.id.syncCv)
        syncIcon = findViewById(R.id.syncIcon)
        syncCv.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy")
            backupDate = simpleDateFormat.format(Date())
            sp.edit().clear().apply()

            syncIcon.startAnimation(rotation)
            firestore.collection(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    GlobalScope.launch {
                        if (docDao.getAllDocument().size == documents.size()) {
                            if (documents.isEmpty) {
                                showSnackBar("No Documents Yet.")
                                syncIcon.clearAnimation()
                            } else {
                                showSnackBar("Data already synced.")
                                syncIcon.clearAnimation()
                            }
                        } else {
                            uploadData()
                        }
                    }
                }
        }
    }

    private fun signInGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, G_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == G_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    updateUI(account)
                }
            } catch (e: ApiException) {
                Log.i("AUTHENTICATION", "ApiException : ${e.message}")
                showSyncFailedDialog()
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUiWithSignIn(true)
                } else {
                    updateUiWithSignIn(false)
                }
            }.addOnFailureListener {
                Log.i("AUTHENTICATION", "addOnFailureListener : ${it.message}")
                Toast.makeText(this, "Error! Please try again", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.i("AUTHENTICATION", "Exception : ${e.message}")
        }

    }

    private fun updateUiWithSignIn(b: Boolean) {
        if (b) {
            //Toast.makeText(this, "Signing successfully", Toast.LENGTH_SHORT).show()
            googleSignIn.visibility = View.INVISIBLE
            upperTxtRv.isVisible = false
            googleLogout.isVisible = true
            aboveAccountTv.isVisible = true
            syncCv.isVisible = true
            synchronizingRl.isVisible = true
            gmailAccountCv.isVisible = true
            Log.i("PROFILE_IMAGE", auth.currentUser?.photoUrl.toString())
            auth.currentUser?.photoUrl?.let { DocUtils.loadImage(this, profileImg, it) }
            gmailId.text = auth.currentUser?.email

        } else {
            googleSignIn.visibility = View.VISIBLE
            upperTxtRv.isVisible = true
            gmailAccountCv.isVisible = false
            aboveAccountTv.isVisible = false
            googleLogout.isVisible = false
            syncCv.isVisible = false
            synchronizingRl.isVisible = false
        }
    }

    private fun downloadData() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Variables.dirPath = DocUtils.createDirectory(applicationContext)
            GlobalScope.launch {
                //get data from Firebase Firestore//
                firestore = FirebaseFirestore.getInstance()
                firestore.collection(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        firebaseDocListSize = documents.size()
                        for (document in documents) {
                            GlobalScope.launch {
                                Log.i("UPLOAD", "Download")
                                val dModel = DocModel(
                                    true,
                                    document.getString("mimType")!!,
                                    document.getString("docName")!!,
                                    "",
                                    document.getString("filePath")!!,
                                    document.getString("docCategoryName")!!,
                                    document.getLong("docCategoryIcon")!!.toInt(),
                                    document.getLong("docCategoryIconBg")!!.toInt(),
                                    document.getString("date")!!
                                )
                                if (!docDao.isSync(document.getString("docName")!!)) {
                                    downloadFile(
                                        Variables.dirPath!!,
                                        document.getString("docName")!!,
                                        dModel
                                    )
                                }
                            }
                            //add category fields description data from cloud Firestore Database to Local Database
                            /*categoryFieldDao.searchFields(document.getString("docCategoryName")!!).forEach {
                                viewModel.updateData(
                                    CategoryFieldModel(
                                        document.getString("docCategoryName")!!,
                                        document.getString("fieldTitle")!!,
                                        document.getString("description")!!
                                    )
                                )
                            }*/
                        }
                    }
                    .addOnFailureListener {
                        showSnackBar("Sync Error! : ${it.message.toString()}")
                        showSyncFailedDialog()
                    }
            }

        } else {
            Toast.makeText(this, "File manage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(
        folderName: String,
        fileName: String,
        dModel: DocModel
    ) {

        val storageRef =
            FirebaseStorage.getInstance().reference.child(auth.currentUser!!.uid).child(fileName)
        val localFile = File(folderName, fileName)
        storageRef.getFile(localFile).addOnSuccessListener {
            if (it.task.isSuccessful) {
                //add (CategoryModel) data from cloud Firestore Database to Local Database
                GlobalScope.launch {
                    val cModel = categoryDao.findByCategoryName(dModel.docCategoryName)
                    Log.i("UPLOAD", "Counter : ${cModel.counter}")
                    cModel.counter = cModel.counter.plus(1)
                    viewModel.updateData(cModel)
                }
                //add (DocModel) data from cloud Firestore Database to Local Database
                viewModel.addData(dModel)
                Log.i("addOnSuccessListener", "addOnSuccessListener Success")

                //check cloud and local storage file list is same or note//
                checkCloudAndRoomDataBaseSize()
            }
        }.addOnFailureListener { exception ->
            showSyncFailedDialog()
        }.addOnProgressListener {
            //file total size//
            Log.i("RETRIEVE", "progress totalByteCount : ${it.totalByteCount}")
            //file current download size//
            Log.i("RETRIEVE", "progress bytesTransferred : ${it.bytesTransferred}")

            //update date and Size with UI//
            if (it.totalByteCount == it.bytesTransferred) {
                backupSizeCount = backupSizeCount.plus(it.bytesTransferred)
                setBackupDateAndTotalSize(backupSizeCount)
            }
        }
    }

    private fun setBackupDateAndTotalSize(bytesTransferred: Long) {
        runOnUiThread {
            lastBackupDateTV.text = backupDate

            //get 2 decimal place for EXAMPLE : 2.0236512 => 2.02//
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING

            val cnt_size: String
            val size_bit = df.format(bytesTransferred / 8).toDouble()
            val size_kb = df.format(bytesTransferred / 1024).toDouble()
            val size_mb = df.format(size_kb / 1024).toDouble()
            val size_gb = df.format(size_mb / 1024).toDouble()

            cnt_size = if (size_gb > 0.9) {
                "${size_gb}GB"
            } else if (size_mb > 0.9) {
                "${size_mb}MB"
            }else if (size_kb > 0.9){
                "${size_kb}KB"
            }else{
                "${size_bit}B"
            }

            backupSizeTV.text = cnt_size
            sp.edit().putString("DATE", backupDate).putString("SIZE", cnt_size).apply()
        }
    }

    private fun uploadData() {
        GlobalScope.launch {
            //check local database is empty or note//
            //otherwise cloud database list size is greater than local database list size//
            //then call download function//
            firestore.collection(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    GlobalScope.launch {
                        if (docDao.getAllDocument()
                                .isEmpty() || (documents.size() > docDao.getAllDocument().size)
                        ) {
                            downloadData()
                        }
                    }
                }
            docDao.getAllDocument().forEach { docModel ->
                if (!docModel.isSynced) {
                    GlobalScope.launch {
                        //put data into the Firebase Firestore//
                        val db =
                            Firebase.firestore.document("${auth.currentUser!!.uid}/${docModel.docName}")
                        val fieldsModel =
                            categoryFieldDao.findByCategoryFieldName(docModel.docCategoryName)
                        //showSnackBar("Syncing Document...")
                        val model = docDao.findByName(docModel.docName)
                        model.isSynced = true
                        viewModel.updateData(model)
                        storageRef = firebaseStorage.reference.child(auth.currentUser!!.uid)
                        storageRef.child(docModel.docName)
                            .putFile(Uri.fromFile(File(docModel.filePath)))
                            .addOnCompleteListener { result ->
                                if (result.isComplete) {
                                    firebaseStorage.reference.child(auth.currentUser!!.uid)
                                        .child(docModel.docName).downloadUrl.addOnCompleteListener { uri ->
                                            val hashMap = hashMapOf<String, Any>(
                                                "docName" to docModel.docName,
                                                "uriPath" to uri.result,
                                                "filePath" to docModel.filePath,
                                                "mimType" to docModel.mimType,
                                                "docCategoryName" to docModel.docCategoryName,
                                                "docCategoryIcon" to docModel.docCategoryIcon,
                                                "docCategoryIconBg" to docModel.docCategoryIconBg,
                                                "date" to docModel.date,
                                                "fieldTitle" to fieldsModel.fieldTitle,
                                                "description" to fieldsModel.description
                                            )
                                            db.set(hashMap)
                                        }
                                    //showSnackBar("Synced Successful")
                                    GlobalScope.launch {
                                        val docList = docDao.getDocListBySyncStatus(false)
                                        Log.i("UPLOAD", "Synced Status : ${docList.isEmpty()}")
                                        if (docList.isEmpty()) {
                                            downloadData()
                                        }
                                    }

                                    checkCloudAndRoomDataBaseSize()

                                } else {
                                    showSnackBar("Sync Failed")
                                }
                            }
                            .addOnFailureListener {
                                showSnackBar("Error!! : ${it.message}")
                            }.addOnProgressListener {
                                //update date and Size with UI//
                                Log.i("UPLOAD", "totalByteCount : ${it.totalByteCount}")
                                if (it.totalByteCount == it.bytesTransferred) {
                                    backupSizeCount = backupSizeCount.plus(it.bytesTransferred)
                                    setBackupDateAndTotalSize(backupSizeCount)
                                }
                            }
                    }
                }
            }
        }
    }

    private fun checkCloudAndRoomDataBaseSize() {
        Timer().schedule(600) {
            firestore.collection(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    GlobalScope.launch {
                        Log.i("UPLOAD", "file size : ${documents.size()}")
                        if (docDao.getAllDocument().size == documents.size()) {
                            syncIcon.clearAnimation()
                            showSyncSuccessDialog()
                        }
                    }
                }
        }
    }

    private fun showSyncSuccessDialog() {
        runOnUiThread {
            val successDialog = Dialog(this)
            successDialog.setContentView(R.layout.dialog_sync_success)
            successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            successDialog.findViewById<TextView>(R.id.okBtnTv)?.setOnClickListener {
                finish()
                successDialog.dismiss()
            }
            successDialog.show()
        }
    }

    private fun showSyncFailedDialog() {
        runOnUiThread {
            val errorDialog = Dialog(this)
            errorDialog.setContentView(R.layout.dialog_sync_error)
            errorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            errorDialog.findViewById<TextView>(R.id.cancelBtnTv).setOnClickListener { errorDialog.dismiss() }
            errorDialog.findViewById<TextView>(R.id.retryBtnTv).setOnClickListener {
                signInGoogle()
                errorDialog.dismiss()
            }
            errorDialog.show()
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

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            updateUiWithSignIn(true)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}