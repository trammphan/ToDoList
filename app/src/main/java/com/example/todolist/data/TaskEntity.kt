package com.example.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Tương ứng với selectedList trong AddTaskUiState
    val priority: String?, // Tương ứng với PriorityLevel.label
    val deadline: String,
    val isDone: Boolean = false
)