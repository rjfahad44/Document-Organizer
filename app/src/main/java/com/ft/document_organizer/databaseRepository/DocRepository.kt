package com.ft.document_organizer.databaseRepository

import androidx.lifecycle.LiveData
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.model.DocModel


class DocRepository(private val docDao: DocDao) {
    val allData: LiveData<List<DocModel>> = docDao.getAllDoc()

    suspend fun insert(docModel: DocModel) {
        docDao.insert(docModel)
    }

    suspend fun update(docModel: DocModel) {
        docDao.update(docModel)
    }

    suspend fun delete(docModel: DocModel) {
        docDao.delete(docModel)
    }
}