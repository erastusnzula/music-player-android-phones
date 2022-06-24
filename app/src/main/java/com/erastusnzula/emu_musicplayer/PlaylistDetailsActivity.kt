package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder

class PlaylistDetailsActivity : AppCompatActivity() {
    companion object{
        var currentPlaylistPosition=-1
        var totalSongCurrent = 0
    }
    private lateinit var playlistDetailsRecyclerView: RecyclerView
    private lateinit var playlistTotal:TextView
    private lateinit var playlistCreatedOn:TextView
    private lateinit var playlistName:TextView
    private lateinit var playlistDetailsImageView: ImageView
    private lateinit var playlistCreatedBy:TextView
    private lateinit var playlistAddButton: Button
    private lateinit var playlistRemoveButton: Button
    lateinit var adapter: MusicRecyclerAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_playlist_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Playlist Details"
        currentPlaylistPosition=intent.extras?.get("index") as Int
        playlistDetailsRecyclerView=findViewById(R.id.playlistDetailsRecyclerView)
        playlistTotal=findViewById(R.id.playlistTotalSongs)
        playlistCreatedOn = findViewById(R.id.playlistCreatedOn)
        playlistName=findViewById(R.id.playlistDetailsTitle)
        playlistDetailsImageView = findViewById(R.id.playlistDetailsImageView)
        playlistCreatedBy=findViewById(R.id.playlistCreatedBy)
        playlistAddButton =findViewById(R.id.addToPlaylist)
        playlistRemoveButton = findViewById(R.id.removeFromPlatlist)
        playlistAddButton.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }
        playlistRemoveButton.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(" Remove all songs")
            alert.setMessage(
                "Do you want to remove all songs ?"
            )
            alert.setIcon(R.drawable.music_player_icon)
            alert.setNegativeButton("No"){dialog,_->
                dialog.dismiss()
            }
            alert.setPositiveButton("Yes") { dialog, _ ->
                PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].playlist.clear()
                playlistTotal.text = "Total Songs: ${adapter.itemCount}"
                adapter.refreshPlaylist()
                dialog.dismiss()
            }
            alert.show()
        }

        playlistDetailsRecyclerView.setItemViewCacheSize(20)
        playlistDetailsRecyclerView.setHasFixedSize(true)
        PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].playlist.sortBy { it.title }
        PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].playlist.distinct()
        adapter=MusicRecyclerAdapter(this, PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].playlist,playlistDetailsActivity = true)
        playlistDetailsRecyclerView.adapter=adapter
        totalSongCurrent=adapter.itemCount
        playlistDetailsRecyclerView.layoutManager=LinearLayoutManager(this)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playlistName.text = "Playlist : " +PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].name
        playlistTotal.text = "Total Songs: ${adapter.itemCount}"
        playlistCreatedOn.text = "Created on: ${PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].createdOn}"
        playlistCreatedBy.text = "Created By: ${PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].createdBy}"
        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.reference[currentPlaylistPosition].playlist[0].art)
                .apply(RequestOptions().placeholder(R.drawable.playlist_music))
                .into(playlistDetailsImageView)
        }
        adapter.notifyDataSetChanged()
        val editor = getSharedPreferences("FAVOURITES",MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPlaylist)
        editor.apply()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
                startActivity(Intent(this, PlaylistActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}