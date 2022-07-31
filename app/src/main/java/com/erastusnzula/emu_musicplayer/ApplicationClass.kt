package com.erastusnzula.emu_musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass: Application() {
    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "Play"
        const val NEXT="Next"
        const val PREVIOUS = "Previous"
        const val EXIT = "exit"
    }

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,"emu music player",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description="Makes the player controls accessible via the notification bar."
            val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }
}
