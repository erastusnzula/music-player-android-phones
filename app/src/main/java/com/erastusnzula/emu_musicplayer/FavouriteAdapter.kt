package com.erastusnzula.emu_musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.math.min

class FavouriteAdapter(private var context: Context, private var musicList: ArrayList<MusicFile>) :
    RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {
    companion object {
        const val margin = 10
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.favourite_single_view, parent, false)
//        val width = parent.width/2 - (2* margin)
//        val height = parent.height/2 - (2* margin)
//        val size = min(width, height)
//        val params = view.layoutParams as ViewGroup.MarginLayoutParams
//        params.width = size
//        params.height=size
//        params.setMargins(margin, margin, margin, margin)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favouriteImageView: ImageView =
            itemView.findViewById(R.id.favouriteSingleImageView)
        private val favoriteSongName: TextView = itemView.findViewById(R.id.favouriteSingleSongName)
        private val favouriteArtist: TextView = itemView.findViewById(R.id.favouriteArtist)
        private val favouriteSongDuration: TextView =
            itemView.findViewById(R.id.favouriteSongDuration)

        fun bind(position: Int) {
            favoriteSongName.text = musicList[position].title
            favouriteArtist.text = musicList[position].artist
            favouriteSongDuration.text = formatDuration(musicList[position].duration)
            Glide.with(context)
                .load(musicList[position].art)
                .apply(RequestOptions().placeholder(R.drawable.music))
                .into(favouriteImageView)
            itemView.setOnClickListener {
                if (musicList[position].id == PlayerActivity.currentPlayingID) {
                    PlayerActivity.playingFromFavourite = true
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("index", position)
                    intent.putExtra("sameSong", false)
                    intent.putExtra("orientation", FavouriteActivity.favoriteOrientation)
                    intent.putExtra("class", "MainActivity")
                    ContextCompat.startActivity(context, intent, null)
                } else {
                    PlayerActivity.audioContinue = true
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("index", position)
                    intent.putExtra("orientation", FavouriteActivity.favoriteOrientation)
                    intent.putExtra("class", "FavouriteAdapter")
                    ContextCompat.startActivity(context, intent, null)
                }
            }

        }
    }

}
