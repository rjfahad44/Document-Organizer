package com.ft.document_organizer.databaseDao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ft.document_organizer.model.DocModel
import java.net.IDN

@Dao
interface DocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(docModel: DocModel)

    @Update
    suspend fun update(docModel: DocModel)

    @Delete
    suspend fun delete(docModel: DocModel)

    @Query("DELETE FROM doc_table")
    fun deleteAllDoc()

    @Query("SELECT * FROM Doc_Table")
    fun getAllDoc(): LiveData<List<DocModel>>

    @Query("SELECT * FROM Doc_Table")
    fun getAllDocument(): List<DocModel>

    @Query("SELECT * FROM Doc_Table WHERE isSyncOrNot ==:status")
    fun getDocListBySyncStatus(status: Boolean): List<DocModel>

    @Query(
        "SELECT * FROM Doc_Table " +
                "ORDER BY CASE WHEN :isSort = 1 THEN DocName END ASC, " +
                "CASE WHEN :isSort = 2 THEN DocCategoryName END ASC, " +
                "CASE WHEN :isSort = 3 THEN DocSaveDate END ASC"
    )
    fun searchDocWithSortType(isSort: Int): LiveData<List<DocModel>>

    @Query(
        "SELECT * FROM Doc_Table WHERE DocName LIKE :query OR DocCategoryName LIKE :query " +
                "ORDER BY CASE WHEN :isSort = 1 THEN DocName END ASC, " +
                "CASE WHEN :isSort = 2 THEN DocCategoryName END ASC, " +
                "CASE WHEN :isSort = 3 THEN DocSaveDate END ASC"
    )
    fun searchDoc(query: String, isSort: Int): LiveData<List<DocModel>>

    @Query(
        "SELECT * FROM Doc_Table WHERE DocCategoryName LIKE :category AND DocName LIKE :query " +
                "ORDER BY CASE WHEN :isSort = 1 THEN DocName END ASC, " +
                "CASE WHEN :isSort = 2 THEN DocSaveDate END ASC"
    )
    fun searchDocByCategory(category: String, query: String, isSort: Int): LiveData<List<DocModel>>

    @Query(
        "SELECT * FROM Doc_Table WHERE DocCategoryName LIKE :category " +
                "ORDER BY CASE WHEN :isSort = 1 THEN DocName END ASC, " +
                "CASE WHEN :isSort = 2 THEN DocSaveDate END ASC"
    )
    fun searchDocByCategoryDefault(category: String, isSort: Int): LiveData<List<DocModel>>

    @Query("SELECT * FROM Doc_Table WHERE DocCategoryName LIKE :category")
    fun isFindOrNot(category: String): List<DocModel>

    @Query("SELECT * FROM Doc_Table WHERE DocName LIKE :docName")
    fun findByName(docName: String): DocModel

    @Query("SELECT * FROM Doc_Table WHERE id LIKE :id")
    fun findById(id: Int): DocModel

    @Query("SELECT isSyncOrNot FROM Doc_Table WHERE DocName LIKE :docName")
    fun isSync(docName: String): Boolean

    @Query("SELECT COUNT(*) FROM Doc_Table")
    fun docCounter(): Int
}