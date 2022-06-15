package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity(),MediaPlayer.OnCompletionListener {
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MusicRecyclerAdapter
    private lateinit var totalSongs: TextView
    private val viewModel: MainScreenLoading by viewModels()
    private lateinit var navigationView: NavigationView
    private var exitDelay: Long = 0


    companion object {
        var musicList: ArrayList<MusicFile> = ArrayList()

        @SuppressLint("StaticFieldLeak")
        lateinit var mainCurrent: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var mainPrevious: ImageButton

        @SuppressLint("StaticFieldLeak")
        lateinit var mainNext: ImageButton

        @SuppressLint("StaticFieldLeak")
        lateinit var playButton: ImageButton

        @SuppressLint("StaticFieldLeak")
        lateinit var mainBottomControls: LinearLayout

        @SuppressLint("StaticFieldLeak")
        lateinit var mainCurrentLayout: LinearLayout


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "All songs"
        recyclerView = findViewById(R.id.recyclerView)
        mainBottomControls = findViewById(R.id.mainBottomControls)
        mainCurrentLayout = findViewById(R.id.mainCurentLayout)
        mainCurrent = findViewById(R.id.mainCurrentPlayingSong)
        mainCurrent.isSelected = true
        mainPrevious = findViewById(R.id.previous)
        mainNext = findViewById(R.id.next)
        totalSongs = findViewById(R.id.mainTotalSongs)
        playButton = findViewById(R.id.playButton)
        navigationView = findViewById(R.id.navigationView)
        drawer = findViewById(R.id.drawer)
        toggle = ActionBarDrawerToggle(this@MainActivity, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermissions()) {
            setupInitialization()
        }
        mainCurrent.setOnClickListener { currentSongClicked() }

        mainNext.setOnClickListener {
            checkPlayNextSong()


        }
        mainPrevious.setOnClickListener {
            checkPlayPreviousSong()
        }

        if (PlayerActivity.isPlaying) {
            try {
                mainCurrent.setTextColor(ContextCompat.getColor(this, R.color.green))
                mainBottomControls.visibility = View.VISIBLE
                mainCurrentLayout.visibility = View.VISIBLE
                mainCurrent.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
                playButton.setImageResource(R.drawable.ic_baseline_pause_24)
                playButton.setOnClickListener {
                    playPauseMode()
                }
            } catch (e: Exception) {
            }

        } else if (PlayerActivity.isPlayerActive) {
            try {
                mainCurrent.text = musicList[PlayerActivity.songPosition].title
                mainBottomControls.visibility = View.VISIBLE
                mainCurrentLayout.visibility = View.VISIBLE
                playButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                mainCurrent.setTextColor(ContextCompat.getColor(this, R.color.green))
                playButton.setOnClickListener {
                    playPauseMode()
                }
            } catch (e: Exception) {
            }
        }


        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> {
                    settingsFunctionalityDialog()
                }
                R.id.go_to_favourites -> {
                    startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))
                }
                R.id.go_to_playlist -> {
                    startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
                }
                R.id.feedback -> {feedBackDialog()}
                R.id.about -> aboutAlertDialog()
            }
            true
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            android.R.id.home -> return true

            R.id.favorite -> {
                startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))

            }
            R.id.playlist -> {
                startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
            }
            R.id.refresh -> {
                setupInitialization()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (exitDelay + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Snackbar.make(drawer, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        exitDelay = System.currentTimeMillis()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
                setupInitialization()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )

            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        PlayerActivity.newSongOnclick =true
        try {
            checkPlayNextSong()
            Toast.makeText(this, "main on complete",Toast.LENGTH_LONG).show()
//            setSongPosition(increment = true)
//            mainCurrent.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
//            PlayerActivity.musicService!!.serviceCreateMediaPlayer()
//            try {
//                PlayerActivity.musicService!!.currentPlayingSongSetup()
//            } catch (e: Exception) {
//                return
//            }
        }catch (e:Exception){
            PlayerActivity.musicService!!.playNextSong(increment = true)
        }
    }

    private fun playPauseMode() {
        try {
            if (PlayerActivity.isPlaying) {
                PlayerActivity.musicService!!.mediaPlayer!!.pause()
                PlayerActivity.isPlaying = false
                playButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24,0F)
            } else {
                PlayerActivity.musicService!!.mediaPlayer!!.start()
                PlayerActivity.musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                PlayerActivity.isPlaying = true
                playButton.setImageResource(R.drawable.ic_baseline_pause_24)
                PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24,1F)
            }
        } catch (e: Exception) {
            return

        }
    }


    private fun currentSongClicked() {
        if (mainCurrent.text.isNotBlank()) {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("sameSong", false)
            intent.putExtra("class", "MainActivity")
            ContextCompat.startActivity(this, intent, null)
        } else {
            return
        }
    }

    private fun checkPlayPreviousSong() {
        if (!PlayerActivity.isShuffle) {
            playPreviousSongMainActivity()
        } else {
            val songPosition = (Math.random() * musicList.size - 1).toInt()
            try {
                PlayerActivity.musicService!!.mediaPlayer!!.stop()
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                mainPlaySong(pos = songPosition)
            } catch (e: Exception) {
                mainPlaySong(pos = songPosition)
            }
            mainCurrent.text = musicList[songPosition].title
        }
    }
    private fun playPreviousSongMainActivity() {
        if (!PlayerActivity.isLooping) {
            val post = if (0 == PlayerActivity.currentPos) musicList.size - 1
            else PlayerActivity.currentPos
            try {
                PlayerActivity.musicService!!.mediaPlayer!!.stop()
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                mainPlaySong(pos = post - 1)
            } catch (e: Exception) {
                mainPlaySong(pos = post - 1)
            }
            mainCurrent.text = musicList[post - 1].title
        }
    }

    private fun checkPlayNextSong() {
        if (!PlayerActivity.isShuffle) {
            playNextSongMainActivity()
        } else {
            val songPosition = (Math.random() * musicList.size - 1).toInt()
            try {
                PlayerActivity.musicService!!.mediaPlayer!!.stop()
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                mainPlaySong(pos = songPosition)
            } catch (e: Exception) {
                mainPlaySong(pos = songPosition)
            }
            mainCurrent.text = musicList[songPosition].title
        }

    }
    private fun playNextSongMainActivity() {
        if (!PlayerActivity.isLooping) {
            val post = if (musicList.size - 1 == PlayerActivity.currentPos) 0
            else PlayerActivity.currentPos
            try {

                PlayerActivity.musicService!!.mediaPlayer!!.stop()
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                mainPlaySong(pos = post + 1)
            } catch (e: Exception) {
                mainPlaySong(pos = post + 1)
            }
            mainCurrent.text = musicList[post + 1].title
        }
    }


    private fun mainPlaySong(pos: Int) {
        PlayerActivity.isPlaying = true
        PlayerActivity.songPosition = pos
        PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
        PlayerActivity.musicService!!.mediaPlayer!!.reset()
        PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(musicList[pos].path)
        PlayerActivity.musicService!!.mediaPlayer!!.prepare()
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24,1F)
        PlayerActivity.musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayerActivity.currentPos = pos
    }

    private fun setupInitialization() {
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        musicList = getAllAudioFiles()
        musicList.sortBy { it.title }
        musicList.distinct()
        adapter = MusicRecyclerAdapter(this, musicList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        totalSongs.text = musicList.size.toString()


    }

    private fun requestRuntimePermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
            return false

        }
        return true
    }

    @SuppressLint("Range")
    private fun getAllAudioFiles(): ArrayList<MusicFile> {
        val allSongsList = ArrayList<MusicFile>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " !=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleCurrent =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idCurrent =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumCurrent =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistCurrent =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathCurrent =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationCurrent =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val art = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                        .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, art).toString()
                    val music = MusicFile(
                        id = idCurrent,
                        title = titleCurrent,
                        album = albumCurrent,
                        artist = artistCurrent,
                        path = pathCurrent,
                        duration = durationCurrent,
                        art = artUri
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        allSongsList.add(music)
                    }

                } while (cursor.moveToNext())
                cursor.close()
            }
        }

        return allSongsList

    }

    private fun aboutAlertDialog() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(" About")
        alert.setMessage(
            "This is a music player made by Erastus Nzula. " +
                    "You can contact me via Twitter: @erastus_nzula " +
                    "or via email: nzulaerastus@gmail.com."
        )
        alert.setIcon(R.drawable.music_player_icon)
        alert.setPositiveButton("Okay") { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()

    }
    private fun settingsFunctionalityDialog() {
        val settingsAlert = AlertDialog.Builder(this)
        settingsAlert.setIcon(R.drawable.music_player_icon)
        settingsAlert.setTitle(" Settings")
        settingsAlert.setMessage("Functionality coming soon.")
        settingsAlert.setPositiveButton("Okay"){dialog,_->
            dialog.dismiss()
        }
        settingsAlert.show()
    }

    private fun feedBackDialog() {
        val feedBackAlert = AlertDialog.Builder(this)
        feedBackAlert.setTitle(" Feedback")
        feedBackAlert.setIcon(R.drawable.music_player_icon)
        feedBackAlert.setMessage("Feedbacks are welcomed.")
        feedBackAlert.setPositiveButton("Okay"){dialog,_->
            dialog.dismiss()
        }
        feedBackAlert.show()
    }


}