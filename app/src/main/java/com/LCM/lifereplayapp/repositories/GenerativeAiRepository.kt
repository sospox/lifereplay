package com.LCM.lifereplayapp.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MemoryInput(
    val type: String,
    val uriString: String?,
    val text: String?,
    val timestamp: Long
)

class GenerativeAiRepository(private val context: Context) {
    private val apiKey = "AIzaSyBtRUr4X5vtarfTn8dJUmfECkiahT5tMio"
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun generateStory(userName: String, memories: List<MemoryInput>): String? = withContext(Dispatchers.IO) {
        try {
            val sortedMemories = memories.sortedBy { it.timestamp }
            if (sortedMemories.isEmpty()) return@withContext "Add some memories first to generate your Life Replay!"

            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

            val response = model.generateContent(
                content {
                    text("You are a world-class Cinematic Director and Storyteller. Create a 'Life Replay' for $userName - a smooth, emotional, and perfectly edited narrative script that weaves together the following chronological memories.\n\n")
                    text("MEMORIES TIMELINE:\n")
                    
                    sortedMemories.forEach { memory ->
                        val dateStr = dateFormat.format(Date(memory.timestamp))
                        text("[$dateStr] ")
                        
                        when (memory.type) {
                            "IMAGE" -> {
                                memory.uriString?.let { uriStr ->
                                    try {
                                        val uri = Uri.parse(uriStr)
                                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                            val bytes = inputStream.readBytes()
                                            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            if (originalBitmap != null) {
                                                val resized = resizeBitmap(originalBitmap)
                                                image(resized) // Intersperse image in the timeline
                                                text(" Photo Memory: ${memory.text ?: "A captured moment"}\n")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        text(" (Image could not be loaded)\n")
                                    }
                                }
                            }
                            "MUSIC" -> {
                                text(" Soundtrack Change: ${memory.text ?: "Atmospheric music"}\n")
                            }
                            "VOICE" -> {
                                text(" Personal Reflection: ${memory.text ?: "A thought recorded"}\n")
                            }
                        }
                    }

                    text("\nSTORYTELLING INSTRUCTIONS FOR A PERFECT EDIT:\n")
                    text("1. NARRATIVE ARC: Weave these sequential moments into a single, fluid story. Don't just list them.\n")
                    text("2. THE SOUNDTRACK: Treat every 'Soundtrack Change' as a musical cue. Describe how the rhythm and mood shift to match these songs, using them as bridges between photo memories.\n")
                    text("3. VISUAL FLOW: Describe cinematic transitions between the images provided. Mention specific visual details you 'see' in the photos to make it vivid.\n")
                    text("4. TONE: Warm, nostalgic, and inspiring. Make it feel like a polished movie script or a heartfelt documentary.\n")
                    text("5. INTEGRATION: Ensure every single photo and song from the timeline is included in this 'Edit'.\n")
                    text("\nOUTPUT: Write a beautiful narrative script with cinematic cues in [brackets].")
                }
            )
            response.text ?: "The AI was unable to generate a story. Try adding more diverse memories!"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(original: Bitmap): Bitmap {
        val maxSize = 1024
        if (original.width <= maxSize && original.height <= maxSize) return original
        val ratio = original.width.toFloat() / original.height.toFloat()
        val width = if (ratio > 1) maxSize else (maxSize * ratio).toInt()
        val height = if (ratio > 1) (maxSize / ratio).toInt() else maxSize
        return Bitmap.createScaledBitmap(original, width, height, true)
    }
}
