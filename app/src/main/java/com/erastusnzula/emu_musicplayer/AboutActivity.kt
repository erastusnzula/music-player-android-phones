package com.erastusnzula.emu_musicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    private lateinit var about: TextView
    private lateinit var feedbackButton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="About"
        about=findViewById(R.id.aboutTextView)
        feedbackButton=findViewById(R.id.goToFeedback)
        about.text=aboutText()

        feedbackButton.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun aboutText(): String {
        return "Developed by Erastus Nzula" +
                "\n\nYour feedback and suggestions will be highly appreciated."
    }
}