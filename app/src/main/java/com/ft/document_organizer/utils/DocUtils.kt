package com.ft.document_organizer.utils

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.ft.document_organizer.R
import java.io.*
import java.math.RoundingMode
import java.nio.channels.FileChannel
import java.text.DecimalFormat

object DocUtils {
    fun createDirectory(context: Context): String {
        //directory path : /storage/emulated/0/Document-Organizer//
        //val cw = ContextWrapper(context)
        //val file = cw.getDir("Document-Organizer", Context.MODE_PRIVATE)
        //val file = File(Environment.getExternalStorageDirectory(), "Document-Organizer")
        val file = File(context.filesDir, "Document-Organizer")
        if (!file.exists()) {
            file.mkdirs()
        }
        file.canRead()
        return file.path
    }

    fun copyFileToInternalStorage(context: Context, uri: Uri, dst: File): String? {
        try {
            /*TODO : applicationContext.contentResolver.openInputStream(uri)
            * This line of code help us to save documents file into the internal or external storage without "MANAGE_EXTERNAL_STORAGE" permission
            */
            context.contentResolver.openInputStream(uri).use { inputStream ->
                try {
                    FileOutputStream(dst).use { outputStream ->
                        var read = 0
                        val bufferSize = 1024
                        val buffers = ByteArray(bufferSize)
                        while (inputStream?.read(buffers).also { read = it!! } != -1) {
                            outputStream.write(buffers, 0, read)
                        }
                    }
                    return dst.path
                } catch (e: IOException) {
                    Log.i("PICK_DOC", "Exception : ${e.message}")
                    return e.message
                }
            }
        } catch (e: IOException) {
            Log.i("PICK_DOC", "Exception : ${e.message}")
            return e.message
        }
    }


    fun getColorCode(context: Context, className: String) {
        val fields = Class.forName("$className.R\$color").declaredFields
        for (field in fields) {
            val colorName = field.name
            val colorId = field.getInt(null)
            val color = ContextCompat.getColor(context, colorId)
            Log.i("COLOR_LIST", "Name : $colorName => Color Id : $colorId => Color : $color")
        }
    }


    fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun saveFile(sourceUri: Uri, destination: File): File {
        val dst: File? = null
        try {
            val source = sourceUri.path?.let { File(it) }
            val src: FileChannel = FileInputStream(source).channel
            val dst: FileChannel = FileOutputStream(destination).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return dst!!
    }

    fun getFileSize(bytes: Long): String {
        //get 2 decimal place for EXAMPLE : 2.0236512 => 2.02//
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        val cnt_size: String
        val size_bit = df.format(bytes / 8).toDouble()
        val size_kb = df.format(bytes / 1024).toDouble()
        val size_mb = df.format(size_kb / 1024).toDouble()
        val size_gb = df.format(size_mb / 1024).toDouble()

        cnt_size = if (size_gb > 0.9) {
            "${size_gb}GB"
        } else if (size_mb > 0.9) {
            "${size_mb}MB"
        } else if (size_kb > 0.9){
            "${size_kb}KB"
        }else{
            "${size_bit}B"
        }
        return cnt_size
    }

    fun rotateView(view: View?, startAngle: Float, endAngle: Float, repeatCount: Int) {
        val rotate = ObjectAnimator.ofFloat(view, "rotation", startAngle, endAngle)
        //-1 means infinite//
        rotate.repeatCount = repeatCount
        rotate.duration = 400
        rotate.start()
    }

    //load profile image//
    fun loadImage(context: Context, view: ImageView, uri: Uri) {
        Glide
            .with(context)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.profile)
            .into(view)
    }

    //get document file path from uri path//
    /*----------------------------------------------------------------------------------------------*/
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else { // non-primary volumes e.g sd card
                    var filePath = "non"
                    //getExternalMediaDirs() added in API 21
                    val extenal = context.externalMediaDirs
                    for (f in extenal) {
                        filePath = f.absolutePath
                        if (filePath.contains(type)) {
                            val endIndex = filePath.indexOf("Android")
                            filePath = filePath.substring(0, endIndex) + split[1]
                        }
                    }
                    filePath
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(uri, java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: java.lang.Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
/*---------------------------------------------------------------------------------------------------------------*/
}