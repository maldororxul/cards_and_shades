package com.example.cardsandshades.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.edit
import com.example.cardsandshades.R

object SoundManager {
    private var appContext: Context? = null
    private var musicPlayer: MediaPlayer? = null
    private var soundPlayer: MediaPlayer? = null

    private const val PREFS_NAME = "game_prefs"
    private const val KEY_MUSIC_VOL = "music_volume"
    private const val KEY_SOUND_VOL = "sound_volume"

    var musicVolume: Float = 0.8f
    var soundVolume: Float = 0.8f

    private var currentMusicResId: Int? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.8f)
        soundVolume = prefs.getFloat(KEY_SOUND_VOL, 0.8f)
    }

    fun switchMusic(context: Context, resId: Int) {
        // Проверка: если этот трек уже играет, игнорируем вызов
        if (currentMusicResId == resId && musicPlayer?.isPlaying == true) {
            return
        }

        musicPlayer?.stop()
        musicPlayer?.release()

        currentMusicResId = resId
        try {
            musicPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true // Бесконечный повтор для фоновой музыки
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startMusic(context: Context? = null) {
        val targetContext = context ?: appContext ?: return
        val resId = targetContext.resources.getIdentifier("menu_music", "raw", targetContext.packageName)
        if (resId != 0) {
            switchMusic(targetContext, resId)
        }
    }

    fun pauseMusic() {
        musicPlayer?.pause()
    }

    fun resumeMusic() {
        musicPlayer?.start()
    }

    fun updateMusicVolume(context: Context, volume: Float) {
        musicVolume = volume
        musicPlayer?.setVolume(volume, volume)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_MUSIC_VOL, volume) }
    }

    fun updateSoundVolume(context: Context, volume: Float) {
        soundVolume = volume
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_SOUND_VOL, volume) }
    }

    fun playSound(context: Context, resId: Int) {
        try {
            MediaPlayer.create(context, resId).apply {
                setVolume(soundVolume, soundVolume)
                start()
                setOnCompletionListener { it.release() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun playSoundByName(context: Context? = null, soundName: String) {
        val targetContext = context ?: appContext ?: return
        val resId = targetContext.resources.getIdentifier(soundName, "raw", targetContext.packageName)
        if (resId != 0) {
            playSound(targetContext, resId)
        }
    }

    fun playMusicByName(context: Context? = null, musicResName: String) {
        val targetContext = context ?: appContext ?: return
        val musicResId = targetContext.resources.getIdentifier(musicResName, "raw", targetContext.packageName)
        if (musicResId != 0) {
            switchMusic(targetContext, musicResId)
        } else {
            startMusic(targetContext)
        }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
        currentMusicResId = null
    }
}
