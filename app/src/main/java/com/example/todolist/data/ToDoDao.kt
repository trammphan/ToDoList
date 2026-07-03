package com.example.todolist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    // --- Bảng Tasks ---
    @Query("SELECT * FROM tasks ORDER BY isDone ASC")
    fun getAllTasks(): Flow<List<TaskEntity>> // Dùng Flow để tự động update UI

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    // --- Bảng Categories ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    // Thêm hàm này vào trong interface ToDoDao
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    // --- THÊM VÀO PHẦN BẢNG CATEGORIES ---
    @Query("DELETE FROM categories WHERE name = :categoryName")
    suspend fun deleteCategory(categoryName: String)

    // Lệnh này dùng để xóa dọn dẹp các task thuộc về danh mục vừa bị xóa
    @Query("DELETE FROM tasks WHERE category = :categoryName")
    suspend fun deleteTasksByCategory(categoryName: String)
}