package com.ft.document_organizer.interFace

import com.ft.document_organizer.model.DocModel

interface LongItemClickListener {
    fun onLongClick(model: DocModel)
}