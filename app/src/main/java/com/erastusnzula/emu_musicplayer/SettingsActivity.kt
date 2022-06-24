package com.erastusnzula.emu_musicplayer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {
    private lateinit var teal: ImageView
    private lateinit var purple200: ImageView
    private lateinit var purple500: ImageView
    private lateinit var green: ImageView
    private lateinit var sort: ImageView
    private lateinit var version: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Settings"
        teal = findViewById(R.id.teal200ImageView)
        purple200 = findViewById(R.id.purple200ImageView)
        purple500 = findViewById(R.id.purle500ImageView)
        green = findViewById(R.id.greenImageView)
        sort = findViewById(R.id.sortByImageView)
        version = findViewById(R.id.version)

        when (MainActivity.themeIndex) {
            0 -> teal.setBackgroundColor(Color.GRAY)
            1 -> purple200.setBackgroundColor(Color.GRAY)
            2 -> purple500.setBackgroundColor(Color.GRAY)
            3 -> green.setBackgroundColor(Color.GRAY)
        }

        teal.setOnClickListener { changeAppTheme(0) }
        purple200.setOnClickListener { changeAppTheme(1) }
        purple500.setOnClickListener { changeAppTheme(2) }
        green.setOnClickListener { changeAppTheme(3) }
        version.text = setVersion()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeAppTheme(index: Int) {
        if (MainActivity.themeIndex != index) {
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Apply theme")
            dialog.setMessage("Functionality coming soon.")
            dialog.setNegativeButton("No") { dialogDismiss, _ ->
                dialogDismiss.dismiss()
            }
            dialog.setPositiveButton("Yes") { _, _ ->
                //exitProtocol()
            }
            //dialog.show()
        }
    }

    private fun setVersion(): String {
        return "Version : ${BuildConfig.VERSION_NAME}"
    }
}