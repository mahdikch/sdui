package com.yandex.divkit.demo.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "PhPlus", indices = [Index(value =   ["key"], unique = true)])
data class PhPlusDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    @ColumnInfo(name = "key")
    val key: String?,
    val value: String,
)
