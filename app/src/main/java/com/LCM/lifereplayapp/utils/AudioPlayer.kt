package com.LCM.lifereplayapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(uri: Uri) {
        try {
            stop()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                
                setOnPreparedListener {
                    it.start()
                }
                
                setOnCompletionListener {
                    stop()
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer Error: what=$what extra=$extra")
                    stop()
                    true
                }
                
                // Use prepareAsync to avoid blocking the UI thread (crucial for network URIs)
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to initialize MediaPlayer", e)
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
    }
}
