package com.example.todolist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {

    // ==========================================
    // QUẢN LÝ BẢNG: TASKS (CÔNG VIỆC)
    // ==========================================

    // [READ] Lấy tất cả công việc, tự động đẩy dữ liệu lên UI khi có thay đổi
    @Query("SELECT * FROM tasks ORDER BY isDone ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // [READ] Lấy chi tiết một công việc cụ thể (dùng khi mở màn hình chỉnh sửa)
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    // [CREATE / UPDATE] Thêm công việc mới hoặc Ghi đè nếu truyền vào ID đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    // [UPDATE] Cập nhật công việc
    @Update
    suspend fun updateTask(task: TaskEntity)

    // [DELETE] Xóa một công việc cụ thể bằng ID (khi bấm thùng rác)
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    // [DELETE] Xóa dọn dẹp tất cả công việc thuộc một danh mục (cascade delete)
    @Query("DELETE FROM tasks WHERE category = :categoryName")
    suspend fun deleteTasksByCategory(categoryName: String)


    // ==========================================
    // QUẢN LÝ BẢNG: CATEGORIES (DANH MỤC)
    // ==========================================

    // [READ] Lấy danh sách tất cả các danh mục
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // [CREATE] Thêm danh mục mới (Nếu trùng tên thì tự động bỏ qua để tránh lỗi)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    // [DELETE] Xóa một danh mục bằng tên của nó
    @Query("DELETE FROM categories WHERE name = :categoryName")
    suspend fun deleteCategory(categoryName: String)
}