package com.akshay.sound

import android.media.MediaPlayer
import com.akshay.sound.interfaces.PlayerAdapter

class MusicPlayer: PlayerAdapter, MediaPlayer.OnPreparedListener {

    private var duration: Int = 0

    private var seekToPos: Int = 0

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }

    override fun play(song: Song) {
        mediaPlayer.reset()

        seekToPos = 0
        mediaPlayer.setDataSource(song.filePath)
        this.duration = song.duration
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.prepare()
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
    }

    override fun seekTo(progress: Int) {
        mediaPlayer.seekTo(progress)
    }

    override fun isPlaying():Boolean {
        return mediaPlayer.isPlaying
    }

    override fun resume() {
        mediaPlayer.start()
    }

    override fun getCurrentPos(): Int {
        return mediaPlayer.currentPosition
    }

    override fun release() {
        mediaPlayer.release()
    }
}
