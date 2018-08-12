package com.akshay.sound.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.akshay.sound.R
import com.akshay.sound.Song
import com.akshay.sound.getAlbumArtUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_song_item.view.*

class SongListItemViewHolder(val view: View, private val clickSubject: PublishSubject<Song>) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val tvName = view.tv_song_name
    private val tvArtistName = view.tv_song_artist
    private val tvDuration = view.tv_song_duration
    private val ivImage = view.iv_song_art

    private lateinit var song: Song

    init {
        view.setOnClickListener(this)
    }

    fun bindData(song: Song) {
        this.song = song
        tvName.text = song.title
        tvArtistName.text = song.artist

        val minutes = song.duration / 1000 / 60
        val seconds = song.duration / 1000 % 60
        var min: String = minutes.toString()
        var sec: String = seconds.toString()

        if (minutes < 10) {
            min = "0$minutes"
        }

        if (seconds < 10) {
            sec = "0$seconds"
        }

        tvDuration.text = String.format("%s:%s", min, sec)

        Glide.with(view.context)
                .load(getAlbumArtUri(song.albumId))
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher))
                .into(ivImage)
    }

    override fun onClick(v: View?) {
        clickSubject.onNext(song)
    }
}
