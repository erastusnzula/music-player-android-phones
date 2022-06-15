package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
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

class MusicRecyclerAdapter(private val context: Context, private val musicList: ArrayList<MusicFile>) :
    RecyclerView.Adapter<MusicRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewLayout = LayoutInflater.from(context).inflate(R.layout.single_song_view, parent, false)
        return ViewHolder(viewLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(position: Int) {
            val songName = itemView.findViewById<TextView>(R.id.singleSongName)
            val songAlbum = itemView.findViewById<TextView>(R.id.singleAlbumName)
            val duration = itemView.findViewById<TextView>(R.id.singleTotalSongLength)
            val art = itemView.findViewById<ImageView>(R.id.singleAlbumImageView)

            songName.text = musicList[position].title
            if (musicList[position].album != "0"){
                songAlbum.text = musicList[position].album
            }else{
                songAlbum.text = "<Unknown>"
            }

            duration.text = formatDuration(musicList[position].duration)


            Glide.with(context)
                .load(musicList[position].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(art)
            itemView.setOnClickListener{
                //songName.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("index", position)
                intent.putExtra("class", "MusicRecyclerAdapter")
                ContextCompat.startActivity(context, intent, null)

            }


        }


    }

}
