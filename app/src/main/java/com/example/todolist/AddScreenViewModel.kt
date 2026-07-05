package com.example.todolist.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.PriorityLevel
import com.example.todolist.data.TaskEntity
import com.example.todolist.data.ToDoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTaskUiState(
    val selectedPriority: PriorityLevel? = null,
    val taksListsUnChoose: List<String> = emptyList(),
    val selectedList: List<String> = emptyList(),
    val selectedDate: String = "",
    val selectedTime: String = "",
    val selectedDateTime: String = "",
)

class AddScreenViewModel(private val dao: ToDoDao): ViewModel() {
    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    init {
        loadTaskList()
    }

    private fun loadTaskList(){
        viewModelScope.launch {
            dao.getAllCategories().collect { categories ->
                val categoryNames = categories.map { it.name }
                _uiState.update { it.copy(taksListsUnChoose = categoryNames) }
            }
        }
    }

    var title by mutableStateOf("")
        private set
    fun updateTitle(value: String){
        title = value
    }
    fun clearTitle() = updateTitle("")

    var description by mutableStateOf("")
        private set
    fun updateDescription(value: String){
        description = value
    }
    fun clearDescription() = updateDescription("")

    var isFocus by mutableStateOf(false)
        private set
    fun updateIsFocus(value: Boolean){
        isFocus = value
    }

    var expandedTaskList by mutableStateOf(false)
        private set
    fun updateExpandedTaskList(value: Boolean) {
        expandedTaskList = value
    }
    var priorityExpanded by mutableStateOf(false)
        private set
    fun updatePriorityExpanded(expanded: Boolean) {
        priorityExpanded = expanded
    }

    fun updatePriority(label: String) {
        _uiState.update {
            if (it.selectedPriority == null) {
                val priority = PriorityLevel.values().first { p -> p.label == label }
                it.copy(selectedPriority = priority)
            } else {
                it
            }
        }
    }

    fun removePriorityLevel(){
        _uiState.update { it.copy(selectedPriority = null) }
    }

    var showDatePicker by mutableStateOf(false)
        private set
    var showTimePicker by mutableStateOf(false)
        private set

    fun selectList(list: String){
        _uiState.update { state ->
            state.copy(
                taksListsUnChoose = state.taksListsUnChoose - list,
                selectedList = state.selectedList + list
            )
        }
    }
    fun removeList(list: String){
        _uiState.update { state ->
            state.copy(
                taksListsUnChoose = state.taksListsUnChoose + list,
                selectedList = state.selectedList - list
            )
        }
    }

    fun openDatePicker() {
        showDatePicker = true
        showTimePicker = false
    }
    fun closeDatePicker() {
        showDatePicker = false
    }
    fun toggleTimePicker() {
        showTimePicker = !showTimePicker
    }
    fun updateDate(date: String) {
        _uiState.update {
            it.copy(selectedDate = date)
        }
    }
    fun updateTime(time: String) {
        _uiState.update {
            it.copy(selectedTime = time)
        }
    }
    fun clearDeadline() {
        _uiState.update {
            it.copy(
                selectedDate = "",
                selectedTime = ""
            )
        }
    }

    var isDescriptionFocused by mutableStateOf(false)
        private set
    fun updateIsDescriptionFocused(value: Boolean){
        isDescriptionFocused = value
    }
    var showMenu by mutableStateOf(false)
        private set
    fun toggleShowMenu(){
        showMenu = !showMenu
    }
    fun openMenu(){
        showMenu = true
    }
    fun closeMenu(){
        showMenu = false
    }
    fun updateDateTime(newDateTime: String) {
        _uiState.update { it.copy(selectedDateTime = newDateTime) }
    }
    fun clearDateTime() = updateDateTime("")

    fun resetState() {
        _uiState.value = AddTaskUiState()
        title = ""
        description = ""
        isFocus = false
        isDescriptionFocused = false
        expandedTaskList = false
        priorityExpanded = false
        showDatePicker = false
        showTimePicker = false
        showMenu = false
        currentTaskId = null
        currentTaskIsDone = false
    }

    var currentTaskId: Int? = null
        private set
    var currentTaskIsDone by mutableStateOf(false)
        private set

    fun loadTaskForEdit(taskId: Int) {
        viewModelScope.launch {
            try {
                val task = dao.getTaskById(taskId)

                if (task != null) {
                    currentTaskId = task.id
                    currentTaskIsDone = task.isDone

                    updateTitle(task.title)
                    updateDescription(task.description)
                    updateIsFocus(task.isPinned)

                    _uiState.update { state ->
                        state.copy(
                            selectedList = listOf(task.category),
                            selectedPriority = PriorityLevel.values().find { p -> p.label == task.priority },
                            selectedDateTime = task.deadline
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(onComplete: () -> Unit) {
        currentTaskId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                dao.deleteTask(id)
                launch(Dispatchers.Main) { onComplete() }
            }
        }
    }

    fun saveCategory(name: String, onComplete: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCategory(com.example.todolist.data.CategoryEntity(name))
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun saveTask(): Boolean {
        val currentData = _uiState.value

        // CHỈ KIỂM TRA TÊN CÔNG VIỆC VÀ DANH MỤC (LIST)
        // Bỏ đi điều kiện kiểm tra Priority và DateTime
        if (title.isBlank() || currentData.selectedList.isEmpty()) {
            return false // Trả về false nếu thiếu Tên hoặc Danh mục
        }

        val taskToSave = TaskEntity(
            id = currentTaskId ?: 0,
            title = title,
            description = description,
            category = currentData.selectedList.first(),

            // Nếu người dùng không chọn, Priority sẽ được lưu là null
            priority = currentData.selectedPriority?.label,

            // Nếu không chọn lịch, Deadline sẽ lưu chuỗi rỗng ""
            deadline = currentData.selectedDateTime,

            isDone = currentTaskIsDone,

            isPinned = isFocus
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTask(taskToSave)
        }
        return true
    }

    // ĐÂY LÀ HÀM MỚI ĐƯỢC THÊM VÀO
    fun markAsDoneAndSave(): Boolean {
        currentTaskIsDone = true // Đánh dấu tick hoàn thành
        return saveTask() // Gọi lại hàm lưu
    }

    // Hàm ép công việc thành "Chưa hoàn thành" rồi lưu
    fun markAsUndoneAndSave(): Boolean {
        currentTaskIsDone = false // Gỡ dấu tickF
        return saveTask()
    }
}