package com.akshay.sound.interfaces

import com.akshay.sound.Song

interface PlayerAdapter {
    fun play(song: Song)

    fun pause()

    fun release()

    fun resume()

    fun isPlaying(): Boolean

    fun getCurrentPos(): Int

    fun seekTo(progress: Int)
}
