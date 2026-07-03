package com.example.todolist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.ToDoDatabase
import com.example.todolist.ui.AddScreenViewModel
import com.example.todolist.ui.MainViewModel
enum class ToDoListScreen {
    Start,
    Detail,
    AddTask,
    AddList
}

@Composable
fun ToDoListApp(
    navController: NavHostController = rememberNavController()
) {
    // 1. Khởi tạo Database và DAO (Context lấy từ giao diện Compose)
    val context = LocalContext.current
    val database = ToDoDatabase.getDatabase(context)
    val dao = database.toDoDao()

    // 2. Tạo Factory cho MainViewModel (dành cho màn hình chính)
    val mainViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(dao) as T
        }
    }

    // 3. Tạo Factory cho AddScreenViewModel (dành cho các màn hình thêm/sửa chi tiết)
    val addViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddScreenViewModel(dao) as T
        }
    }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ToDoListScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ==========================================
            // MÀN HÌNH CHÍNH (Hiển thị danh sách Task)
            // ==========================================
            composable(route = ToDoListScreen.Start.name) {
                // Ép kiểu tạo MainViewModel thông qua Factory
                val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)

                Manchinh(
                    viewModel = mainViewModel,
                    onAddButtonClicked = {
                        navController.navigate(route = ToDoListScreen.AddList.name)
                    },
                    onDetailButonCliked = { task ->
                        // Truyền ID của task qua chuỗi route
                        navController.navigate(route = "${ToDoListScreen.Detail.name}/${task.id}")
                    },
                    onAddTaskButtonClicked = {
                        navController.navigate(route = ToDoListScreen.AddTask.name)
                    }
                )
            }

            // ==========================================
            // MÀN HÌNH CHI TIẾT (Xem/Sửa Task)
            // ==========================================
            composable(route = "${ToDoListScreen.Detail.name}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""

                val detailViewModel: AddScreenViewModel = viewModel(factory = addViewModelFactory)

                TaskDetailScreen(
                    taskId = taskId, // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ TRUYỀN ID
                    viewModel = detailViewModel,
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }

            // ==========================================
            // MÀN HÌNH THÊM DANH SÁCH (Category)
            // ==========================================
            composable(route = ToDoListScreen.AddList.name) {
                val addListViewModel: AddScreenViewModel = viewModel(factory = addViewModelFactory)

                AddTaskListScreen(
                    addListViewModel = addListViewModel,
                    onBack = {
                        navController.navigateUp()
                    },
                    onCreate = {
                        // TODO: Gọi hàm lưu Category ở đây nếu cần, sau đó thoát màn hình
                        navController.navigateUp()
                    }
                )
            }

            // ==========================================
            // MÀN HÌNH THÊM CÔNG VIỆC (Task)
            // ==========================================
            composable(route = ToDoListScreen.AddTask.name) {
                val addViewModel: AddScreenViewModel = viewModel(factory = addViewModelFactory)

                AddTaskScreen(
                    addViewModel = addViewModel,
                    onBack = {
                        navController.navigateUp()
                    },
                    onCreate = {
                        // Gọi hàm lưu Task vào Room DB.
                        // Hàm saveTask() trả về true nếu không bị trống thông tin
                        if (addViewModel.saveTask()) {
                            navController.navigateUp() // Lưu thành công thì đóng màn hình
                        }
                    }
                )
            }
        }
    }
}