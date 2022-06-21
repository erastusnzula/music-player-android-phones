package com.erastusnzula.emu_musicplayer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.math.min

class PlaylistAdapter(private var context: Context, private var playList: ArrayList<Playlist>) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    companion object{
        const val margin = 10
        lateinit var playlistTotalSongs: TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val size = parent.width/2-(2* margin)
        val view = LayoutInflater.from(context).inflate(R.layout.playlist_view_single, parent,false)
        val cardLayout = view.findViewById<CardView>(R.id.playlistCardView).layoutParams as ViewGroup.MarginLayoutParams
        cardLayout.width=size
        cardLayout.height=size
        cardLayout.setMargins(margin, margin, margin, margin)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return playList.size
    }

    fun playlistRefresh(){
        playList = ArrayList()
        playList.addAll(PlaylistActivity.musicPlaylist.reference)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val playlistImageView=itemView.findViewById<ImageView>(R.id.playlistImageView)
        private val playlistName: TextView = itemView.findViewById(R.id.playlistName)
        private val deletePlaylist: ImageButton =itemView.findViewById(R.id.deletePlaylist)


        fun bind(position: Int) {
            playlistTotalSongs= itemView.findViewById(R.id.totalSongsCount)
            playlistName.text=playList[position].name
            playlistName.isSelected=true
            deletePlaylist.setOnClickListener {
                val alert=AlertDialog.Builder(context)
                alert.setTitle(playList[position].name)
                alert.setMessage("Do you want to delete ? ")
                alert.setNegativeButton("No"){dialog,_->
                    dialog.dismiss()
                }
                alert.setPositiveButton("Yes"){dialog,_->
                    PlaylistActivity.musicPlaylist.reference.removeAt(position)
                    playlistRefresh()
                    dialog.dismiss()
                }
                alert.show()
            }
            itemView.setOnClickListener {
                val intent = Intent(context, PlaylistDetailsActivity::class.java)
                intent.putExtra("index", position)
                ContextCompat.startActivity(context, intent, null)
            }

            if(PlaylistActivity.musicPlaylist.reference[position].playlist.size > 0){
                Glide.with(context)
                    .load(PlaylistActivity.musicPlaylist.reference[position].playlist[0].art)
                    .apply(RequestOptions().placeholder(R.drawable.music_player))
                    .into(playlistImageView)
            }

        }



    }

}
