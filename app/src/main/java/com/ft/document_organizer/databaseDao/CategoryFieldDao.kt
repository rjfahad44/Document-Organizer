package com.ft.document_organizer.databaseDao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ft.document_organizer.model.CategoryFieldModel
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.model.DocModel

@Dao
interface CategoryFieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryFieldModel: CategoryFieldModel)
    @Update
    suspend fun update(categoryFieldModel: CategoryFieldModel)
    @Delete
    suspend fun delete(categoryFieldModel: CategoryFieldModel)

    @Query("SELECT * FROM Category_Field_Table ORDER BY CategoryName ASC")
    fun getAllFields() : LiveData<List<CategoryFieldModel>>

    @Query("SELECT * FROM Category_Field_Table WHERE FieldTitle LIKE :title OR CategoryName LIKE :title")
    fun findByCategoryFieldName(title: String): CategoryFieldModel

    @Query("SELECT * FROM Category_Field_Table WHERE FieldTitle LIKE :query OR CategoryName LIKE :query ORDER BY FieldTitle ASC")
    fun getCategoryFields(query: String): LiveData<List<CategoryFieldModel>>

    @Query("SELECT * FROM Category_Field_Table WHERE CategoryName LIKE :query ORDER BY FieldTitle ASC")
    fun searchFields(query: String): List<CategoryFieldModel>
}