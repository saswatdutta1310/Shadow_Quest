package com.shadowquest.audio;

/**
 * Pluggable backend for audio playback, allowing deterministic mocking in tests
 * and safe execution without requiring audio hardware.
 */
public interface AudioBackend {
    void playMusic(String resourcePath, double volume, boolean loop);
    void pauseMusic();
    void resumeMusic();
    void stopMusic();
    void setMusicVolume(double volume);

    void playSfx(String resourcePath, double volume);

    void playNarration(String resourcePath, double volume);
    void pauseNarration();
    void resumeNarration();
    void stopNarration();

    void dispose();
}
