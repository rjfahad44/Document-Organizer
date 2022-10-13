package com.ft.document_organizer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Category_Table")
class CategoryModel(
    @ColumnInfo(name = "Counter")
    var counter: Int,
    @ColumnInfo(name = "CategoryIcon")
    var icon: Int,
    @ColumnInfo(name = "CategoryColorBg")
    var colorBg: Int,
    @ColumnInfo(name = "CategoryTitle")
    var title: String
    )
{
    @PrimaryKey(autoGenerate = true) var id = 0
}
