package com.akshay.sound

import android.net.Uri
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import io.reactivex.Observable

val sArtworkUri = Uri.parse("content://media/external/audio/albumart")

fun getAlbumArtUri(albumId: String): Uri {
    return ContentUris.withAppendedId(sArtworkUri, albumId.toLong())
}

fun fetchSongs(context: Context): Observable<MutableList<Song>> {
    return Observable.create { subscriber ->
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DURATION)

        val sortOrder = MediaStore.Audio.AudioColumns.TITLE + " ASC"

        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)
            val songsList =  mutableListOf<Song>()
            while (cursor.moveToNext()) {
                songsList.add(Song(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5)))
            }
            subscriber.onNext(songsList)
            subscriber.onComplete()
        } catch (e: Exception) {
            subscriber.onError(e)
        } finally {
            cursor?.close()
        }
    }
}