package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        var musicList: ArrayList<MusicFile> = ArrayList()
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var isLooping: Boolean = false
        var musicService: MusicService? = null
        var isPlayerActive = false
        var isShuffle = false
        var isTimerSet = false
        var currentPlayingID: String = ""
        var isFavourite = false
        var favouriteIndex = -1
        var playingFromFavourite = false
        var playingFromPlaylist = false

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

        @SuppressLint("StaticFieldLeak")
        lateinit var favouriteImageButton: ImageButton

    }

    private lateinit var nextSong: ImageButton
    private lateinit var previousSong: ImageButton
    private lateinit var equalizer: ImageButton
    private lateinit var timer: ImageButton
    private lateinit var shareImageButton: ImageButton
    private var directPlay=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_player)
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
        shareImageButton = findViewById(R.id.activeShareImageButton)
        favouriteImageButton = findViewById(R.id.fav)
        if (intent.data?.scheme.contentEquals("content")) {
            if (requestRuntimePermissions()) {
                directPlay=true
                playFromIntentAction()
            }

        } else {
            directPlay=false
            setUpInitialization()
        }


        activePlayButton.setOnClickListener { playPauseControl() }
        nextSong.setOnClickListener { playNextSong(increment = true) }
        previousSong.setOnClickListener { playNextSong(increment = false) }
        shuffleButton.setOnClickListener { shuffle() }
        equalizer.setOnClickListener { equalizerIntent() }
        repeatImageButton.setOnClickListener { repeatControl() }
        shareImageButton.setOnClickListener { shareMusicFile() }
        timer.setOnClickListener {
            if (!isTimerSet) {
                closeAppTimer()
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Cancel music timer?")
                dialog.setPositiveButton("Yes") { _, _ ->
                    isTimerSet = false
                    timer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                }
                dialog.show()
            }

        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if(isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_circle_filled_24)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        favouriteImageButton.setOnClickListener {
            try {
                if (isFavourite) {
                    isFavourite = false
                    favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite_borderless)
                    FavouriteActivity.favouriteSongsList.remove(musicList[songPosition])
                } else {
                    isFavourite = true
                    favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite)
                    FavouriteActivity.favouriteSongsList.add(musicList[songPosition])
                    Toast.makeText(this, "Successfully added to favourites", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                return@setOnClickListener
            }

        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (directPlay){
                    finish()
                }else {
                    try {
                        finish()
                    }catch (e: Exception){
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentServices()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        createMediaPlayer()
        musicService!!.runnableSeekBar()
        isPlayerActive = true

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        try {
            setSongPosition(increment = true)
            createMediaPlayer()
            currentPlayingSongSetup()
            CurrentPlayingFragment.currentSong.isSelected = true
            Glide.with(applicationContext)
                .load(musicList[songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.custom_icon).centerCrop())
                .into(CurrentPlayingFragment.currentPlayingImageView)
            CurrentPlayingFragment.currentSong.text = musicList[songPosition].title
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
                playFromIntentAction()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicList[songPosition].id == "Unknown" && !isPlaying) exitProtocol()
    }

    private fun currentPlayingSongSetup() {
        try {

            Glide.with(this)
                .load(musicList[songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(activeAlbum)

            activeSongName.text = musicList[songPosition].title
            if (isLooping) repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            if (isShuffle) shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.orange))
            if (isTimerSet) timer.setColorFilter(ContextCompat.getColor(this, R.color.purple_700))
            favouriteIndex = checkIfIsFavourite(musicList[songPosition].id)
            if (isFavourite) {
                favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite)
            } else {
                favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite_borderless)
            }

        } catch (e: Exception) {
        }

    }

    private fun setUpInitialization() {
        songPosition = intent.getIntExtra("index", 0)

        when (intent.getStringExtra("class")) {
            "currentPlaying" -> {
                val orientation =
                    intent.getIntExtra("orientation", Configuration.ORIENTATION_PORTRAIT)
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    albumLayout.visibility = View.GONE
                }
                currentPlayingSongSetup()
                //activeSongName.text = musicList[songPosition].title
                songStartTime.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                songEndTime.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                seekBar.progress = 0
                seekBar.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                if (isPlaying) activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
                else activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            }
            "FavouriteAdapter" -> {
                val orientation =
                    intent.getIntExtra("orientation", Configuration.ORIENTATION_PORTRAIT)
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    albumLayout.visibility = View.GONE
                }
                intentContentInitialization(
                    playList = FavouriteActivity.favouriteSongsList,
                    fromFavourite = true
                )
            }
            "MusicRecyclerAdapter" -> {
                val orientation =
                    intent.getIntExtra("orientation", Configuration.ORIENTATION_PORTRAIT)
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    albumLayout.visibility = View.GONE
                }
                intentContentInitialization(
                    playList = MainActivity.musicList
                )
            }
            "FavouriteShuffle" -> {
                val orientation =
                    intent.getIntExtra("orientation", Configuration.ORIENTATION_PORTRAIT)
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    albumLayout.visibility = View.GONE
                }
                intentContentInitialization(
                    playList = FavouriteActivity.favouriteSongsList,
                    shuffleList = true,
                    fromFavourite = true
                )


            }
            "PlaylistDetails" -> {
                val orientation =
                    intent.getIntExtra("orientation", Configuration.ORIENTATION_PORTRAIT)
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    albumLayout.visibility = View.GONE
                }
                intentContentInitialization(
                    playList = PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist,
                    fromPlaylist = true
                )

            }

        }

    }

    private fun intentContentInitialization(
        playList: ArrayList<MusicFile>,
        fromFavourite: Boolean = false,
        fromPlaylist: Boolean = false,
        shuffleList: Boolean = false
    ) {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicList = ArrayList()
        musicList.addAll(playList)
        if (shuffleList) musicList.shuffle()
        playingFromFavourite = fromFavourite
        playingFromPlaylist = fromPlaylist
        currentPlayingSongSetup()
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicList[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            songStartTime.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            songEndTime.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            seekBar.progress = 0
            seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            currentPlayingID = musicList[songPosition].id
            playSong()

        } catch (e: Exception) {
            return
        }
    }


    private fun playPauseControl() {
        if (isPlaying) {
            pauseSong()
        } else {
            playSong()
        }
    }

    private fun playSong() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        try {
            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
        }catch (e:Exception){}
    }

    private fun pauseSong() {
        isPlaying = false
        activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24)
        try {
            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        }catch (e:Exception){}
    }

    private fun playNextSong(increment: Boolean) {
        try {
            if (increment) {
                setSongPosition(increment = true)
                currentPlayingSongSetup()
                createMediaPlayer()
            } else {
                setSongPosition(increment = false)
                currentPlayingSongSetup()
                createMediaPlayer()
            }
        } catch (e: Exception) {
        }

    }


    private fun shuffle() {
        if (!isShuffle) {
            isShuffle = true
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.orange))
            isLooping = false
            repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_24)
        } else {
            isShuffle = false
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
    }

    private fun repeatControl() {
        if (isLooping) {
            isLooping = false
            repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_24)
        } else {
            isLooping = true
            isShuffle = false
            shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
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
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        dialog.findViewById<Button>(R.id.endSave)?.setOnClickListener {
            val endTime = when (radioGroup?.checkedRadioButtonId) {
                R.id.endIn10 -> 10
                R.id.endIn20 -> 20
                R.id.endIn30 -> 30
                R.id.endIn40 -> 40
                R.id.endIn50 -> 50
                else -> 60
            }
            timer.setColorFilter(ContextCompat.getColor(this, R.color.orange))
            Toast.makeText(this, "Ending in $endTime minutes.", Toast.LENGTH_LONG).show()
            isTimerSet = true
            Thread {
                Thread.sleep((60000 * endTime).toLong())
                if (isTimerSet) exitProtocol()
            }.start()
            dialog.dismiss()

        }
    }

    private fun shareMusicFile() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicList[songPosition].path))
        startActivity(Intent.createChooser(shareIntent, "Share File"))
    }


    private fun getSongPath(contentUri: Uri): MusicFile {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.TITLE)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val title = titleColumn?.let { cursor.getString(it) }!!
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return MusicFile(
                id = "Unknown",
                title = title,
                album = "Unknown",
                artist = "Unknown",
                duration = duration,
                path = path.toString(),
                art = "Unknown"
            )
        } finally {
            cursor?.close()
        }
    }


    private fun playFromIntentAction() {
        try {
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicList = ArrayList()
            musicList.add(getSongPath(intent.data!!))
            activeSongName.text = musicList[songPosition].title
            try {
                Glide.with(this)
                    .load(getSongArt(musicList[songPosition].path))
                    .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                    .into(activeAlbum)
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun requestRuntimePermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return false

        }
        return true
    }


}