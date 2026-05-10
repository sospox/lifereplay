package com.LCM.lifereplayapp.repositories

import com.LCM.lifereplayapp.data.models.Todo
import kotlinx.coroutines.flow.Flow

interface TodoService {
    suspend fun createTask(todo: Todo):Todo? // create task
    suspend fun getAllTasks(): List<Todo> // read all tasks
    suspend fun getTask(id:Int): Todo? // read one task
    suspend fun updateTask(todo: Todo):Todo? // update task
    suspend fun insertImage(fileName: String, fileBytes: ByteArray): Flow<UploadResult> // insert image
    suspend fun deleteTask(id:Int): Boolean // delete task and return true or false based on success
}