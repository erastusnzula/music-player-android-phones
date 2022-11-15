package com.erastusnzula.emu_musicplayer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationBroadCast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS->{
                previousNextSong(increment = false, context = context!!)
            }
            ApplicationClass.PLAY->{
                if (PlayerActivity.isPlaying){
                    pauseSong()
                }else{
                    playSong()
                }
            }
            ApplicationClass.NEXT-> {
                previousNextSong(increment = true, context=context!!)
            }
            ApplicationClass.EXIT-> {
                exitProtocol()
            }
            AudioManager.ACTION_AUDIO_BECOMING_NOISY->{
                Toast.makeText(context, "becoming noisy", Toast.LENGTH_SHORT).show()
                pauseSong()
            }
        }
    }

    private fun playSong(){
        try {
            PlayerActivity.isPlaying = true
            PlayerActivity.musicService!!.mediaPlayer!!.start()
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
            PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
            try {
                CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
            } catch (e: Exception) {
            }
        }catch (e:Exception){}
    }

    private fun pauseSong(){
        PlayerActivity.isPlaying=false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_circle_filled_24)
        PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        try {
            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        }catch(e:Exception){}
    }

    private fun previousNextSong(increment: Boolean, context: Context){
        setSongPosition(increment=increment)
        PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
        CurrentPlayingFragment.currentSong.text=PlayerActivity.musicList[PlayerActivity.songPosition].title

        try {
            Glide.with(context)
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(PlayerActivity.activeAlbum)

            PlayerActivity.activeSongName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            PlayerActivity.songEndTime.text = formatDuration(PlayerActivity.musicList[PlayerActivity.songPosition].duration)
            PlayerActivity.musicService!!.serviceCreateMediaPlayer()
            playSong()
            PlayerActivity.favouriteIndex= checkIfIsFavourite(PlayerActivity.musicList[PlayerActivity.songPosition].id)
            if (PlayerActivity.isFavourite) {
                PlayerActivity.favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite)
            }else{
                PlayerActivity.favouriteImageButton.setImageResource(R.drawable.ic_active_player_favourite_borderless)
            }
        } catch (e: Exception) {}
    }
}