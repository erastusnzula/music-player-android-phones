package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        var musicList: ArrayList<MusicFile> = ArrayList()
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var isLooping: Boolean = false
        var newSongOnclick: Boolean = false
        var musicService: MusicService? = null
        var currentPos = 0
        var isPlayerActive = false
        var isShuffle = false
        var minutes5 = false
        var minutes10 = false
        var minutes15= false
        var minutes20 = false

        @SuppressLint("StaticFieldLeak")
        lateinit var activePlayButton: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var activeAlbum: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var activeSongName: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var songStartTime: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var songEndTime: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var seekBar: SeekBar

        @SuppressLint("StaticFieldLeak")
        lateinit var albumLayout: LinearLayout

        @SuppressLint("StaticFieldLeak")
        lateinit var shuffleButton: ImageButton

        @SuppressLint("StaticFieldLeak")
        lateinit var repeatImageButton: ImageButton

    }

    private lateinit var nextSong: ImageButton
    private lateinit var previousSong: ImageButton
    private lateinit var equalizer: ImageButton
    private lateinit var timer : ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_player)
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Current playing"
        activeAlbum = findViewById(R.id.activeSongAlbumImageView)
        albumLayout = findViewById(R.id.activeAlbumPictureLayout)
        activePlayButton = findViewById(R.id.ActivePlayImageButton)
        activeSongName = findViewById(R.id.activeCurrentSong)
        songEndTime = findViewById(R.id.activeEndTime)
        nextSong = findViewById(R.id.activeNextImageButton)
        previousSong = findViewById(R.id.activePreviousImageButton)
        shuffleButton = findViewById(R.id.activeShuffleImageButton)
        repeatImageButton = findViewById(R.id.activeRepeatImageButton)
        songStartTime = findViewById(R.id.activeStartTime)
        seekBar = findViewById(R.id.seekBar)
        equalizer = findViewById(R.id.activeEqualizerImageButton)
        timer = findViewById(R.id.activeTimerImageButton)
        setUpInitialization()


        activePlayButton.setOnClickListener {
            playPauseControl()
        }
        nextSong.setOnClickListener {
            playNextSong(increment = true)
        }
        previousSong.setOnClickListener {
            playNextSong(increment = false)
        }
        shuffleButton.setOnClickListener {
            shuffle()
        }
        equalizer.setOnClickListener {
            equalizerIntent()
        }
        repeatImageButton.setOnClickListener {
            repeatControl()
        }
        timer.setOnClickListener {
            val setTime = minutes5 || minutes10|| minutes15|| minutes20
            if(!setTime){
                closeAppTimer()
            }else{
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Cancel end music timer?")
                dialog.setPositiveButton("Yes"){_,_->
                    minutes5=false
                    minutes10=false
                    minutes20=false
                    minutes15=false
                    timer.setColorFilter(ContextCompat.getColor(this,R.color.teal_200))
                }
                dialog.show()
            }

        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.favorite -> {
                startActivity(Intent(this@PlayerActivity, FavouriteActivity::class.java))
            }
            R.id.playlist -> {
                startActivity(Intent(this@PlayerActivity, PlaylistActivity::class.java))
            }
            R.id.about -> {}
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentServices()
        createMediaPlayer()
        MainActivity.mainBottomControls.visibility = View.VISIBLE
        MainActivity.mainCurrentLayout.visibility = View.VISIBLE
        musicService!!.runnableSeekBar()
        if (isPlaying) {
            MainActivity.mainCurrent.setTextColor(ContextCompat.getColor(this, R.color.green))
        }
        isPlayerActive = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        newSongOnclick = true
        try {
            setSongPosition(increment = true)
            MainActivity.mainCurrent.text = musicList[songPosition].title
            createMediaPlayer()
            try {
                currentPlayingSongSetup()
            } catch (e: Exception) {
                return
            }
        } catch (e: Exception) {
            playNextSong(increment = true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            albumLayout.visibility = View.GONE
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            albumLayout.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20 || resultCode == RESULT_OK) return
    }

    private fun currentPlayingSongSetup() {
        try {
            Glide.with(this)
                .load(musicList[songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(activeAlbum)

            activeSongName.text = musicList[songPosition].title
            if (isLooping) repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            if (isShuffle) shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.green))
            if(minutes5|| minutes10|| minutes15|| minutes20) timer.setColorFilter(ContextCompat.getColor(this,R.color.purple_700))
        } catch (e: Exception) {
            songPosition = currentPos
            musicList.addAll(MainActivity.musicList)
            Glide.with(this)
                .load(musicList[songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(activeAlbum)

            activeSongName.text = musicList[songPosition].title
            if (isLooping) repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            if (isShuffle) shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.green))

        }

    }

    private fun setUpInitialization() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "MusicRecyclerAdapter" -> {
                newSongOnclick = true
                musicList.addAll(MainActivity.musicList)
                currentPlayingSongSetup()
                fromMainActivityLayout()
            }
            "MainActivity" -> {
                songPosition = currentPos
                musicList.addAll(MainActivity.musicList)
                newSongOnclick = intent.getBooleanExtra("sameSong", false)
                if (isPlaying) {
                    activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                }

                currentPlayingSongSetup()
                fromMainActivityLayout()

            }

        }

    }

    private fun fromMainActivityLayout() {
        val mainCurrent = MainActivity.mainCurrent
        val mainPrevious = MainActivity.mainPrevious
        val mainNext = MainActivity.mainNext
        val mainPlay = MainActivity.playButton
        mainCurrent.text = musicList[songPosition].title
        mainPlay.setOnClickListener {
            playPauseControl()
        }
        mainNext.setOnClickListener {
            playNextSong(increment = true)
        }
        mainPrevious.setOnClickListener {
            playNextSong(increment = false)
        }


    }

    private fun createMediaPlayer() {
        try {

            when {
                musicService!!.mediaPlayer == null -> {
                    playSong()

                }
                newSongOnclick -> {
                    musicService!!.mediaPlayer!!.stop()
                    musicService!!.mediaPlayer!!.release()
                    playSong()

                }
                else -> {
                    songStartTime.text =
                        formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                    songEndTime.text =
                        formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                    seekBar.progress = 0
                    seekBar.max = musicService!!.mediaPlayer!!.duration
                    musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                    return
                }
            }
        } catch (e: Exception) {
            return
        }
    }

    private fun playSong() {
        musicService!!.mediaPlayer = MediaPlayer()
        musicService!!.mediaPlayer!!.reset()
        musicService!!.mediaPlayer!!.setDataSource(musicList[songPosition].path)
        musicService!!.mediaPlayer!!.prepare()
        musicService!!.mediaPlayer!!.start()
        isPlaying = true
        activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        MainActivity.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        musicService!!.showNotification(R.drawable.ic_baseline_pause_24, 1F)
        songStartTime.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
        songEndTime.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
        seekBar.progress = 0
        seekBar.max = musicService!!.mediaPlayer!!.duration
        musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        currentPos = songPosition


    }

    private fun playPauseControl() {
        if (isPlaying) {
            pauseSong()
        } else {
            resume()
        }
    }

    private fun resume() {
        isPlaying = true
        activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        MainActivity.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_baseline_pause_24, 1F)

    }

    private fun pauseSong() {
        isPlaying = false
        activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        MainActivity.playButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24, 0F)
    }

    private fun playNextSong(increment: Boolean) {
        if (increment) {
            newSongOnclick = true
            setSongPosition(increment = true)
            activeSongName.text = musicList[songPosition].title
            MainActivity.mainCurrent.text = musicList[songPosition].title
            createMediaPlayer()
        } else {
            newSongOnclick = true
            setSongPosition(increment = false)
            activeSongName.text = musicList[songPosition].title
            MainActivity.mainCurrent.text = musicList[songPosition].title
            createMediaPlayer()
        }

    }


    private fun shuffle() {
        if (!isShuffle) {
//            songPosition = (Math.random() * (musicList.size - 1)).toInt()
//            currentPlayingSongSetup()
//            createMediaPlayer()
            isShuffle = true
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.green))
            isLooping=false
            repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_24)
        } else {
            isShuffle = false
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
        }
    }

    private fun repeatControl() {
        if (isLooping) {
            isLooping = false
            repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_24)
        } else {
            isLooping = true
            isShuffle=false
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
        }
    }

    private fun equalizerIntent() {
        try {
            val equalizerInt = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            equalizerInt.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                musicService!!.mediaPlayer!!.audioSessionId
            )
            equalizerInt.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            equalizerInt.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            startActivityForResult(equalizerInt, 20)

        } catch (e: Exception) {
            Toast.makeText(this, "Equalizer not supported.", Toast.LENGTH_LONG).show()
        }
    }

    private fun closeAppTimer() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_dialog)
        dialog.show()
        val fiveMinutes = dialog.findViewById<LinearLayout>(R.id.fiveMinutes)
        val tenMinutes = dialog.findViewById<LinearLayout>(R.id.tenMinutes)
        val fifteenMinutes = dialog.findViewById<LinearLayout>(R.id.fifteenMinutes)
        val twentyMinutes = dialog.findViewById<LinearLayout>(R.id.twentyMinutes)
        fiveMinutes?.setOnClickListener{
            timer.setColorFilter(ContextCompat.getColor(this,R.color.green))
            Toast.makeText(this, "EMU player will stop playing in 5 minutes",Toast.LENGTH_LONG).show()
            minutes5 = true
            Thread{Thread.sleep(5000*60)
            if (minutes5) exitProtocol()}.start()

            dialog.dismiss()

        }
        tenMinutes?.setOnClickListener {
            timer.setColorFilter(ContextCompat.getColor(this,R.color.green))
            Toast.makeText(this, "EMU player will stop playing in 10 minutes",Toast.LENGTH_LONG).show()
            minutes10 = true
            Thread{Thread.sleep(10000*60)
                if (minutes10) exitProtocol()}.start()
            dialog.dismiss()
        }
        fifteenMinutes?.setOnClickListener {
            timer.setColorFilter(ContextCompat.getColor(this,R.color.green))
            Toast.makeText(this, "EMU player will stop playing in 15 minutes",Toast.LENGTH_LONG).show()
            minutes15 = true
            Thread{Thread.sleep(15000*60)
                if (minutes15) exitProtocol()}.start()
            dialog.dismiss()

        }
        twentyMinutes?.setOnClickListener {
            timer.setColorFilter(ContextCompat.getColor(this,R.color.green))
            Toast.makeText(this, "EMU player will stop playing in 20 minutes",Toast.LENGTH_LONG).show()
            minutes20 = true
            Thread{Thread.sleep(20000*60)
                if (minutes20) exitProtocol()}.start()
            dialog.dismiss()
        }
    }


}