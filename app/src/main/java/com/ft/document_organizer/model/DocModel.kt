package com.ft.document_organizer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Doc_Table")
class DocModel(
    @ColumnInfo(name = "isSyncOrNot")
    var isSynced: Boolean,
    @ColumnInfo(name = "MimType")
    var mimType: String,
    @ColumnInfo(name = "DocName")
    var docName: String,
    @ColumnInfo(name = "DocUri")
    var docUri: String,
    @ColumnInfo(name = "FilePath")
    var filePath: String,
    @ColumnInfo(name = "DocCategoryName")
    var docCategoryName: String,
    @ColumnInfo(name = "DocCategoryIcon")
    var docCategoryIcon: Int,
    @ColumnInfo(name = "DocCategoryIconBg")
    var docCategoryIconBg: Int,
    @ColumnInfo(name = "DocSaveDate")
    var date: String)
{
    @PrimaryKey(autoGenerate = true) var id = 0
}