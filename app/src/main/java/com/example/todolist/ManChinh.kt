package com.example.todolist

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.data.TaskEntity
import com.example.todolist.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Manchinh(
    viewModel: MainViewModel, // Nhận ViewModel được tiêm từ Navigation
    onAddButtonClicked: () -> Unit = {},
    onDetailButonCliked: (TaskEntity) -> Unit = {_ ->},
    onAddTaskButtonClicked: () -> Unit = {}
) {
    // 1. Lấy dữ liệu Real-time từ Room Database thông qua Flow
    val allTasks by viewModel.allTasks.collectAsState()
    val categoriesEntities by viewModel.categories.collectAsState()

    // Chuyển đổi danh sách CategoryEntity sang List<String> cho dễ dùng ở UI
    val categories = categoriesEntities.map { it.name }

    // 2. Trạng thái UI
    val selectedCategory = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val newCategoryText = remember { mutableStateOf("") }

    //Dùng để lưu tên danh mục đang muốn xóa và bật/tắt Dialog xác nhận
    val showDeleteConfirmDialog = remember { mutableStateOf<String?>(null) }

    // Tự động chọn danh mục đầu tiên nếu danh sách category không trống
    LaunchedEffect(categories) {
        if (selectedCategory.value.isEmpty() && categories.isNotEmpty()) {
            selectedCategory.value = categories[0]
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
            // ==========================================
            // THANH CUỘN NGANG (DANH MỤC)
            // ==========================================
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút tạo danh mục mới
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
                                    .clickable(onClick = { showDialog.value = true }) // Bấm vào đây cũng mở Dialog
                            )
                        }
                    )
                }

                // Hiển thị các danh mục lấy từ Database
                items(categories) { category ->
                    val isSelected = selectedCategory.value == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory.value = category },
                        label = { Text(category) },
                        // THÊM PHẦN NÀY: Dấu X hiện ra khi danh mục được chọn
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Category",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            // Mở hộp thoại xác nhận xóa
                                            showDeleteConfirmDialog.value = category
                                        }
                                )
                            }
                        } else null
                    )
                }
            }

            // ==========================================
            // VÙNG NỘI DUNG CHÍNH (CÔNG VIỆC)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = if (selectedCategory.value.isNotEmpty()) "Mục: ${selectedCategory.value}" else "Chưa có danh mục",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyLarge
                )

                // Lọc công việc theo danh mục được chọn
                val filteredTasks = allTasks
                    .filter { it.category == selectedCategory.value }
                    .sortedBy { it.isDone }

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
                            Text(
                                text = "Chưa có công việc nào trong mục này.",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredTasks) { task ->
                            Card(
                                onClick = {
                                    onDetailButonCliked(task)
                                }
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
                                            // Cập nhật trạng thái hoàn thành trực tiếp vào DB
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG THÊM DANH MỤC
    // ==========================================
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
                        if (newCategoryText.value.trim().isNotEmpty()) {
                            val name = newCategoryText.value.trim()

                            // Ghi danh mục mới vào DB thông qua ViewModel
                            viewModel.addCategory(name)

                            selectedCategory.value = name
                            showDialog.value = false
                            newCategoryText.value = ""
                        }
                    }
                ) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false; newCategoryText.value = "" }) {
                    Text("Hủy")
                }
            }
        )
    }
    // ==========================================
    // DIALOG XÓA DANH MỤC
    // ==========================================
    if (showDeleteConfirmDialog.value != null) {
        val categoryToDelete = showDeleteConfirmDialog.value!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog.value = null },
            title = { Text("Xoá danh mục") },
            text = { Text("Bạn có chắc chắn muốn xoá danh mục '$categoryToDelete'? Toàn bộ công việc trong mục này cũng sẽ bị xoá vĩnh viễn.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Gọi hàm xóa trong ViewModel
                        viewModel.deleteCategory(categoryToDelete)

                        // Đóng Dialog và Reset lại mục đang chọn
                        showDeleteConfirmDialog.value = null
                        selectedCategory.value = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xoá")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog.value = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}