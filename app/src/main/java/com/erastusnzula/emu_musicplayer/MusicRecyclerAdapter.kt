package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MusicRecyclerAdapter(
    private val context: Context,
    private var musicList: ArrayList<MusicFile>,
    private var playlistDetailsActivity: Boolean = false,
    private val selectionActivity: Boolean = false
) :
    RecyclerView.Adapter<MusicRecyclerAdapter.ViewHolder>() {
    private var countSelected = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewLayout =
            LayoutInflater.from(context).inflate(R.layout.single_song_view, parent, false)
        return ViewHolder(viewLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songName: TextView = itemView.findViewById(R.id.singleSongName)
        private val songAlbum: TextView = itemView.findViewById(R.id.singleAlbumName)
        private val duration: TextView = itemView.findViewById(R.id.singleTotalSongLength)
        private val art: ImageView = itemView.findViewById(R.id.singleAlbumImageView)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            songName.text = musicList[position].title
            if (musicList[position].album != "0") {
                songAlbum.text = musicList[position].album
            } else {
                songAlbum.text = "<Unknown>"
            }
            duration.text = formatDuration(musicList[position].duration)
            Glide.with(context)
                .load(musicList[position].art)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon))
                .into(art)
            itemView.setOnClickListener {
                when {
                    musicList[position].id == PlayerActivity.currentPlayingID -> {
                        PlayerActivity.playingFromFavourite = false
                        val intent = Intent(context, PlayerActivity::class.java)
                        intent.putExtra("index", position)
                        intent.putExtra("sameSong", false)
                        intent.putExtra("orientation", MainActivity.orientation)
                        intent.putExtra("class", "MainActivity")
                        ContextCompat.startActivity(context, intent, null)
                    }
                    playlistDetailsActivity -> {
                        val intent = Intent(context, PlayerActivity::class.java)
                        intent.putExtra("index", position)
                        intent.putExtra("orientation", MainActivity.orientation)
                        intent.putExtra("class", "PlaylistDetails")
                        ContextCompat.startActivity(context, intent, null)

                    }
                    selectionActivity -> {
                        if (addSongToPlaylist(musicList[position])) {
                            itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.purple_200))
                            countSelected +=1
                            SelectionActivity.selectionTotalSelected.text=countSelected.toString()


                        }else{
                            itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                        }
                    }
                    else -> {
                        val intent = Intent(context, PlayerActivity::class.java)
                        intent.putExtra("index", position)
                        intent.putExtra("orientation", MainActivity.orientation)
                        intent.putExtra("class", "MusicRecyclerAdapter")
                        ContextCompat.startActivity(context, intent, null)
                    }
                }

            }


        }


    }

    @SuppressLint("SetTextI18n")
    private fun addSongToPlaylist(song: MusicFile): Boolean {
        PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist.forEachIndexed { index, musicFile ->
            if (song.id == musicFile.id) {
                Toast.makeText(context, "Song already added",Toast.LENGTH_LONG).show()
                return false
            }
        }
        SelectionActivity.selectionSubmitButton.setOnClickListener {
            val intent = Intent(context,PlaylistActivity::class.java)
            ContextCompat.startActivity(context,intent,null)
            }
        PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        musicList= ArrayList()
        musicList=PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }

}
