package com.ft.document_organizer.databaseRepository

import androidx.lifecycle.LiveData
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.model.CategoryModel


class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategory : LiveData<List<CategoryModel>> = categoryDao.getAllCategory()
    suspend fun insert(categoryModel: CategoryModel){
        categoryDao.insert(categoryModel)
    }

    suspend fun update(categoryModel: CategoryModel){
        categoryDao.update(categoryModel)
    }

    suspend fun delete(categoryModel: CategoryModel){
        categoryDao.delete(categoryModel)
    }

    fun findByName(name : String){
        categoryDao.findByCategoryName(name)
    }
}