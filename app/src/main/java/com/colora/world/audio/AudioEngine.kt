package com.colora.world.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import com.colora.world.data.Settings

/**
 * Local, original audio (tools/audio/synth.py). SFX via SoundPool with a small
 * voice limit and per-id rate limiting (never one sound per stroke point);
 * one looping music track with audio focus. Paused whenever the app is in the
 * background (MainActivity forwards lifecycle).
 */
class AudioEngine(private val ctx: Context, private val settings: Settings) {
    private val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    private val pool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
    private val ids = HashMap<String, Int>()
    private val lastPlayed = HashMap<String, Long>()
    private val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var music: MediaPlayer? = null
    private var musicTrack: String? = null
    private var foreground = false
    private var focus: AudioFocusRequest? = null

    private fun load(name: String): Int? = ids[name] ?: runCatching {
        val dir = if (name.startsWith("music.")) "music" else "sfx"
        ctx.assets.openFd("audio/$dir/$name.ogg").use { pool.load(it, 1) }
    }.getOrNull()?.also { ids[name] = it }

    fun preload(names: List<String>) = names.forEach { load(it) }

    fun sfx(name: String, minGapMs: Long = 90) {
        val v = settings.v
        if (v.muted || v.sfxVolume <= 0f || !foreground) return
        val now = SystemClock.uptimeMillis()
        if (now - (lastPlayed[name] ?: 0) < minGapMs) return
        lastPlayed[name] = now
        val id = load(name) ?: return
        pool.play(id, v.sfxVolume, v.sfxVolume, 1, 0, 1f)
    }

    /** Preset sounds; templates with their own animal call use it. */
    fun toySound(audioSetId: String?, preset: String?) {
        val name = when {
            audioSetId != null && audioSetId.startsWith("sfx.animal.") -> audioSetId
            preset != null -> "sfx.$preset"
            else -> "ui.tap"
        }
        sfx(name, 400)
    }

    fun setMusic(track: String?) {
        musicTrack = track
        applyMusic()
    }

    fun onForeground(fg: Boolean) {
        foreground = fg
        applyMusic()
    }

    fun refresh() = applyMusic()

    private fun applyMusic() {
        val v = settings.v
        val want = musicTrack.takeIf { foreground && !v.muted && v.musicVolume > 0f }
        if (want == null) {
            music?.pause(); abandonFocus(); return
        }
        val mp = music
        if (mp != null && mp.tag == want) {
            mp.setVolume(v.musicVolume * 0.6f, v.musicVolume * 0.6f)
            if (!mp.isPlaying && requestFocus()) mp.start()
            return
        }
        mp?.release(); music = null
        runCatching {
            ctx.assets.openFd("audio/music/$want.ogg").use { fd ->
                MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    isLooping = true
                    prepare()
                    setVolume(v.musicVolume * 0.6f, v.musicVolume * 0.6f)
                    tag = want
                }
            }
        }.getOrNull()?.let { music = it; if (requestFocus()) it.start() }
    }

    private var MediaPlayer.tag: String?
        get() = tags[this]
        set(value) { tags[this] = value }
    private val tags = java.util.WeakHashMap<MediaPlayer, String?>()

    private fun requestFocus(): Boolean {
        if (Build.VERSION.SDK_INT < 26) return true
        val req = focus ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) music?.pause()
                else if (change == AudioManager.AUDIOFOCUS_GAIN && foreground) music?.start()
            }.build().also { focus = it }
        return am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() { if (Build.VERSION.SDK_INT >= 26) focus?.let { am.abandonAudioFocusRequest(it) } }
}
