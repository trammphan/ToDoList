package com.example.todolist

import android.app.AlertDialog
import android.os.Bundle
import android.text.Layout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.PriorityLevel
import com.example.todolist.ui.AddScreenViewModel
import com.example.todolist.ui.theme.ToDoListTheme
import com.example.todolist.ui.theme.shapes
import java.sql.Date
import java.sql.Time
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String = "",
    onBack: () -> Unit = {},
    viewModel: AddScreenViewModel
) {
    val screenUI by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current // LẤY CONTEXT ĐỂ HIỂN THỊ TOAST

    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        val id = taskId.toIntOrNull()
        if (id != null) {
            viewModel.loadTaskForEdit(id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.primaryContainer)
            .padding(vertical = 32.dp, horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Task Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    focusManager.clearFocus()
                                    onBack()
                                }
                            )
                        },
                        actions = {
                            IconButton(onClick = { showDeleteDialog.value = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = colorScheme.error
                                )
                            }

                            // NẾU CÔNG VIỆC CHƯA HOÀN THÀNH THÌ MỚI HIỆN NÚT SAVE
                            if (!viewModel.currentTaskIsDone) {
                                Text(
                                    text = "Save",
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            focusManager.clearFocus()
                                            // GỌI HÀM LƯU VÀ HIỂN THỊ THÔNG BÁO
                                            if (viewModel.saveTask()) {
                                                Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Vui lòng nhập tên công việc và chọn danh mục!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            // KIỂM TRA TRẠNG THÁI ĐỂ GỌI ĐÚNG HÀM
                            if (viewModel.currentTaskIsDone) {
                                if (viewModel.markAsUndoneAndSave()) {
                                    onBack()
                                }
                            } else {
                                if (viewModel.markAsDoneAndSave()) {
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            // Đổi màu sắc nếu đang ở trạng thái đã hoàn thành
                            containerColor = if (viewModel.currentTaskIsDone) colorScheme.secondaryContainer else colorScheme.primary,
                            contentColor = if (viewModel.currentTaskIsDone) colorScheme.onSecondaryContainer else colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (viewModel.currentTaskIsDone) "Undone" else "Done",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    AddScreenInfo(
                        name = R.string.task_name,
                        value = viewModel.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        isMultiline = false
                    )
                    Spacer(Modifier.height(12.dp))

                    AddScreenInfo(
                        name = R.string.description,
                        value = viewModel.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        isMultiline = true
                    )

                    Pinned(
                        onFocus = viewModel.isFocus,
                        onClick = { viewModel.updateIsFocus(it) }
                    )

                    SingleSelectDropdown (
                        label = R.string.list_choice,
                        items = screenUI.taksListsUnChoose,
                        selected = screenUI.selectedList.firstOrNull(),
                        expanded = viewModel.expandedTaskList,
                        onExpandedChange = viewModel::updateExpandedTaskList,
                        onSelect = { viewModel.selectList(it) },
                        onRemove = { viewModel.removeList(it) },
                        icon = {
                            Icon(
                                Icons.Default.Tab,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        },
                        chip = { text, remove -> SelectedJobChips(text, remove) }
                    )
                    Spacer(Modifier.height(16.dp))

                    val priorityLabels = PriorityLevel.values().map { it.label }
                    SingleSelectDropdown(
                        label = R.string.priority_choice,
                        items = priorityLabels,
                        selected = screenUI.selectedPriority?.label,
                        expanded = viewModel.priorityExpanded,
                        onExpandedChange = viewModel::updatePriorityExpanded,
                        onSelect = { viewModel.updatePriority(it) },
                        onRemove = { viewModel.removePriorityLevel() },
                        icon = {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        },
                        chip = { text, remove -> PriorityResultChip(text, remove) }
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.clickable { viewModel.openDatePicker() }
                        )
                        Spacer(Modifier.width(12.dp))

                        if (screenUI.selectedDateTime.isEmpty()) {
                            Text(
                                text = stringResource(R.string.deadline),
                                modifier = Modifier
                                    .clickable { viewModel.openDatePicker() }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            DeadlineChip(
                                date = screenUI.selectedDateTime,
                                time = "",
                                onRemove = {
                                    viewModel.clearDeadline()
                                    viewModel.clearDateTime()
                                }
                            )
                        }
                    }

                    if (viewModel.showDatePicker) {
                        DateTimePickerDialog(
                            showTimePicker = viewModel.showTimePicker,
                            onToggleTimePicker = viewModel::toggleTimePicker,
                            onConfirm = { date, time ->
                                viewModel.updateDate(date)
                                if (time != null) viewModel.updateTime(time)

                                val combinedDateTime = if (time != null) "$time, $date" else date
                                viewModel.updateDateTime(combinedDateTime)
                            },
                            onDismiss = viewModel::closeDatePicker
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Xóa công việc") },
            text = { Text("Bạn có chắc chắn muốn xóa công việc này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog.value = false
                        viewModel.deleteTask(onComplete = onBack)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ManHinhChiTietPreview() {
    ToDoListTheme(dynamicColor = false) {}
}