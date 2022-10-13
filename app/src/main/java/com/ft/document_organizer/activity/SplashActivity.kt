package com.ft.document_organizer.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.ft.document_organizer.R
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.model.CategoryFieldModel
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.utils.DocUtils

class SplashActivity : AppCompatActivity() {

    //View Holder Initialize//
    private lateinit var viewModel: DocViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*get all color id with color code from the /res/values/colors*/
        //DocUtils.getColorCode(this, packageName)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DocViewModel::class.java]
        loadDefaultCategory()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }, 2000)
    }

    private fun loadDefaultCategory() {
        //Default Category Name//
        val sp = getSharedPreferences("DEFAULT_CATEGORY", MODE_PRIVATE)
        if (!sp.getBoolean("CATEGORY", false)) {
            val editor = sp.edit()
            editor.putBoolean("CATEGORY", true)
            editor.apply()
            setDefaultCategory()
        }
    }

    private fun setDefaultCategory() {
        val defaultCategoryList = arrayOf(
            "Invoice",
            "Personal",
            "Book",
            "Bills",
            "Bank",
            "Business Card",
            "Contact",
            "Medical",
            "Tickets",
            "Water",
            "Electricity",
            "Gaz",
            "School",
            "Product"
        )
        val colorArrayList = arrayOf(
            -1905153,
            -68928,
            -4134168,
            -10508,
            -11823,
            -7236,
            -739329,
            -18481,
            -4653064,
            -4653126,
            -3032837,
            -5325825,
            -7085664,
            -4286779
        )
        val iconArrayList = arrayOf(
            R.drawable.i0,
            R.drawable.i1,
            R.drawable.i2,
            R.drawable.i3,
            R.drawable.i4,
            R.drawable.i5,
            R.drawable.i6,
            R.drawable.i7,
            R.drawable.i8,
            R.drawable.i9,
            R.drawable.i10,
            R.drawable.i11,
            R.drawable.i12,
            R.drawable.i13
        )
        for (i in defaultCategoryList.indices) {
            setDefaultCategoryField(defaultCategoryList[i])
            viewModel.addData(
                CategoryModel(
                    0,
                    iconArrayList[i],
                    colorArrayList[i],
                    defaultCategoryList[i]
                )
            )
        }
    }

    private fun setDefaultCategoryField(categoryName: String) {
        when (categoryName) {
            "Invoice", "Water", "Electricity", "Gaz", "Bank", "Bills", "Book" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Name", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Amount", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Date", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Personal", "School" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Business Card" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Company Name", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Profession", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Email", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Tel", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Contact" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Name", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Date", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Medical" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Medications", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Dose", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Doctor", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Price", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Name", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Date", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Tickets" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Name", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Amount", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "From", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "To", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Date", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
            "Product" -> {
                viewModel.addData(CategoryFieldModel(categoryName, "Price",""))
                viewModel.addData(CategoryFieldModel(categoryName, "Brand", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Model", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Seller", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Condition", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "ID", ""))
                viewModel.addData(CategoryFieldModel(categoryName, "Description", ""))
            }
        }
    }
}