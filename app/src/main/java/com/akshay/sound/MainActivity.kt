package com.akshay.sound

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import com.akshay.sound.adapter.SongsListAdapter
import com.akshay.sound.interfaces.PlayerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"
    private var subscription: CompositeDisposable = CompositeDisposable()
    private val songListAdapter: SongsListAdapter by lazy {
        SongsListAdapter(mutableListOf())
    }
    private lateinit var playerAdapter: PlayerAdapter

    private lateinit var musicPlayer: MusicPlayer
    private var seekBarDisposable: Disposable? = null

    private var updateSeekBarObservable: Observable<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initViews()

        showProgress()
        loadSongs()
    }

    private lateinit var songName: TextView
    private lateinit var songArtist: TextView
    private lateinit var songArt: ImageView
    private lateinit var musicFab: FloatingMusicActionButton
    private lateinit var nowPlayingBottomLayout: View
    private lateinit var nowPlayingSeekBar: AppCompatSeekBar

    private fun initViews() {
        nowPlayingBottomLayout = findViewById(R.id.view_bottom_now_playing)
        nowPlayingSeekBar = findViewById(R.id.seekbar_now_playing)
        songName = findViewById(R.id.tv_song_name_now_playing)
        songName.isSelected = true
        songArtist = findViewById(R.id.tv_song_artist_now_playing)
        songArt = findViewById(R.id.iv_song_art_now_playing)

        nowPlayingSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerAdapter.seekTo(progress*1000)
                }
            }
        })


        musicFab = findViewById(R.id.btn_play_now_playing)
        musicFab.setOnMusicFabClickListener(object: FloatingMusicActionButton.OnMusicFabClickListener {
            override fun onClick(view: View) {
                togglePlay()
                updateMusicFab()
            }
        })

        rv_songs_list.visibility = View.VISIBLE
        rv_songs_list.layoutManager = LinearLayoutManager(this)
        rv_songs_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv_songs_list.adapter = songListAdapter
        (rv_songs_list.adapter as SongsListAdapter).getItemClickSubject()
                .subscribe (
                    { song ->
                        playSong(song)
                        updateNowPlaying(song)
                    },
                    {
                        throwable -> Log.e(TAG, throwable.toString())
                    }
                )
    }

    private fun togglePlay() {
        if (playerAdapter.isPlaying()) {
            playerAdapter.pause()
            seekBarDisposable?.dispose()
        } else {
            playerAdapter.resume()
            subscribeToSeekBarUpdate()
        }
    }

    private fun updateMusicFab() {
        if (playerAdapter.isPlaying())
            musicFab.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE)
        else
            musicFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
    }

    private fun updateNowPlaying(song: Song) {
        nowPlayingBottomLayout.visibility = View.VISIBLE
        songName.text = song.title

        songArtist.text = song.artist

        Glide.with(this)
                .load(getAlbumArtUri(song.albumId))
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher))
                .into(songArt)

        subscribeToSeekBarUpdate()
    }

    private fun createObservable(): Observable<Long>? {
        updateSeekBarObservable = null
        updateSeekBarObservable = Observable.timer(1, TimeUnit.SECONDS).repeat().observeOn(AndroidSchedulers.mainThread())
        return updateSeekBarObservable
    }

    private fun subscribeToSeekBarUpdate() {
        createObservable()
        if (seekBarDisposable != null && !seekBarDisposable!!.isDisposed) {
            seekBarDisposable!!.dispose()
        }

        seekBarDisposable = updateSeekBarObservable?.subscribe {
            nowPlayingSeekBar.progress = playerAdapter.getCurrentPos() / 1000
            Log.d(TAG, nowPlayingSeekBar.progress.toString())
        }

        subscription.add(seekBarDisposable!!)
    }

    private fun playSong(song: Song) {
        playerAdapter.play(song)
        nowPlayingSeekBar.max = song.duration / 1000
        musicFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
    }

    private fun loadSongs() {
        subscription.add(fetchSongs(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { songList ->
                    hideProgress()
                    populateSongList(songList)
                },
                { e -> Log.d(TAG, e.toString()) })
        )
    }

    private fun showProgress() {
        loading_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        loading_bar.visibility = View.GONE
    }

    private fun populateSongList(songList: MutableList<Song>) {
        songListAdapter.setList(songList)
        musicPlayer = MusicPlayer()
        playerAdapter = musicPlayer
    }

   /* override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> {
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    override fun onResume() {
        super.onResume()
        subscription = CompositeDisposable()
    }

    override fun onPause() {
        super.onPause()
        subscription.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.release()
    }
}

