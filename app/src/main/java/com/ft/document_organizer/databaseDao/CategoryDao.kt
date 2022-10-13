package com.ft.document_organizer.databaseDao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ft.document_organizer.model.CategoryModel

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryModel: CategoryModel)

    @Update
    suspend fun update(categoryModel: CategoryModel)

    @Delete
    suspend fun delete(categoryModel: CategoryModel)

    @Query("SELECT * FROM Category_Table ORDER BY CategoryTitle ASC")
    fun getAllCategory() : LiveData<List<CategoryModel>>

    @Query("SELECT * FROM Category_Table WHERE CategoryTitle LIKE :name ORDER BY CategoryTitle ASC")
    fun findByCategoryName(name: String): CategoryModel

    @Query("SELECT * FROM Category_Table")
    fun allCategory() : List<CategoryModel>

    @Query("SELECT * FROM Category_Table WHERE CategoryTitle LIKE :query ORDER BY CategoryTitle ASC")
    fun searchCategory(query: String): LiveData<List<CategoryModel>>
}