package com.shadowquest.audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock audio backend for deterministic headless testing without audio hardware.
 */
public class MockAudioBackend implements AudioBackend {
    private String currentMusicPath;
    private double currentMusicVolume;
    private boolean musicLooping;
    private boolean musicPlaying;
    private boolean musicPaused;

    private String currentNarrationPath;
    private double currentNarrationVolume;
    private boolean narrationPlaying;
    private boolean narrationPaused;

    private final List<String> playedSfx = new ArrayList<>();
    private boolean disposed;

    @Override
    public void playMusic(String resourcePath, double volume, boolean loop) {
        this.currentMusicPath = resourcePath;
        this.currentMusicVolume = volume;
        this.musicLooping = loop;
        this.musicPlaying = (resourcePath != null);
        this.musicPaused = false;
    }

    @Override
    public void pauseMusic() {
        if (musicPlaying) {
            this.musicPaused = true;
        }
    }

    @Override
    public void resumeMusic() {
        if (musicPlaying) {
            this.musicPaused = false;
        }
    }

    @Override
    public void stopMusic() {
        this.musicPlaying = false;
        this.musicPaused = false;
        this.currentMusicPath = null;
    }

    @Override
    public void setMusicVolume(double volume) {
        this.currentMusicVolume = volume;
    }

    @Override
    public void playSfx(String resourcePath, double volume) {
        if (resourcePath != null) {
            playedSfx.add(resourcePath);
        }
    }

    @Override
    public void playNarration(String resourcePath, double volume) {
        this.currentNarrationPath = resourcePath;
        this.currentNarrationVolume = volume;
        this.narrationPlaying = (resourcePath != null);
        this.narrationPaused = false;
    }

    @Override
    public void pauseNarration() {
        if (narrationPlaying) {
            this.narrationPaused = true;
        }
    }

    @Override
    public void resumeNarration() {
        if (narrationPlaying) {
            this.narrationPaused = false;
        }
    }

    @Override
    public void stopNarration() {
        this.narrationPlaying = false;
        this.narrationPaused = false;
        this.currentNarrationPath = null;
    }

    @Override
    public void dispose() {
        stopMusic();
        stopNarration();
        playedSfx.clear();
        disposed = true;
    }

    public String getCurrentMusicPath() { return currentMusicPath; }
    public double getCurrentMusicVolume() { return currentMusicVolume; }
    public boolean isMusicLooping() { return musicLooping; }
    public boolean isMusicPlaying() { return musicPlaying; }
    public boolean isMusicPaused() { return musicPaused; }

    public String getCurrentNarrationPath() { return currentNarrationPath; }
    public double getCurrentNarrationVolume() { return currentNarrationVolume; }
    public boolean isNarrationPlaying() { return narrationPlaying; }
    public boolean isNarrationPaused() { return narrationPaused; }

    public List<String> getPlayedSfx() { return List.copyOf(playedSfx); }
    public void clearPlayedSfx() { playedSfx.clear(); }
    public boolean isDisposed() { return disposed; }
}
