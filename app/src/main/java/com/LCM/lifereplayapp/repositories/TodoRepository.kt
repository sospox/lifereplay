package com.LCM.lifereplayapp.repositories

import com.LCM.lifereplayapp.data.models.Todo
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class UploadResult {
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
}

class TodoRepository:TodoService {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://hsapkgxtuybcktzepdbr.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhzYXBrZ3h0dXliY2t0emVwZGJyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQyOTY4OTIsImV4cCI6MjA2OTg3Mjg5Mn0.0CzMp75GlRFvT5CdASFcAHS0nX2FocUDCip19hF7Xvw"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    override suspend fun createTask(todo: Todo): Todo? {
        val result = supabase.from("todo").insert(todo) {
            select()
        }.decodeSingleOrNull<Todo>()
        return result
    }

    override suspend fun getAllTasks(): List<Todo> {
        val task = supabase.from("todo").select().decodeList<Todo>()
        return task
    }

    override suspend fun getTask(id: Int): Todo? {
        val todo = supabase.from("todo").select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<Todo>()
        return todo
    }

    override suspend fun updateTask(todo: Todo): Todo? {
        val todoResult = supabase.from("todo").update(
            todo
        ) {
            select()
            filter {
                eq("id", todo.id!!)
            }
        }.decodeSingleOrNull<Todo>()
        return todoResult
    }

    override suspend fun insertImage(
        fileName: String,
        fileBytes: ByteArray
    ): Flow<UploadResult>{
        val bucket = supabase.storage.from("todo/images")

        return bucket.uploadAsFlow(fileName, fileBytes)
            .map { status ->
                when (status) {
                    is UploadStatus.Progress -> {
                        val percent = status.totalBytesSend.toFloat() / status.contentLength * 100
                        UploadResult.Progress(percent)
                    }
                    is UploadStatus.Success -> {
                        println("Upload successful!")
                        UploadResult.Success(bucket.publicUrl(fileName))
                    }
                }
            }
    }

    override suspend fun deleteTask(id: Int): Boolean {
        supabase.from("todo").delete {
            filter {
                eq("id", id)
            }
        }
        return getTask(id) == null
    }
}
