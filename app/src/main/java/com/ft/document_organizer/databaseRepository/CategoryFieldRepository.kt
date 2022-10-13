package com.ft.document_organizer.databaseRepository

import androidx.lifecycle.LiveData
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.model.CategoryFieldModel


class CategoryFieldRepository(private val categoryFieldDao: CategoryFieldDao) {
    val allCategoryFields : LiveData<List<CategoryFieldModel>> = categoryFieldDao.getAllFields()
    suspend fun insert(categoryFieldModel: CategoryFieldModel){
        categoryFieldDao.insert(categoryFieldModel)
    }

    suspend fun update(categoryFieldModel: CategoryFieldModel){
        categoryFieldDao.update(categoryFieldModel)
    }

    suspend fun delete(categoryFieldModel: CategoryFieldModel){
        categoryFieldDao.delete(categoryFieldModel)
    }
}