package com.erastusnzula.emu_musicplayer

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class FavouriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_fovourite)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Favourite Songs"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                finish()
            }

            R.id.favourite_playlist ->{
                startActivity(Intent(this@FavouriteActivity, PlaylistActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}