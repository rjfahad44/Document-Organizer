package com.ft.document_organizer.interFace

import android.view.View
import com.ft.document_organizer.model.CategoryModel

interface CategoryMenuListener {
    fun onMenuItem(model: CategoryModel, view: View)
}