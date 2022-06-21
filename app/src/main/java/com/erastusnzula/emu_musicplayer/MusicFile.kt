package com.erastusnzula.emu_musicplayer

import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class MusicFile(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val art: String
)

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds =
        (TimeUnit.SECONDS.convert(
            duration,
            TimeUnit.MILLISECONDS
        ) - minutes * TimeUnit.SECONDS.convert(
            1,
            TimeUnit.MINUTES
        ))
    return String.format("%02d:%02d", minutes, seconds)


}

fun getSongArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.isShuffle) {
        if (!PlayerActivity.isLooping) {
            if (increment) {
                if (PlayerActivity.musicList.size - 1 == PlayerActivity.songPosition) {
                    PlayerActivity.songPosition = 0
                } else {
                    ++PlayerActivity.songPosition
                }

            } else {
                if (0 == PlayerActivity.songPosition) {
                    PlayerActivity.songPosition = PlayerActivity.musicList.size - 1

                } else {
                    --PlayerActivity.songPosition
                }
            }
        }
    } else {
        PlayerActivity.songPosition = (Math.random() * (PlayerActivity.musicList.size - 1)).toInt()
    }
}

fun exitProtocol() {
    if (PlayerActivity.musicService != null) {
        //PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}


fun checkIfIsFavourite(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongsList.forEachIndexed { index, musicFile ->
        if (id == musicFile.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkSongPath(songs: ArrayList<MusicFile>): ArrayList<MusicFile> {
    try {
        songs.forEachIndexed { index, musicFile ->
            val song = File(musicFile.path)
            if (!song.exists()) {
                songs.removeAt(index)
            }
        }
        return songs
    } catch (e: Exception){}
    return songs
}

class Playlist{
    lateinit var name : String
    lateinit var playlist : ArrayList<MusicFile>
    lateinit var createdBy : String
    lateinit var createdOn : String
}

class MusicPlaylist{
    var reference: ArrayList<Playlist> = ArrayList()
}