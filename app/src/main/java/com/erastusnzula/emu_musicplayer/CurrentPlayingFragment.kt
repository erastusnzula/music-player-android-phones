package com.erastusnzula.emu_musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CurrentPlayingFragment : Fragment() {

   companion object{
       @SuppressLint("StaticFieldLeak")
       lateinit var playButtonF: ImageButton
       @SuppressLint("StaticFieldLeak")
       lateinit var nextButtonF:ImageButton
       @SuppressLint("StaticFieldLeak")
       lateinit var currentSong:TextView
       @SuppressLint("StaticFieldLeak")
       lateinit var constrainedLayout: ConstraintLayout
       @SuppressLint("StaticFieldLeak")
       lateinit var currentPlayingImageView:ImageView
   }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =inflater.inflate(R.layout.fragment_current_playing, container, false)
        playButtonF = view.findViewById(R.id.currentPlayingPlay)
        nextButtonF = view.findViewById(R.id.currentPlayingNext)
        currentSong=view.findViewById(R.id.currentPlayingTextView)
        constrainedLayout=view.findViewById(R.id.currentPlayingConstrained)
        currentPlayingImageView=view.findViewById(R.id.currentPlayingImageView)
        constrainedLayout.visibility = View.INVISIBLE

        playButtonF.setOnClickListener { playPauseControl() }
        nextButtonF.setOnClickListener {  playNext()}
        constrainedLayout.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("orientation", MainActivity.orientation)
            intent.putExtra("class", "currentPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            constrainedLayout.visibility = View.VISIBLE
            currentSong.isSelected = true
            Glide.with(requireContext())
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
                .into(currentPlayingImageView)
            currentSong.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            if(PlayerActivity.isPlaying) playButtonF.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            else playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        }
    }

    private fun playNext() {
        Glide.with(requireContext())
            .load(PlayerActivity.musicList[PlayerActivity.songPosition].art)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
            .into(currentPlayingImageView)
        setSongPosition(increment = true)
        currentSong.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        PlayerActivity.musicService!!.serviceCreateMediaPlayer()
        playSong()
    }

    private fun playPauseControl() {
        if (PlayerActivity.isPlaying) {
            pauseSong()
        } else {
            playSong()
        }
    }

    private fun playSong() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
    }

    private fun pauseSong() {
        PlayerActivity.isPlaying = false
        PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24)
        playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
    }
}

