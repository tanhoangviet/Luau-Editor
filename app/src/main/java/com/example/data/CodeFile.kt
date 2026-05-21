package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_files")
data class CodeFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val content: String,
    val language: String,
    val isDefault: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
