package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectionActivity : AppCompatActivity() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var selectionSubmitButton :Button

        @SuppressLint("StaticFieldLeak")
        lateinit var selectionTotalSelected : TextView
    }
    private lateinit var selectionRecyclerView: RecyclerView

    private lateinit var adapter:MusicRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Select Songs"

        selectionRecyclerView=findViewById(R.id.selectionRecyclerView)
        selectionTotalSelected=findViewById(R.id.selecetedSongs)
        selectionSubmitButton=findViewById(R.id.selectedSubmit)

        selectionRecyclerView.setHasFixedSize(true)
        selectionRecyclerView.setItemViewCacheSize(20)
        adapter = MusicRecyclerAdapter(this,MainActivity.musicList, selectionActivity = true)
        selectionRecyclerView.adapter=adapter
        selectionRecyclerView.layoutManager=LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}