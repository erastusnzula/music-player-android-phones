package com.erastusnzula.emu_musicplayer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {
    private lateinit var version: Button
    private lateinit var sortSongs: Button
    private lateinit var changeTheme : Button
    private lateinit var leaveFeedback: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Settings"
        version = findViewById(R.id.version)
        sortSongs = findViewById(R.id.songsOrder)
        changeTheme = findViewById(R.id.changeTheme)
        leaveFeedback=findViewById(R.id.settingFeedbackBtn)

        sortSongs.setOnClickListener { arrangeSongsInOrder() }
        changeTheme.setOnClickListener {  }
        leaveFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }


        version.text = setVersion()


    }

    private fun arrangeSongsInOrder() {
        val itemList = arrayOf("Song title", "Song size","Recently added")
        var currentOrder = MainActivity.sortOrder
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Songs display order")
        dialog.setPositiveButton("Ok"){dialogDismiss,_->
            val editor = getSharedPreferences("SORT", MODE_PRIVATE).edit()
            editor.putInt("sortOrder", currentOrder)
            editor.apply()
            dialogDismiss.dismiss()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
        dialog.setSingleChoiceItems(itemList,currentOrder){_,which->
            currentOrder=which
        }
        dialog.show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setVersion(): String {
        return "Version : ${BuildConfig.VERSION_NAME}"
    }
}