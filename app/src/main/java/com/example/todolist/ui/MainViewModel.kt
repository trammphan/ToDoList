package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.CategoryEntity
import com.example.todolist.data.TaskEntity
import com.example.todolist.data.ToDoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val dao: ToDoDao) : ViewModel() {

    // Lấy toàn bộ Task từ DB, tự động cập nhật khi DB thay đổi
    val allTasks: StateFlow<List<TaskEntity>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Lấy toàn bộ Category từ DB
    val categories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Hàm cập nhật trạng thái Checkbox (isDone)
    fun updateTaskStatus(task: TaskEntity, isDone: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateTask(task.copy(isDone = isDone))
        }
    }

    // Hàm thêm danh mục mới
    fun addCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCategory(CategoryEntity(name))
        }
    }

    // Hàm xóa danh mục và toàn bộ công việc bên trong
    fun deleteCategory(categoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Xóa toàn bộ công việc nằm trong danh mục này trước
            dao.deleteTasksByCategory(categoryName)

            // Sau đó xóa tên danh mục
            dao.deleteCategory(categoryName)
        }
    }
}