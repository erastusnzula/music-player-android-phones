package com.erastusnzula.emu_musicplayer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {
    private lateinit var version: Button
    private lateinit var sortSongs: Button
    private lateinit var leaveFeedback: Button
    private lateinit var purpleBlueTheme: LinearLayout
    private lateinit var tealWhiteTheme: LinearLayout
    private lateinit var blueWhiteTheme: LinearLayout
    private lateinit var blackWhiteTheme: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Settings"
        version = findViewById(R.id.version)
        sortSongs = findViewById(R.id.songsOrder)
        leaveFeedback=findViewById(R.id.settingFeedbackBtn)

        purpleBlueTheme = findViewById(R.id.purpleBlueTheme)
        tealWhiteTheme = findViewById(R.id.tealWhiteTheme)
        blackWhiteTheme = findViewById(R.id.blackWhiteTheme)
        blueWhiteTheme = findViewById(R.id.blueTheme)


        sortSongs.setOnClickListener { arrangeSongsInOrder() }
        leaveFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
        version.text = setVersion()

        blueWhiteTheme.setOnClickListener { changeTheme() }
        blackWhiteTheme.setOnClickListener { changeTheme() }
        tealWhiteTheme.setOnClickListener { changeTheme() }
        purpleBlueTheme.setOnClickListener { changeTheme() }
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
            Toast.makeText(this, "song rearranged successfully.", Toast.LENGTH_LONG).show()
            //finish()
            //startActivity(Intent(this, MainActivity::class.java))
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

    private fun changeTheme(){
        val themeDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Themes")
            .setMessage("Theme change functionality coming soon.")
            .setPositiveButton("Ok"){dismiss,_->
                dismiss.dismiss()
            }
            .create()
        themeDialog.show()
        themeDialog.setIcon(R.drawable.custom_icon)
        themeDialog.setCancelable(false)
        themeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.GREEN)
    }
}