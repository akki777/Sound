package com.akshay.sound.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akshay.sound.R
import com.akshay.sound.Song
import com.akshay.sound.viewholder.SongListItemViewHolder
import io.reactivex.subjects.PublishSubject

class SongsListAdapter(private val songsList: MutableList<Song>) : RecyclerView.Adapter<SongListItemViewHolder>() {

    private val itemClickSubject: PublishSubject<Song> = PublishSubject.create()

    fun setList(list: MutableList<Song>) {
        songsList.clear()
        songsList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListItemViewHolder {
        return SongListItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_song_item, parent, false), itemClickSubject)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    override fun onBindViewHolder(holder: SongListItemViewHolder, position: Int) {
        holder.bindData(songsList[position])
    }

    fun getItemClickSubject(): PublishSubject<Song> {
        return itemClickSubject
    }
}