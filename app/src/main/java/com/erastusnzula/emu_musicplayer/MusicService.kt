package com.erastusnzula.emu_musicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.properties.Delegates

class MusicService : Service(),MediaPlayer.OnCompletionListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    var newSongOnClick =false
    private lateinit var runnable:Runnable


    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder

    }
    override fun onCompletion(mp: MediaPlayer?) {
        PlayerActivity.newSongOnclick =true
        try {
            setSongPosition(increment = true)
            MainActivity.mainCurrent.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            serviceCreateMediaPlayer()
            try {
                currentPlayingSongSetup()
            } catch (e: Exception) {
                return
            }
        }catch (e:Exception){
            playNextSong(increment = true)
        }
    }


    inner class MyBinder : Binder() {
        fun currentServices(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseIcon: Int, playBackSpeed:Float){
        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent=PendingIntent.getActivity(this, 0,intent,0)
        val previousIntent = Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.PREVIOUS)
        val pendingPreviousIntent = PendingIntent.getBroadcast(baseContext,0,previousIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val playIntent = Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.PLAY)
        val pendingPlayIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val nextIntent = Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.NEXT)
        val pendingNextIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val exitIntent = Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.EXIT)
        val pendingExitIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val songArt = getSongArt(PlayerActivity.musicList[PlayerActivity.songPosition].path)
        val largeIcon=if (songArt != null){
            BitmapFactory.decodeByteArray(songArt,0, songArt.size)
        }else{
            BitmapFactory.decodeResource(resources,R.drawable.music_player_icon)
        }


        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(PlayerActivity.musicList[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicList[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.ic_song_icon)
            .setLargeIcon(largeIcon)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1,2)
                .setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous",pendingPreviousIntent)
            .addAction(playPauseIcon,"Play",pendingPlayIntent)
            .addAction(R.drawable.ic_baseline_skip_next_24,"Next",pendingNextIntent)
            .addAction(R.drawable.ic_baseline_cancel_24,"Exit",pendingExitIntent)


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,mediaPlayer!!.currentPosition.toLong(), playBackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())

        }
        startForeground(7, notification.build())


    }
    fun serviceCreateMediaPlayer() {
        try {
            when {
                PlayerActivity.musicService!!.mediaPlayer == null -> {
                    servicePlaySong()

                }
                newSongOnClick -> {
                    PlayerActivity.musicService!!.mediaPlayer!!.stop()
                    PlayerActivity.musicService!!.mediaPlayer!!.release()
                    servicePlaySong()

                }
                else -> {
                    return
                }
            }
        } catch (e: Exception) {
            return
        }
    }

    private fun servicePlaySong() {
        PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
        PlayerActivity.musicService!!.mediaPlayer!!.reset()
        PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicList[PlayerActivity.songPosition].path)
        PlayerActivity.musicService!!.mediaPlayer!!.prepare()
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.isPlaying = true
        PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
        MainActivity.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24, 1F)
        PlayerActivity.songStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
        PlayerActivity.songEndTime.text = formatDuration(mediaPlayer!!.duration.toLong())
        PlayerActivity.seekBar.progress=0
        PlayerActivity.seekBar.max= mediaPlayer!!.duration
        PlayerActivity.musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        PlayerActivity.currentPos = PlayerActivity.songPosition

    }

    fun runnableSeekBar(){
        runnable=Runnable{
            PlayerActivity.songStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.seekBar.progress=mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)

        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }


    fun playNextSong(increment: Boolean) {
        if (increment) {
            PlayerActivity.newSongOnclick = true
            setSongPosition(increment = true)
            PlayerActivity.activeSongName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            MainActivity.mainCurrent.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            serviceCreateMediaPlayer()
        } else {
            PlayerActivity.newSongOnclick = true
            setSongPosition(increment = false)
            PlayerActivity.activeSongName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            MainActivity.mainCurrent.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            serviceCreateMediaPlayer()
        }

    }


    fun currentPlayingSongSetup() {
        try {
            Glide.with(this)
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(PlayerActivity.activeAlbum)

            PlayerActivity.activeSongName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            if(PlayerActivity.isLooping)PlayerActivity.repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            if(PlayerActivity.isShuffle)PlayerActivity.shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.green))
        } catch (e: Exception) {
            PlayerActivity.songPosition = PlayerActivity.currentPos
            PlayerActivity.musicList.addAll(MainActivity.musicList)
            Glide.with(this)
                .load(PlayerActivity.musicList[PlayerActivity.songPosition].art)
                .apply(RequestOptions().placeholder(R.drawable.ic_song_icon))
                .into(PlayerActivity.activeAlbum)

            PlayerActivity.activeSongName.text = PlayerActivity.musicList[PlayerActivity.songPosition].title
            if(PlayerActivity.isLooping)PlayerActivity.repeatImageButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            if(PlayerActivity.isShuffle)PlayerActivity.shuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.green))

        }

    }

}