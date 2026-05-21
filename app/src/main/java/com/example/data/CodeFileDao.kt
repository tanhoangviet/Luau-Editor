package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeFileDao {
    @Query("SELECT * FROM code_files ORDER BY lastModified ASC")
    fun getAllFiles(): Flow<List<CodeFile>>

    @Query("SELECT * FROM code_files WHERE id = :id")
    suspend fun getFileById(id: Int): CodeFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CodeFile): Long

    @Update
    suspend fun updateFile(file: CodeFile)

    @Query("DELETE FROM code_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)

    @Query("SELECT COUNT(*) FROM code_files")
    suspend fun getCount(): Int
}
