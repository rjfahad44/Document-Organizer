package com.ft.document_organizer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ft.document_organizer.databaseDao.CategoryDao
import com.ft.document_organizer.databaseDao.CategoryFieldDao
import com.ft.document_organizer.databaseDao.DocDao
import com.ft.document_organizer.model.CategoryFieldModel
import com.ft.document_organizer.model.CategoryModel
import com.ft.document_organizer.model.DocModel

@Database(entities = [DocModel::class, CategoryModel::class, CategoryFieldModel::class], version = 1, exportSchema = false)
abstract class DocDatabase : RoomDatabase() {
    abstract fun getDao() : DocDao
    abstract fun getCategoryDao() : CategoryDao
    abstract fun getCategoryFieldDao() : CategoryFieldDao

    companion object{
        @Volatile
        private var INSTANCE : DocDatabase? = null
        fun getDatabase(context: Context) : DocDatabase {
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocDatabase::class.java,
                    "Document_DB"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}