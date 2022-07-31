package com.erastusnzula.emu_musicplayer

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavouriteActivity : AppCompatActivity() {
    companion object{
        var favouriteSongsList:ArrayList<MusicFile> = ArrayList()
        var favoriteOrientation = Configuration.ORIENTATION_PORTRAIT
    }
    private lateinit var favouriteRecyclerView: RecyclerView
    private lateinit var favouriteFloatingButton: Button
    private lateinit var favouriteInstructions: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_fovourite)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favourite Songs"
        favouriteRecyclerView = findViewById(R.id.favouriteRecyclerView)
        favouriteFloatingButton=findViewById(R.id.favouriteFloatingActionButton)
        favouriteInstructions=findViewById(R.id.favouriteInstructions)
        favouriteRecyclerView.setItemViewCacheSize(20)
        favouriteRecyclerView.setHasFixedSize(true)
        favouriteSongsList= checkSongPath(songs = favouriteSongsList)
        favouriteSongsList.sortBy { it.title }
        favouriteSongsList.distinct()
        favouriteRecyclerView.adapter = FavouriteAdapter(this, favouriteSongsList)
        favouriteRecyclerView.layoutManager =LinearLayoutManager(this)
        if (favouriteSongsList.size <1) favouriteFloatingButton.visibility= View.INVISIBLE
        if(favouriteSongsList.isNotEmpty()) favouriteInstructions.visibility=View.GONE
        favouriteFloatingButton.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteShuffle")
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            R.id.favourite_playlist -> {
                startActivity(Intent(this@FavouriteActivity, PlaylistActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            favoriteOrientation = Configuration.ORIENTATION_LANDSCAPE

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            favoriteOrientation =Configuration.ORIENTATION_PORTRAIT

        }
    }

}