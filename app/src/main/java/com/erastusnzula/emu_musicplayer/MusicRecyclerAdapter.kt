package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

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
        private val root: View = itemView.rootView

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            songName.text = musicList[position].title
            if (musicList[position].album != "0") {
                songAlbum.text = musicList[position].album
            } else {
                songAlbum.text = "<Unknown>"
            }
            duration.text = formatDuration(musicList[position].duration)
            val pathSong = musicList[position].path
            val pos = position
            Glide.with(context)
                .load(musicList[position].art)
                .apply(RequestOptions().placeholder(R.drawable.custom_icon))
                .into(art)

            if (!selectionActivity) {
                root.setOnLongClickListener {
                    val dialog = AlertDialog.Builder(context)
                    dialog.setTitle(" Song Actions")
                    dialog.setIcon(R.drawable.ic_song_icon)
                    dialog.setItems(arrayOf("Remove song","View song Details")){dismiss, which->
                        when (which){
                            0->{
                                musicList.removeAt(pos)
                                Snackbar.make(root, "Removed successfully", 1000).show()
                                notifyItemRangeChanged(0, musicList.size-1)
                                dismiss.dismiss()
                            }
                            1->{
                                val infoLayout =
                                    LayoutInflater.from(context).inflate(R.layout.song_information, null)
                                val name = infoLayout.findViewById<TextView>(R.id.songTitleName)
                                val songDuration = infoLayout.findViewById<TextView>(R.id.songDuration)
                                val location = infoLayout.findViewById<TextView>(R.id.songLocation)

                                val alert = AlertDialog.Builder(context)
                                alert.setView(infoLayout)
                                name.text = name.text.toString() + "\n" + songName.text
                                songDuration.text = songDuration.text.toString() + "\n" + duration.text
                                location.text = location.text.toString() + "\n" + pathSong
                                alert.setPositiveButton("Ok"){d, _->
                                    d.dismiss()
                                    dismiss.dismiss()
                                }
                                alert.show()

                            }
                        }
                    }
                    dialog.show()
                    true

                }

            }

            when {
                playlistDetailsActivity -> {
                    root.setOnClickListener {
                        when (musicList[position].id) {
                            PlayerActivity.currentPlayingID -> {
                                sendIntent("currentPlaying", pos = position)
                            }
                            else -> sendIntent(className = "PlaylistDetails", pos = position)
                        }
                    }
                }
                selectionActivity -> {
                    root.setOnClickListener {
                        if (addSongToPlaylist(musicList[position])) {
                            itemView.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.purple_200
                                )
                            )
                            countSelected += 1
                            SelectionActivity.selectionTotalSelected.text =
                                countSelected.toString()

                        } else {
                            itemView.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.white
                                )
                            )
                        }
                    }
                }
                else -> {
                    root.setOnClickListener {
                        when (musicList[position].id) {
                            PlayerActivity.currentPlayingID -> {
                                sendIntent("currentPlaying", pos = position)
                            }
                            else -> {
                                sendIntent("MusicRecyclerAdapter", pos = position)
                                //songName.setTextColor(ContextCompat.getColor(context,R.color.green))
                            }
                        }

                    }
                }

            }
        }


    }

    private fun sendIntent(className: String, pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("orientation", MainActivity.orientation)
        intent.putExtra("class", className)
        ContextCompat.startActivity(context, intent, null)
    }

    @SuppressLint("SetTextI18n")
    private fun addSongToPlaylist(song: MusicFile): Boolean {
        PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist.forEachIndexed { index, musicFile ->
            if (song.id == musicFile.id) {
                Toast.makeText(context, "Song already added", Toast.LENGTH_LONG).show()
                return false
            }
        }
        SelectionActivity.selectionSubmitButton.setOnClickListener {
            val intent = Intent(context, PlaylistActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
        PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist.add(
            song
        )

        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList =
            PlaylistActivity.musicPlaylist.reference[PlaylistDetailsActivity.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(list: ArrayList<MusicFile>) {
        musicList = ArrayList()
        musicList.addAll(list)
        notifyDataSetChanged()


    }

}
