package com.LCM.lifereplayapp.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LCM.lifereplayapp.data.models.Todo
import com.LCM.lifereplayapp.repositories.TodoRepository
import com.LCM.lifereplayapp.repositories.UploadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoViewModel: ViewModel() {
    val todoRepository = TodoRepository()

    private val _uploadProgress = MutableStateFlow<Float>(0f)
    val uploadProgress: StateFlow<Float> get() = _uploadProgress

    private val _todo: MutableStateFlow<Todo> = MutableStateFlow(Todo(
        title = "",
        media = "",
        description = "",
        isComplete = false,
        dueDate = 0,
    ))

    val todo: StateFlow<Todo> get() = _todo

    private val _taskCreated = MutableStateFlow(false)
    val taskCreated: StateFlow<Boolean> get() = _taskCreated

    fun createTask(title: String, description: String, dueDate: Long) {
        viewModelScope.launch {
            try {
                _todo.value = _todo.value.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    isComplete = false
                )
                val result = todoRepository.createTask(_todo.value)
                if (result != null) {
                    _taskCreated.value = true
                    resetTodo()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetTodo() {
        _todo.value = Todo(
            title = "",
            media = "",
            description = "",
            isComplete = false,
            dueDate = 0,
        )
        _uploadProgress.value = 0f
    }

    fun resetTaskCreated() {
        _taskCreated.value = false
    }

    fun insertImage(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            try {
                todoRepository.insertImage(fileName, fileBytes).collect { result ->
                    when (result) {
                        is UploadResult.Progress -> {
                            _uploadProgress.value = result.percent
                        }
                        is UploadResult.Success -> {
                            _todo.value = _todo.value.copy(media = result.url)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}