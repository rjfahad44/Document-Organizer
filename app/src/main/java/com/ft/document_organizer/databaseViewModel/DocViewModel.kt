package com.ft.document_organizer.databaseViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ft.document_organizer.database.DocDatabase
import com.ft.document_organizer.databaseRepository.CategoryFieldRepository
import com.ft.document_organizer.databaseRepository.CategoryRepository
import com.ft.document_organizer.databaseRepository.DocRepository
import com.ft.document_organizer.model.CategoryFieldModel
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.model.DocModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DocViewModel(application: Application) : AndroidViewModel(application) {
    val allDoc: LiveData<List<DocModel>>
    private val docRepository: DocRepository

    val allCategory: LiveData<List<CategoryModel>>
    private val categoryRepository: CategoryRepository

    val allFields: LiveData<List<CategoryFieldModel>>
    private val categoryFieldRepository: CategoryFieldRepository

    init {
        val dao = DocDatabase.getDatabase(application).getDao()
        docRepository = DocRepository(dao)
        allDoc = docRepository.allData

        val categoryDao = DocDatabase.getDatabase(application).getCategoryDao()
        categoryRepository = CategoryRepository(categoryDao)
        allCategory = categoryRepository.allCategory

        val categoryFieldDao = DocDatabase.getDatabase(application).getCategoryFieldDao()
        categoryFieldRepository = CategoryFieldRepository(categoryFieldDao)
        allFields = categoryFieldRepository.allCategoryFields
    }

    /*Document CRUD View Model*/
    fun addData(docModel: DocModel) = viewModelScope.launch(Dispatchers.IO) {
        docRepository.insert(docModel)
    }

    fun updateData(docModel: DocModel) = viewModelScope.launch(Dispatchers.IO) {
        docRepository.update(docModel)
    }

    fun deleteData(docModel: DocModel) = viewModelScope.launch(Dispatchers.IO) {
        docRepository.delete(docModel)
    }


    /*Category CRUD View Model*/
    fun addData(categoryModel: CategoryModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.insert(categoryModel)
    }

    fun updateData(categoryModel: CategoryModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.update(categoryModel)
    }

    fun deleteData(categoryModel: CategoryModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.delete(categoryModel)
    }
    fun findByName(name: String) = viewModelScope.launch(Dispatchers.IO)  {
        categoryRepository.findByName(name)
    }

    /*Category Field CRUD View Model*/
    fun addData(categoryFieldModel: CategoryFieldModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryFieldRepository.insert(categoryFieldModel)
    }

    fun updateData(categoryFieldModel: CategoryFieldModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryFieldRepository.update(categoryFieldModel)
    }

    fun deleteData(categoryFieldModel: CategoryFieldModel) = viewModelScope.launch(Dispatchers.IO) {
        categoryFieldRepository.delete(categoryFieldModel)
    }
}