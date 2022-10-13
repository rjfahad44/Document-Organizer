package com.ft.document_organizer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Category_Field_Table")
class CategoryFieldModel(
    @ColumnInfo(name = "CategoryName")
    var categoryName: String,
    @ColumnInfo(name = "FieldTitle")
    val fieldTitle: String,
    @ColumnInfo(name = "Description")
    var description: String)
{
    @PrimaryKey(autoGenerate = true) var id = 0
}
