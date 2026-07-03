package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.AddScreenInfo
import com.example.todolist.AddTaskScreen
import com.example.todolist.R
import com.example.todolist.TopBar
import com.example.todolist.ui.AddScreenViewModel
import com.example.todolist.ui.theme.ToDoListTheme

@Composable
fun AddTaskListScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    addListViewModel: AddScreenViewModel // CẬP NHẬT 1: Đã xoá "= viewModel()"
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .background(colorScheme.primaryContainer)
            .padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    containerColor = Color.Transparent, // Khong cho Scaffold che mat nen vang
                    topBar = {
                        //Top Bar
                        TopBar(
                            title = R.string.addtask_title, // Bạn có thể cân nhắc đổi thành R.string.addlist_title nếu có
                            onBack = onBack,
                            onCreate = onCreate
                        )
                    }
                ) { innerPadding ->
                    AddScreenInfo(
                        name = R.string.addlist_title,
                        value = addListViewModel.title,
                        onValueChange = {addListViewModel.updateTitle(it)},
                        modifier = Modifier.padding(innerPadding),
                        isMultiline = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManHinhThemDSPreview(){
    ToDoListTheme (dynamicColor = false) {
        // CẬP NHẬT 2: Tạm ẩn Preview để tránh lỗi Database Context
        /* AddTaskListScreen(
            onCreate = {},
            onBack = {},
            // addListViewModel = ...
        ) */
    }
}