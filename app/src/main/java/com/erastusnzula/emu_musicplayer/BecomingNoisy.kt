package com.erastusnzula.emu_musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class BecomingNoisy: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY){
            PlayerActivity.isPlaying=false
            PlayerActivity.isPlaying=false
            PlayerActivity.musicService!!.mediaPlayer!!.pause()
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24)
            PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            try {
                CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            }catch(e:Exception){}

        }
    }
}