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
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MusicRecyclerAdapter
    private val viewModel: MainScreenLoading by viewModels()
    private lateinit var navigationView: NavigationView
    private var exitDelay: Long = 0


    companion object {
        var musicList: ArrayList<MusicFile> = ArrayList()
        var orientation = Configuration.ORIENTATION_PORTRAIT
        var sortOrder = 0
        var orderList = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC",
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
        lateinit var currentPlayingFrag:FragmentContainerView

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }

        setContentView(R.layout.activity_main)
        supportActionBar?.title = "songs"
        recyclerView = findViewById(R.id.recyclerView)
        navigationView = findViewById(R.id.navigationView)
        drawer = findViewById(R.id.drawer)
        currentPlayingFrag = findViewById(R.id.currentPlaying)
        toggle = ActionBarDrawerToggle(this@MainActivity, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermissions()) {
            setupInitialization()
            FavouriteActivity.favouriteSongsList = ArrayList()
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val tokenType = object : TypeToken<ArrayList<MusicFile>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<MusicFile> =
                    GsonBuilder().create().fromJson(jsonString, tokenType)
                FavouriteActivity.favouriteSongsList.addAll(data)
            }

            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("PlaylistSongs", null)
            if (jsonStringPlaylist != null) {
                val dataPlaylist: MusicPlaylist =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }

        }


        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.go_to_favourites -> {
                    startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))
                }
                R.id.go_to_playlist -> {
                    startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
                }
                R.id.feedback -> {
                    startActivity(Intent(this, FeedbackActivity::class.java))
                }
                R.id.about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
            }
            true
        }


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = Configuration.ORIENTATION_LANDSCAPE

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = Configuration.ORIENTATION_PORTRAIT
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
            //R.id.app_bar_search -> {}
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
                //Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
                exitProtocol()
            }
        } catch (e: Exception) {
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            exitProcess(1)
        }

    }

    override fun onResume() {
        super.onResume()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongsList)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPlaylist)
        editor.apply()
        val sortEditor = getSharedPreferences("SORT", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if (sortOrder != sortValue){
            sortOrder=sortValue
            musicList=getAllAudioFiles()
            adapter.updateMusicList(musicList)
        }
        if(PlayerActivity.musicService != null) currentPlayingFrag.visibility = View.VISIBLE
    }


    @SuppressLint("SetTextI18n")
    private fun setupInitialization() {
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        //musicList.sortBy { it.title }
        val sortEditor = getSharedPreferences("SORT", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)
        musicList = getAllAudioFiles()
        musicList.distinct()
        //supportActionBar?.title="songs: ${musicList.size}"
        adapter = MusicRecyclerAdapter(this, musicList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)


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
            orderList[sortOrder],
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


}