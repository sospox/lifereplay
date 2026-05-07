package com.LCM.lifereplayapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(uri: Uri) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            start()
            setOnCompletionListener {
                stop()
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
