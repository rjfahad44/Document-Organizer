package com.ft.document_organizer.interFace

import com.ft.document_organizer.model.CategoryFieldModel

interface FieldDeleteListener {
    fun onDeleteItem(model: CategoryFieldModel)
}