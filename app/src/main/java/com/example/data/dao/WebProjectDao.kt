package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WebProject
import kotlinx.coroutines.flow.Flow

@Dao
interface WebProjectDao {
    @Query("SELECT * FROM web_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<WebProject>>

    @Query("SELECT * FROM web_projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<WebProject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: WebProject): Long

    @Update
    suspend fun updateProject(project: WebProject)

    @Delete
    suspend fun deleteProject(project: WebProject)

    @Query("DELETE FROM web_projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM web_projects")
    suspend fun getProjectCount(): Int
}
