package com.shadowquest.audio;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central audio coordinator for Shadow Quest: The Lost Realm.
 * Manages background music, voice narration, sound effects, volume channels,
 * global mute, cooldown throttling, and resource lifecycles.
 */
public class AudioManager {
    private static AudioManager instance;

    public static final double DEFAULT_MUSIC_VOLUME = 0.70;
    public static final double DEFAULT_SFX_VOLUME = 0.85;
    private static final long DEFAULT_SFX_THROTTLE_MS = 60; // Minimum interval between identical SFX

    private AudioBackend backend;
    private final NarrationManager narrationManager;
    private final Map<SoundEffect, Long> lastSfxTimes = new EnumMap<>(SoundEffect.class);

    private double musicVolume = DEFAULT_MUSIC_VOLUME;
    private double sfxVolume = DEFAULT_SFX_VOLUME;
    private boolean muted = false;
    private double unmutedMusicVolume = DEFAULT_MUSIC_VOLUME;
    private double unmutedSfxVolume = DEFAULT_SFX_VOLUME;

    private AudioTrack currentTrack = null;
    private boolean musicLooping = true;
    private boolean musicPaused = false;

    public AudioManager() {
        this(new JavaFXAudioBackend());
    }

    public AudioManager(AudioBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
        this.narrationManager = new NarrationManager(this.backend);
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public static synchronized void setInstance(AudioManager newInstance) {
        if (instance != null && instance != newInstance) {
            instance.dispose();
        }
        instance = newInstance;
    }

    public synchronized void setBackend(AudioBackend backend) {
        if (this.backend != null && this.backend != backend) {
            this.backend.dispose();
        }
        this.backend = Objects.requireNonNull(backend, "backend");
    }

    public synchronized AudioBackend getBackend() {
        return backend;
    }

    // --- Volume & Mute Controls ---

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (!muted) {
            this.unmutedMusicVolume = this.musicVolume;
            backend.setMusicVolume(this.musicVolume);
        }
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        if (!muted) {
            this.unmutedSfxVolume = this.sfxVolume;
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean mute) {
        if (this.muted == mute) {
            return;
        }
        this.muted = mute;
        if (muted) {
            this.unmutedMusicVolume = (musicVolume > 0 ? musicVolume : DEFAULT_MUSIC_VOLUME);
            this.unmutedSfxVolume = (sfxVolume > 0 ? sfxVolume : DEFAULT_SFX_VOLUME);
            backend.setMusicVolume(0.0);
        } else {
            this.musicVolume = this.unmutedMusicVolume;
            this.sfxVolume = this.unmutedSfxVolume;
            backend.setMusicVolume(this.musicVolume);
        }
    }

    public void toggleMute() {
        setMuted(!muted);
    }

    // --- Music Coordination ---

    public void playMusic(AudioTrack track) {
        playMusic(track, true);
    }

    public void playMusic(AudioTrack track, boolean loop) {
        if (track == null) {
            stopMusic();
            return;
        }
        if (currentTrack == track && !musicPaused) {
            return; // Already playing requested track
        }

        this.currentTrack = track;
        this.musicLooping = loop;
        this.musicPaused = false;
        double effectiveVolume = muted ? 0.0 : musicVolume;
        backend.playMusic(track.getResourcePath(), effectiveVolume, loop);
    }

    public void pauseMusic() {
        if (currentTrack != null && !musicPaused) {
            musicPaused = true;
            backend.pauseMusic();
        }
    }

    public void resumeMusic() {
        if (currentTrack != null && musicPaused) {
            musicPaused = false;
            backend.resumeMusic();
        }
    }

    public void stopMusic() {
        currentTrack = null;
        musicPaused = false;
        backend.stopMusic();
    }

    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    public boolean isMusicPaused() {
        return musicPaused;
    }

    // --- Sound Effects Coordination ---

    public void playSfx(SoundEffect sfx) {
        if (sfx == null || muted || sfxVolume <= 0.0) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastTime = lastSfxTimes.get(sfx);
        if (lastTime != null && (now - lastTime) < DEFAULT_SFX_THROTTLE_MS) {
            return; // Throttled to prevent ear-piercing stacking
        }
        lastSfxTimes.put(sfx, now);

        backend.playSfx(sfx.getResourcePath(), sfxVolume);
    }

    // --- Voice Narration Coordination ---

    public void playNarration(int sceneIndex) {
        if (muted) {
            return;
        }
        narrationManager.playScene(sceneIndex, sfxVolume);
    }

    public void pauseNarration() {
        narrationManager.pause();
    }

    public void resumeNarration() {
        if (!muted) {
            narrationManager.resume();
        }
    }

    public void stopNarration() {
        narrationManager.stop();
    }

    public NarrationManager getNarrationManager() {
        return narrationManager;
    }

    // --- Lifecycle Management ---

    public void pauseAll() {
        pauseMusic();
        pauseNarration();
    }

    public void resumeAll() {
        resumeMusic();
        resumeNarration();
    }

    public void stopAll() {
        stopMusic();
        stopNarration();
    }

    public void resetForLevel() {
        stopNarration();
        lastSfxTimes.clear();
    }

    public void resetForMainMenu() {
        stopNarration();
        lastSfxTimes.clear();
        playMusic(AudioTrack.MENU, true);
    }

    public void dispose() {
        stopAll();
        lastSfxTimes.clear();
        backend.dispose();
    }
}
