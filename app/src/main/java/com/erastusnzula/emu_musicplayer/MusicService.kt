package com.erastusnzula.emu_musicplayer


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager
    private lateinit var runnable: Runnable
    lateinit var audioBecomingNoisy:BecomingNoisy
    lateinit var intentFilter: IntentFilter

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder

    }

    inner class MyBinder : Binder() {
        fun currentServices(): MusicService {
            return this@MusicService
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseIcon: Int) {

        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val previousIntent = Intent(
            baseContext,
            NotificationBroadCast::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val pendingPreviousIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playIntent =
            Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.PLAY)
        val pendingPlayIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent =
            Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.NEXT)
        val pendingNextIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val exitIntent =
            Intent(baseContext, NotificationBroadCast::class.java).setAction(ApplicationClass.EXIT)
        val pendingExitIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val songArt = getSongArt(PlayerActivity.musicList[PlayerActivity.songPosition].path)
        val largeIcon = if (songArt != null) {
            BitmapFactory.decodeByteArray(songArt, 0, songArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon)
        }


        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
        notification.setContentIntent(contentIntent)
        notification.setSilent(true)
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notification.setContentTitle(PlayerActivity.musicList[PlayerActivity.songPosition].title)
        notification.setContentText(PlayerActivity.musicList[PlayerActivity.songPosition].artist)
        notification.setSmallIcon(R.drawable.ic_song_icon)
        notification.setLargeIcon(largeIcon)
        notification.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.sessionToken)
        )
        notification.priority = NotificationCompat.PRIORITY_HIGH
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notification.setOnlyAlertOnce(true)
        notification.addAction(
            R.drawable.ic_baseline_skip_previous_24,
            "Previous",
            pendingPreviousIntent
        )
        notification.addAction(playPauseIcon, "Play", pendingPlayIntent)
        notification.addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingNextIntent)
        notification.addAction(R.drawable.ic_baseline_cancel_24, "Exit", pendingExitIntent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playBackSpeed = if (PlayerActivity.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playBackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val newPlayBackState = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playBackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(newPlayBackState)
                }
            })

        }
        startForeground(7, notification.build())


    }

    fun serviceCreateMediaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer =
                MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicList[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.isPlaying = true
            PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
            PlayerActivity.songStartTime.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.songEndTime.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.seekBar.progress = 0
            PlayerActivity.seekBar.max = mediaPlayer!!.duration
            PlayerActivity.currentPlayingID =
                PlayerActivity.musicList[PlayerActivity.songPosition].id
//            audioBecomingNoisy = BecomingNoisy()
//            intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        } catch (e: Exception) {
            return
        }
    }

    fun runnableSeekBar() {
        runnable = Runnable {
            PlayerActivity.songStartTime.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }


    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            PlayerActivity.isPlaying = false
            PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            mediaPlayer!!.pause()
            showNotification(R.drawable.ic_baseline_play_circle_filled_24)
            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)

     }
 //       else {
//            PlayerActivity.isPlaying = true
//            mediaPlayer!!.start()
//            PlayerActivity.activePlayButton.setImageResource(R.drawable.ic_baseline_pause_24)
//            showNotification(R.drawable.ic_baseline_pause_24)
//            CurrentPlayingFragment.playButtonF.setImageResource(R.drawable.ic_baseline_pause_24)
//        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


}