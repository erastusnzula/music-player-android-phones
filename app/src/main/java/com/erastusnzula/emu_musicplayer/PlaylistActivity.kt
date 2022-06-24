package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaylistActivity : AppCompatActivity() {
    companion object{
        var musicPlaylist: MusicPlaylist= MusicPlaylist()

    }
    private lateinit var playlistRecyclerView:RecyclerView
    private lateinit var floatingAddButton: Button
    private lateinit var adapter: PlaylistAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_playlist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="All Playlists"
        playlistRecyclerView = findViewById(R.id.recyclerViewPlaylist)
        floatingAddButton=findViewById(R.id.playlistFloatingActionButton)
        floatingAddButton.setOnClickListener {
            addPlaylistDialog()
        }
        playlistRecyclerView.setHasFixedSize(true)
        playlistRecyclerView.setItemViewCacheSize(20)
        adapter = PlaylistAdapter(this, playList = musicPlaylist.reference)
        playlistRecyclerView.adapter=adapter
        playlistRecyclerView.layoutManager=GridLayoutManager(this, 2)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playlist_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                finish()
            }
            R.id.playlist_favourite->{
                startActivity(Intent(this@PlaylistActivity, FavouriteActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }


    private fun addPlaylistDialog(){
        val dialogLayout = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog, null)
        val playlistName = dialogLayout.findViewById<TextInputEditText>(R.id.playlistEntername)
        val playlistOwner = dialogLayout.findViewById<TextInputEditText>(R.id.playlistOwner)
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setView(dialogLayout)
        dialog.setTitle("Add Playlist")
        dialog.setCancelable(false)
        dialog.setNegativeButton("Cancel"){dialogDismiss,_->
            dialogDismiss.dismiss()
        }
        dialog.setPositiveButton("Add"){dialogDismiss,_->
            val name = playlistName.text
            val owner = playlistOwner.text
            if (name != null && owner !=null){
                addPlaylist(name.toString(), owner.toString())
            }
            dialogDismiss.dismiss()
        }
        dialog.show()


    }

    private fun addPlaylist(name: String, owner: String) {
        var playlistExists = false
        for (i in musicPlaylist.reference){
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if (playlistExists){
            Toast.makeText(this, "Playlist exists", Toast.LENGTH_SHORT).show()
        }else{
            val newPlaylist = Playlist()
            newPlaylist.name = name
            newPlaylist.playlist= ArrayList()
            newPlaylist.createdBy=owner
            val calendar= Calendar.getInstance().time
            val format = SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH)
            newPlaylist.createdOn=format.format(calendar)
            musicPlaylist.reference.add(newPlaylist)
            adapter.playlistRefresh()
        }

    }


}