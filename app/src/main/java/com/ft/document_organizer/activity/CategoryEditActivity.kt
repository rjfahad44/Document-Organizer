package com.ft.document_organizer.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ft.document_organizer.R
import com.ft.document_organizer.adapder.CategoryFieldRvAdapter
import com.ft.document_organizer.adapder.IconChooserAdapter
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.databaseViewModel.DocViewModel
import com.ft.document_organizer.interFace.FieldDeleteListener
import com.ft.document_organizer.interFace.IconClickListener
import com.ft.document_organizer.model.CategoryFieldModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryEditActivity : AppCompatActivity(), FieldDeleteListener, IconClickListener {

    //View Model Provider//
    private lateinit var viewModel: DocViewModel
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryFieldDao: CategoryFieldDao

    private lateinit var updateBtnTv: TextView
    private lateinit var categoryNameEt: EditText
    private lateinit var cvSelectColor: ImageView
    private lateinit var folderIcon: ImageView
    private lateinit var cvIconContainer: CardView
    private lateinit var lL: LinearLayout
    private lateinit var addFieldImgV: ImageView
    private lateinit var addFieldET: EditText
    private lateinit var cancelBtnTv: TextView
    private lateinit var saveBtnTv: TextView

    //Category List Rv
    private lateinit var categoryRv: RecyclerView
    private lateinit var categoricalAdapter: CategoryFieldRvAdapter

    //Category Icon Rv//
    private lateinit var iconChooserAdapter: IconChooserAdapter
    private lateinit var iconList: ArrayList<Int>

    private lateinit var dialogIconChooser: Dialog
    private lateinit var dialogColorChooser: Dialog
    private lateinit var dialogDelete: Dialog

    //default category icon and icon background color//
    var icon: Int = R.drawable.i0
    var iconBgColor: Int = -1905153

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        initializeWidget()
    }

    private fun initializeWidget() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DocViewModel::class.java]

        categoryDao = DocDatabase.getDatabase(this).getCategoryDao()
        categoryFieldDao = DocDatabase.getDatabase(this).getCategoryFieldDao()

        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }

        updateBtnTv = findViewById(R.id.updateBtnTv)
        categoryNameEt = findViewById(R.id.categoryNameEt)

        cvSelectColor = findViewById(R.id.colorChooserImgV)
        intent.extras?.getInt("colorBg")?.let { cvSelectColor.setBackgroundColor(it) }

        cvSelectColor.setOnClickListener { chooseColor() }

        folderIcon = findViewById(R.id.folderIcon)
        intent.extras?.getInt("icon")?.let {
            icon = it
            folderIcon.setImageResource(it)
        }

        cvIconContainer = findViewById(R.id.cvIconContainer)
        cvIconContainer.setOnClickListener { chooseCategoryIcon() }

        lL = findViewById(R.id.lL)
        intent.extras?.getInt("colorBg")?.let { lL.setBackgroundColor(it) }

        addFieldImgV = findViewById(R.id.addFieldImgV)
        addFieldImgV.setOnClickListener { addField() }
        addFieldET = findViewById(R.id.addFieldET)
        cancelBtnTv = findViewById(R.id.cancelBtnTv)
        saveBtnTv = findViewById(R.id.saveBtnTv)
        updateCategory()

        categoryRv = findViewById(R.id.rvFieldCategory)
        categoryRv.layoutManager = LinearLayoutManager(this)
        categoricalAdapter = CategoryFieldRvAdapter(this, this)
        categoryRv.adapter = categoricalAdapter
        intent.extras?.getString("title")?.let {
            categoryNameEt.isFocusable = true
            categoryNameEt.setText(it)
            loadData(it)
        }
    }

    private fun addField() {
        addFieldET.visibility = VISIBLE
        cancelBtnTv.visibility = VISIBLE
        saveBtnTv.visibility = VISIBLE

        addFieldET.doOnTextChanged { text, start, before, count ->
            if (count>0){
                saveBtnTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
                saveBtnTv.isEnabled = true
            }else{
                saveBtnTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
                saveBtnTv.isEnabled = false
            }
        }

        saveBtnTv.setOnClickListener {
            if (categoryNameEt.text.toString().isNotEmpty()){
                GlobalScope.launch {
                    categoryFieldDao.searchFields("empty").forEach { model->
                        model.categoryName = categoryNameEt.text.toString()
                        viewModel.updateData(model)
                    }
                }
                viewModel.addData(CategoryFieldModel(categoryNameEt.text.toString(), addFieldET.text.toString(), ""))
                loadData(categoryNameEt.text.toString())

                addFieldET.setText("")
                saveBtnTv.isEnabled = false
            }else{
                Toast.makeText(this@CategoryEditActivity, "Category Name is Empty!", Toast.LENGTH_SHORT).show()
            }
        }

        cancelBtnTv.setOnClickListener {
            addFieldET.setText("")
            addFieldET.visibility = GONE
            cancelBtnTv.visibility = GONE
            saveBtnTv.visibility = GONE
        }
    }

    private fun chooseCategoryIcon() {
        dialogIconChooser = Dialog(this)
        dialogIconChooser.setContentView(R.layout.dialog_icon_chooser)
        dialogIconChooser.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //close dialog by button click//
        dialogIconChooser.findViewById<ImageView>(R.id.closeImgV).setOnClickListener { dialogIconChooser.dismiss() }

        //show icon into the icon chooser dialogIconChooser//
        iconList = ArrayList()
        val iconChooseRv = dialogIconChooser.findViewById<RecyclerView>(R.id.iconChooserRvId)
        iconChooseRv.layoutManager = GridLayoutManager(this, 6)
        iconChooserAdapter = IconChooserAdapter(this, this)
        iconChooseRv.adapter = iconChooserAdapter

        iconList.add(R.drawable.i0)
        iconList.add(R.drawable.i1)
        iconList.add(R.drawable.i2)
        iconList.add(R.drawable.i3)
        iconList.add(R.drawable.i4)
        iconList.add(R.drawable.i5)
        iconList.add(R.drawable.i6)
        iconList.add(R.drawable.i7)
        iconList.add(R.drawable.i8)
        iconList.add(R.drawable.i9)
        iconList.add(R.drawable.i10)
        iconList.add(R.drawable.i11)
        iconList.add(R.drawable.i12)
        iconList.add(R.drawable.i13)
        iconList.add(R.drawable.i14)
        iconList.add(R.drawable.i15)
        iconList.add(R.drawable.i16)
        iconList.add(R.drawable.i17)
        iconList.add(R.drawable.i18)
        iconList.add(R.drawable.i19)
        iconList.add(R.drawable.i20)
        iconList.add(R.drawable.i21)
        iconList.add(R.drawable.i22)
        iconList.add(R.drawable.i23)
        iconList.add(R.drawable.i24)
        iconList.add(R.drawable.i25)
        iconList.add(R.drawable.i26)
        iconList.add(R.drawable.i27)
        iconList.add(R.drawable.i28)
        iconList.add(R.drawable.i29)
        iconList.add(R.drawable.i30)
        iconList.add(R.drawable.i31)
        iconList.add(R.drawable.i32)
        iconList.add(R.drawable.i33)
        iconList.add(R.drawable.i34)
        iconList.add(R.drawable.i35)
        iconList.add(R.drawable.i36)
        iconList.add(R.drawable.i37)
        iconList.add(R.drawable.i38)
        iconList.add(R.drawable.i39)
        iconList.add(R.drawable.i40)
        iconList.add(R.drawable.i41)
        iconList.add(R.drawable.i42)
        iconList.add(R.drawable.i43)
        iconList.add(R.drawable.i44)
        iconList.add(R.drawable.i45)
        iconList.add(R.drawable.i46)
        iconList.add(R.drawable.i47)
        iconList.add(R.drawable.i48)
        iconList.add(R.drawable.i49)
        iconList.add(R.drawable.i50)
        iconList.add(R.drawable.i51)
        iconList.add(R.drawable.i52)
        iconList.add(R.drawable.i53)
        iconList.add(R.drawable.i54)
        iconList.add(R.drawable.i55)
        iconList.add(R.drawable.i56)
        iconList.add(R.drawable.i57)
        iconList.add(R.drawable.i58)
        iconList.add(R.drawable.i59)
        iconList.add(R.drawable.i60)
        iconList.add(R.drawable.i61)
        iconList.add(R.drawable.i62)
        iconList.add(R.drawable.i63)
        iconList.add(R.drawable.i64)
        iconList.add(R.drawable.i65)
        iconList.add(R.drawable.i66)
        iconList.add(R.drawable.i67)
        iconList.add(R.drawable.i68)
        iconList.add(R.drawable.i69)
        iconList.add(R.drawable.i70)
        iconList.add(R.drawable.i71)
        iconList.add(R.drawable.i72)
        iconList.add(R.drawable.i73)
        iconList.add(R.drawable.i74)
        iconList.add(R.drawable.i75)
        iconList.add(R.drawable.i76)
        iconList.add(R.drawable.i77)
        iconList.add(R.drawable.i78)
        iconList.add(R.drawable.i79)
        iconList.add(R.drawable.i80)
        iconList.add(R.drawable.i81)
        iconList.add(R.drawable.i82)
        iconList.add(R.drawable.i83)

        iconChooserAdapter.update(iconList)

        dialogIconChooser.show()
    }

    private fun loadData(query: String) {
        categoryFieldDao.getCategoryFields(query).observe(this){
            categoricalAdapter.update(it as ArrayList<CategoryFieldModel>)
        }
    }

    private fun chooseColor() {
        dialogColorChooser = Dialog(this)
        dialogColorChooser.setContentView(R.layout.dialog_color_chooser)
        dialogColorChooser.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val colorListInt = arrayOf(
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
            -5325825
        )

        val c0 = dialogColorChooser.findViewById<ImageView>(R.id.c0)
        val c1 = dialogColorChooser.findViewById<ImageView>(R.id.c1)
        val c2 = dialogColorChooser.findViewById<ImageView>(R.id.c2)
        val c3 = dialogColorChooser.findViewById<ImageView>(R.id.c3)
        val c4 = dialogColorChooser.findViewById<ImageView>(R.id.c4)
        val c5 = dialogColorChooser.findViewById<ImageView>(R.id.c5)
        val c6 = dialogColorChooser.findViewById<ImageView>(R.id.c6)
        val c7 = dialogColorChooser.findViewById<ImageView>(R.id.c7)
        val c8 = dialogColorChooser.findViewById<ImageView>(R.id.c8)
        val c9 = dialogColorChooser.findViewById<ImageView>(R.id.c9)
        val c10 = dialogColorChooser.findViewById<ImageView>(R.id.c10)
        val c11 = dialogColorChooser.findViewById<ImageView>(R.id.c11)

        c0.setOnClickListener {
            iconBgColor = colorListInt[0]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c1.setOnClickListener {
            iconBgColor = colorListInt[1]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c2.setOnClickListener {
            iconBgColor = colorListInt[2]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c3.setOnClickListener {
            iconBgColor = colorListInt[3]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c4.setOnClickListener {
            iconBgColor = colorListInt[4]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c5.setOnClickListener {
            iconBgColor = colorListInt[5]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c6.setOnClickListener {
            iconBgColor = colorListInt[6]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c7.setOnClickListener {
            iconBgColor = colorListInt[7]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c8.setOnClickListener {
            iconBgColor = colorListInt[8]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c9.setOnClickListener {
            iconBgColor = colorListInt[9]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c10.setOnClickListener {
            iconBgColor = colorListInt[10]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }
        c11.setOnClickListener {
            iconBgColor = colorListInt[11]
            cvSelectColor.setBackgroundColor(iconBgColor)
            lL.setBackgroundColor(iconBgColor)
            dialogColorChooser.dismiss()
        }

        dialogColorChooser.findViewById<ImageView>(R.id.closeImgV).setOnClickListener { dialogColorChooser.dismiss() }

        dialogColorChooser.show()
    }

    private fun updateCategory() {
        categoryNameEt.doOnTextChanged { text, start, before, count ->
            if (count>0){
                updateBtnTv.isEnabled = true
                updateBtnTv.setTextColor(ContextCompat.getColor(this, R.color.blue))
                updateBtnTv.compoundDrawables.getOrNull(2)?.setTint(ContextCompat.getColor(this, R.color.blue))
                updateBtnTv.setOnClickListener {
                    if (categoryNameEt.text.toString().isNotEmpty()) {
                        GlobalScope.launch {
                            intent.extras?.getString("title").let {
                                val categoryModel = categoryDao.findByCategoryName(it!!)
                                categoryModel.icon = icon
                                categoryModel.colorBg = iconBgColor
                                categoryModel.title = categoryNameEt.text.toString()
                                viewModel.updateData(categoryModel)

                                categoryFieldDao.searchFields(it).forEach { model->
                                    model.categoryName = categoryNameEt.text.toString()
                                    viewModel.updateData(model)
                                }
                                runOnUiThread{
                                    Toast.makeText(applicationContext, "Update", Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Category name is empty!", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                updateBtnTv.isEnabled = false
                updateBtnTv.setTextColor(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
                updateBtnTv.compoundDrawables.getOrNull(2)?.setTint(ContextCompat.getColor(this, R.color.toolbar_btn_icon_bg))
            }
        }
    }

    override fun onDeleteItem(model: CategoryFieldModel) {
        dialogDelete = Dialog(this)
        dialogDelete.setContentView(R.layout.dialog_delete_doc)
        dialogDelete.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogDelete.findViewById<TextView>(R.id.cancelBtnTv).setOnClickListener { dialogDelete.dismiss() }
        dialogDelete.findViewById<TextView>(R.id.deleteBtnTv).setOnClickListener {
            Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show()
            viewModel.deleteData(model)
            dialogDelete.dismiss()
        }
        dialogDelete.show()
    }

    override fun iconClick(drawableId: Int) {
        if (dialogIconChooser.isShowing){
            dialogIconChooser.dismiss()
        }
        icon = drawableId
        folderIcon.setImageResource(drawableId)
    }
}