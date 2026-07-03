package com.example.todolist

import android.app.AlertDialog
import android.os.Bundle
import android.text.Layout
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
fun AddTaskScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    addViewModel: AddScreenViewModel
){
    val screenUI by addViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current // BỔ SUNG 1: Khởi tạo FocusManager

    Box(
        modifier = Modifier
            .fillMaxSize() // Ép Box chiếm toàn màn hình để bắt mọi sự kiện chạm
            .background(colorScheme.primaryContainer)
            .padding(vertical = 32.dp, horizontal = 16.dp)
            // BỔ SUNG 2: Bắt sự kiện chạm ra ngoài để tắt bàn phím
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
            Column {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    containerColor = Color.Transparent,
                    topBar = {
                        TopBar(
                            title = R.string.addtask_title,
                            // BỔ SUNG 3: Tắt bàn phím trước khi thoát hoặc tạo mới
                            onBack = {
                                focusManager.clearFocus()
                                onBack()
                            },
                            onCreate = {
                                focusManager.clearFocus()
                                onCreate()
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()) // BỔ SUNG 4: Thêm khả năng cuộn
                    ) {
                        //Task name
                        AddScreenInfo(
                            name = R.string.task_name,
                            value = addViewModel.title,
                            onValueChange = { addViewModel.updateTitle(it) },
                            isMultiline = false
                        )
                        Spacer(Modifier.height(12.dp))
                        //Detailed task description
                        AddScreenInfo(
                            name = R.string.description,
                            value = addViewModel.description,
                            onValueChange = { addViewModel.updateDescription(it) },
                            isMultiline = true
                        )
                        //Pin phan dang luu y
                        Pinned(
                            onFocus = addViewModel.isFocus,
                            onClick = {addViewModel.updateIsFocus(it)}
                        )
                        //Dropdown chon danh sach
                        SingleSelectDropdown (
                            label = R.string.list_choice,
                            items = screenUI.taksListsUnChoose,
                            selected = screenUI.selectedList.firstOrNull(),
                            expanded = addViewModel.expandedTaskList,
                            onExpandedChange = addViewModel::updateExpandedTaskList,
                            onSelect = {
                                addViewModel.selectList(it)
                            },
                            onRemove = { addViewModel.removeList(it)},
                            icon = {
                                Icon(
                                    Icons.Default.Tab,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            chip = {
                                    text, remove -> SelectedJobChips (text, remove)
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        val priorityLabels = PriorityLevel.values()
                            .map { it.label }

                        //Dropdown chon do uu tien
                        SingleSelectDropdown(
                            label = R.string.priority_choice,
                            items = priorityLabels,
                            selected = screenUI.selectedPriority?.label,
                            expanded = addViewModel.priorityExpanded,
                            onExpandedChange = addViewModel::updatePriorityExpanded,
                            onSelect = { addViewModel.updatePriority(it) },
                            onRemove = { addViewModel.removePriorityLevel()},
                            icon = {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            chip = {
                                    text, remove -> PriorityResultChip(text, remove)
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        //Chon date picker
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    addViewModel.openDatePicker()
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            DeadlineSection(
                                selectedDate = screenUI.selectedDate,
                                selectedTime = screenUI.selectedTime,
                                onOpenPicker = { addViewModel.openDatePicker() },
                                onRemoveDeadline = { addViewModel.clearDeadline() }
                            )
                        }
                        if (addViewModel.showDatePicker) {
                            DateTimePickerDialog(
                                showTimePicker = addViewModel.showTimePicker,
                                onToggleTimePicker = addViewModel::toggleTimePicker,
                                onConfirm = { date, time ->
                                    addViewModel.updateDate(date)
                                    if (time != null) addViewModel.updateTime(time)

                                    val combinedDateTime = if (time != null) "$time, $date" else date
                                    addViewModel.updateDateTime(combinedDateTime)
                                },
                                onDismiss = addViewModel::closeDatePicker
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    @StringRes title: Int,
    onBack: () -> Unit = {},
    onCreate: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable(
                        // Tắt ripple + nền xám
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ){ onBack() }
            )
        },
        actions = {
            Text(
                text = "Create",
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable(
                        // Tắt ripple + nền xám
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCreate() }
            )
        }
    )
}

@Composable
fun AddScreenInfo(
    @StringRes name: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    //Tham so tuy chon de xac dinh co phai textarea
    isMultiline: Boolean = false){
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            singleLine = !isMultiline,
            shape = shapes.large,
            maxLines = if (isMultiline) 6 else 1,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isMultiline) Modifier.heightIn(min = 120.dp) else Modifier
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary
            ),
            onValueChange = onValueChange,
            label = { Text(stringResource(name)) },
            isError = false,
            keyboardActions = KeyboardActions(
                onDone = { }
            )
        )
    }
}

@Composable
fun Pinned(
    modifier: Modifier = Modifier,
    onFocus: Boolean,
    onClick: (Boolean) -> Unit){
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        Row(
            modifier = Modifier
                .clickable{onClick(!onFocus)}
        ) {
            Text(
                text = "Pinned",
                color = if(onFocus) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.inverseSurface
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = null,
                tint = if(onFocus) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}
@Composable
fun SingleSelectDropdown(
    @StringRes label: Int,
    items: List<String>,
    selected: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    icon: @Composable (() -> Unit)? = null,
    chip: @Composable (String, (String) -> Unit) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        icon?.invoke()
        Spacer(Modifier.width(16.dp))

        // Da chon -> Hien chips
        if(selected != null){
            chip(selected) { onRemove(selected) } // Tuy Priority Level Chips hay List Chips
        }
        // Chua chon -> Hien text
        else {
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.clickable {
                    onExpandedChange(true)
                }
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown Button",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(22.dp)
                .clickable{
                    if(selected == null){
                        onExpandedChange(true)
                    }
                }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {onExpandedChange(false)}
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun SelectedJobChips(
    selected: String?,
    onRemove: (String) -> Unit
) {
    if (selected != null) {
        AssistChip(
            onClick = {},
            label = { Text(selected) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.clickable { onRemove(selected) }
                )
            }
        )
    }
}
//Mau nen cua chip
@Composable
fun priorityBackground(level: PriorityLevel): Color =
    when (level) {
        PriorityLevel.Do_First -> MaterialTheme.colorScheme.error
        PriorityLevel.Do_Next -> MaterialTheme.colorScheme.primary
        PriorityLevel.Do_Later -> MaterialTheme.colorScheme.secondary
        PriorityLevel.Do_Last -> MaterialTheme.colorScheme.tertiary
    }
//Mau chu, icon 'Close' cua chip
@Composable
fun priorityText_Icon(level: PriorityLevel): Color =
    when (level) {
        PriorityLevel.Do_First -> MaterialTheme.colorScheme.errorContainer
        PriorityLevel.Do_Next -> MaterialTheme.colorScheme.primaryContainer
        PriorityLevel.Do_Later -> MaterialTheme.colorScheme.secondaryContainer
        PriorityLevel.Do_Last -> MaterialTheme.colorScheme.tertiaryContainer
    }
@Composable
fun PriorityResultChip(
    priority: String,
    onRemove: (String) -> Unit
) {
    // Convert String -> PriorityLevel
    val level = PriorityLevel.values().first { it.label == priority }
    AssistChip(
        onClick = { onRemove(priority) },
        label = {
            Text(
                text = priority,
                color = priorityText_Icon(level)
            )
        },
        trailingIcon = { //dung trailingIcon de icon nam ben phai text, leadingicon nam ben trai text
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = priorityText_Icon(level))
        },
        colors = AssistChipDefaults.assistChipColors(
            priorityBackground(level)
        )
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    showTimePicker: Boolean,
    onToggleTimePicker: () -> Unit,
    onConfirm: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val dateState = rememberDatePickerState() // Bien State bat buoc co de dieu kien Date Picker
    val timeState = rememberTimePickerState() // Bien State bat buoc co de dieu kien Time Picker

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = dateState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()

                        val time = if (showTimePicker) {
                            "%02d:%02d".format(timeState.hour, timeState.minute)
                        } else null

                        onConfirm(date, time)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column {

                //Chi hien thi 1 trong 2: datepicker or timepicker
                if (!showTimePicker) {
                    Text("Chọn ngày", style = MaterialTheme.typography.titleMedium)
                    DatePicker(state = dateState)
                } else {
                    Text("Chọn giờ", style = MaterialTheme.typography.titleMedium)
                    TimePicker(state = timeState)
                }

                Spacer(Modifier.height(16.dp))

                //Nut chuyen doi giua datepicker va timepicker
                Text(
                    text = if (showTimePicker) "Quay lại chọn ngày" else "Chọn giờ (tùy chọn)",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onToggleTimePicker() }
                )
            }
        }
    )
}
@Composable
fun DeadlineChip(
    date: String,
    time: String,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = buildString {
                    if (time.isNotEmpty()) append("$time, ")
                    append(date)
                }
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onRemove() }
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
@Composable
fun DeadlineSection(
    selectedDate: String,
    selectedTime: String,
    onOpenPicker: () -> Unit,
    onRemoveDeadline: () -> Unit
) {
    if (selectedDate.isEmpty() && selectedTime.isEmpty()) {
        Text(
            text = stringResource(R.string.deadline),
            modifier = Modifier
                .clickable { onOpenPicker() }
                .padding(8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        DeadlineChip(
            date = selectedDate,
            time = selectedTime,
            onRemove = onRemoveDeadline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ManHinhThemCVPreview(){
    ToDoListTheme(dynamicColor = false) {
        // CẬP NHẬT 3: Tạm ẩn Preview để tránh lỗi Database Context
        /* AddTaskScreen(
            onBack = {},
            onCreate = {},
            // addViewModel = ... (Cần mock dữ liệu ở đây nếu muốn bật lại preview)
        ) */
    }
}