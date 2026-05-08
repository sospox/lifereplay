package com.LCM.lifereplayapp.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class GenerativeAiRepository(private val context: Context) {
    // Note: In a real app, use an API key from a secure place (like BuildConfig or a backend)
    private val apiKey = "AIzaSyBtRUr4X5vtarfTn8dJUmfECkiahT5tMio"
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun generateStory(memories: List<Pair<String?, String?>>): String? = withContext(Dispatchers.IO) {
        try {
            val historyText = memories.mapNotNull { it.second }.joinToString("\n")
            val bitmaps = memories.mapNotNull { it.first }.mapNotNull { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        
                        if (originalBitmap != null && (originalBitmap.width > 1024 || originalBitmap.height > 1024)) {
                            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                            val newWidth = if (ratio > 1) 1024 else (1024 * ratio).toInt()
                            val newHeight = if (ratio > 1) (1024 / ratio).toInt() else 1024
                            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                        } else {
                            originalBitmap
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (historyText.isBlank() && bitmaps.isEmpty()) return@withContext "Add some memories first to generate a story!"

            val response = model.generateContent(
                content {
                    bitmaps.forEach { image(it) }
                    text("Based on these memories and photos, create a cohesive and emotional 'Life Replay' story. Here is the context of what happened:\n$historyText")
                }
            )
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
