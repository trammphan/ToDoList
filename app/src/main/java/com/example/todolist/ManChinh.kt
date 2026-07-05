package com.example.todolist

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.data.TaskEntity
import com.example.todolist.ui.MainViewModel
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun getPriorityColor(priority: String?): Color {
    return when (priority) {
        "Do First" -> Color(0xFFE57373) // Đỏ (Ưu tiên cao nhất)
        "Do Next"  -> Color(0xFFFFB74D) // Cam
        "Do Later" -> Color(0xFF64B5F6) // Xanh dương
        "Do Last"  -> Color(0xFF81C784) // Xanh lá (Ưu tiên thấp nhất)
        else       -> Color.Transparent
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Manchinh(
    viewModel: MainViewModel,
    onAddButtonClicked: () -> Unit = {},
    onDetailButonCliked: (TaskEntity) -> Unit = {_ ->},
    onAddTaskButtonClicked: () -> Unit = {}
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val categoriesEntities by viewModel.categories.collectAsState()
    val categories = categoriesEntities.map { it.name }

    val context = LocalContext.current // Dùng để hiển thị Toast thông báo

    val selectedCategory = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val newCategoryText = remember { mutableStateOf("") }
    val showDeleteConfirmDialog = remember { mutableStateOf<String?>(null) }

    // TỐI ƯU 1: Mặc định chọn tab "Ghim" khi vừa mở app
    LaunchedEffect(Unit) {
        if (selectedCategory.value.isEmpty()) {
            selectedCategory.value = "Ghim"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 40.sp
                    )
                },
                windowInsets = TopAppBarDefaults.windowInsets
                    .exclude(WindowInsets.statusBars)
                    .add(WindowInsets(top = 30.dp, bottom = 18.dp)),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskButtonClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm công việc mới",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = { showDialog.value = true },
                        label = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm danh mục mới",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = { showDialog.value = true })
                            )
                        }
                    )
                }

                item {
                    val isPinnedCategorySelected = selectedCategory.value == "Ghim"
                    FilterChip(
                        selected = isPinnedCategorySelected,
                        onClick = { selectedCategory.value = "Ghim" },
                        label = { Text("📌 Ghim") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                items(categories) { category ->
                    val isSelected = selectedCategory.value == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory.value = category },
                        label = { Text(category) },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Category",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showDeleteConfirmDialog.value = category }
                                )
                            }
                        } else null
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = if (selectedCategory.value.isNotEmpty()) "Mục: ${selectedCategory.value}" else "Chưa có danh mục",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val priorityOrder = listOf("Do First", "Do Next", "Do Later", "Do Last")
                val taskComparator = compareBy<TaskEntity> { it.isDone }
                    .thenBy { task ->
                        val index = priorityOrder.indexOf(task.priority)
                        if (index == -1) 4 else index
                    }

                val filteredTasks = allTasks
                    .filter {
                        if (selectedCategory.value == "Ghim") {
                            it.isPinned
                        } else {
                            it.category == selectedCategory.value
                        }
                    }
                    .sortedWith(taskComparator)

                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(R.drawable.macdinh),
                                contentDescription = "Hình mặc định",
                                modifier = Modifier
                                    .size(400.dp)
                                    .padding(bottom = 16.dp)
                            )
                            // TỐI ƯU 2: Đổi câu thông báo rỗng phù hợp với hoàn cảnh
                            Text(
                                text = if (selectedCategory.value == "Ghim") "Bạn chưa ghim công việc nào."
                                else "Chưa có công việc nào trong mục này.",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (selectedCategory.value == "Ghim") {
                            item {
                                Text(
                                    text = "📌 Công việc đã ghim (Toàn hệ thống)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        items(filteredTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(50.dp)
                                        .background(getPriorityColor(task.priority))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Card(
                                    onClick = { onDetailButonCliked(task) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.isDone,
                                            onCheckedChange = { checked ->
                                                viewModel.updateTaskStatus(task, checked)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (task.isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.weight(0.5f))
                                        if (task.isPinned) {
                                            // Cách 1: Vẽ một vòng tròn màu đỏ nhỏ nhắn
                                            Box(
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .background(
                                                        Color(0xFFE57373),
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
                newCategoryText.value = ""
            },
            title = { Text("Thêm danh mục mới") },
            text = {
                OutlinedTextField(
                    value = newCategoryText.value,
                    onValueChange = { newCategoryText.value = it },
                    label = { Text("Tên danh mục") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newCategoryText.value.trim()
                        if (name.isNotEmpty()) {
                            // TỐI ƯU 3: Chặn đặt tên trùng với mục "Ghim" của hệ thống
                            if (name.equals("Ghim", ignoreCase = true)) {
                                Toast.makeText(context, "Tên này đã được hệ thống sử dụng, vui lòng chọn tên khác!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addCategory(name)
                                selectedCategory.value = name
                                showDialog.value = false
                                newCategoryText.value = ""
                            }
                        }
                    }
                ) { Text("Thêm") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false; newCategoryText.value = "" }) { Text("Hủy") }
            }
        )
    }

    if (showDeleteConfirmDialog.value != null) {
        val categoryToDelete = showDeleteConfirmDialog.value!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog.value = null },
            title = { Text("Xoá danh mục") },
            text = { Text("Bạn có chắc chắn muốn xoá danh mục '$categoryToDelete'? Toàn bộ công việc trong mục này cũng sẽ bị xoá vĩnh viễn.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(categoryToDelete)
                        showDeleteConfirmDialog.value = null
                        // Đưa về mục Ghim sau khi xóa danh mục để UI không bị treo
                        selectedCategory.value = "Ghim"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Xoá") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog.value = null }) { Text("Hủy") }
            }
        )
    }
}

@Suppress("ComposableNaming")
@Preview(
    name = "Chế độ sáng (Light Mode)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MainScreenPreview() {
    ToDoListTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val fakeDao = object : com.example.todolist.data.ToDoDao {
                override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(
                    listOf(
                        TaskEntity(
                            id = 1,
                            title = "Học lập trình Jetpack Compose",
                            category = "Học tập",
                            isDone = false,
                            description = "Học cơ bản về State và Preview trong Compose",
                            priority = "Do First",
                            deadline = "2026-12-31",
                            isPinned = true
                        ),
                        TaskEntity(
                            id = 2,
                            title = "Làm bài tập Lab 3 Android",
                            category = "Học tập",
                            isDone = false,
                            description = "Kết nối Room Database vào ứng dụng ToDoList",
                            priority = "Do Next",
                            deadline = "23:59 Hôm nay",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 3,
                            title = "Chuẩn bị slide thuyết trình nhóm",
                            category = "Học tập",
                            isDone = false,
                            description = "Làm slide bài tập lớn môn di động",
                            priority = "Do Next",
                            deadline = "Ngày mai",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 4,
                            title = "Đọc sách Clean Code (Chương 3)",
                            category = "Học tập",
                            isDone = false,
                            description = "Đọc và ghi chú về quy chuẩn đặt tên hàm/biến",
                            priority = "Do Later",
                            deadline = "Chủ nhật",
                            isPinned = true
                        ),
                        TaskEntity(
                            id = 5,
                            title = "Tìm hiểu thêm về Kotlin Multiplatform",
                            category = "Học tập",
                            isDone = false,
                            description = "Xem qua tài liệu giới thiệu cơ bản trên trang chủ",
                            priority = "Do Last",
                            deadline = "Cuối tháng",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 6,
                            title = "Ôn tập trắc nghiệm lý thuyết Android",
                            category = "Học tập",
                            isDone = true,
                            description = "Luyện đề thi thử trên hệ thống",
                            priority = "Do First",
                            deadline = "Đã xong",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 7,
                            title = "Xem lại video record buổi học tuần trước",
                            category = "Học tập",
                            isDone = true,
                            description = "Xem phần chữa bài tập chương 2",
                            priority = "Do Later",
                            deadline = "Đã xong",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 8,
                            title = "Mua đồ ăn tối",
                            category = "Việc nhà",
                            isDone = false,
                            description = "Mua rau và thịt gà",
                            priority = "Do Next",
                            deadline = "18:00",
                            isPinned = false
                        ),
                        TaskEntity(
                            id = 9,
                            title = "Dọn dẹp bàn làm việc",
                            category = "Việc nhà",
                            isDone = true,
                            description = "Sắp xếp lại sách vở và lau bụi",
                            priority = "Do Last",
                            deadline = "Đã xong",
                            isPinned = false
                        )
                    )
                )

                override fun getAllCategories(): Flow<List<com.example.todolist.data.CategoryEntity>> = flowOf(
                    listOf(
                        com.example.todolist.data.CategoryEntity(name = "Học tập"),
                        com.example.todolist.data.CategoryEntity(name = "Việc nhà"),
                        com.example.todolist.data.CategoryEntity(name = "Sức khỏe")
                    )
                )

                override suspend fun insertTask(task: TaskEntity) {}
                override suspend fun updateTask(task: TaskEntity) {}
                override suspend fun insertCategory(category: com.example.todolist.data.CategoryEntity) {}
                override suspend fun deleteTask(taskId: Int) {}
                override suspend fun getTaskById(taskId: Int): TaskEntity? = null
                override suspend fun deleteCategory(categoryName: String) {}
                override suspend fun deleteTasksByCategory(categoryName: String) {}
            }

            val fakeViewModel = remember { MainViewModel(dao = fakeDao) }

            Manchinh(
                viewModel = fakeViewModel,
                onAddButtonClicked = {},
                onDetailButonCliked = {},
                onAddTaskButtonClicked = {}
            )
        }
    }
}