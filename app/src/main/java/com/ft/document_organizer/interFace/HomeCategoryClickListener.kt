package com.ft.document_organizer.interFace

import com.ft.document_organizer.model.CategoryModel

interface HomeCategoryClickListener {
    fun onCategoryClick(categoryModel: CategoryModel)
}