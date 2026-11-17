package com.shadowquest.audio;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Standard production AudioBackend using JavaFX MediaPlayer for music/narration
 * and AudioClip for low-latency sound effects, with seamless javax.sound.sampled
 * fallback when JavaFX native media libraries are unavailable or blocked.
 *
 * Implements strict lifecycle management to guarantee voice lines never overlap
 * across scene transitions, skips, or audio pauses.
 */
public class JavaFXAudioBackend implements AudioBackend {
    private MediaPlayer musicPlayer;
    private MediaPlayer narrationPlayer;
    private Clip activeMusicClip;
    private Clip activeNarrationClip;
    private final Map<String, AudioClip> sfxClipCache = new HashMap<>();
    private boolean javaFXMediaAvailable = true;

    private String currentMusicPath;
    private boolean musicLooping = true;
    private double currentMusicVolume = 1.0;

    @Override
    public void playMusic(String resourcePath, double volume, boolean loop) {
        if (resourcePath == null) {
            stopMusic();
            return;
        }

        this.currentMusicPath = resourcePath;
        this.musicLooping = loop;
        this.currentMusicVolume = volume;

        stopMusic();

        if (!javaFXMediaAvailable) {
            playSampledMusic(resourcePath, volume, loop);
            return;
        }

        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                return;
            }

            Media media = new Media(resource.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setVolume(volume);
            musicPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
            musicPlayer.play();
        } catch (Throwable t) {
            if (t instanceof UnsatisfiedLinkError || t instanceof NoClassDefFoundError) {
                javaFXMediaAvailable = false;
            }
            musicPlayer = null;
            playSampledMusic(resourcePath, volume, loop);
        }
    }

    @Override
    public void pauseMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.pause();
            } catch (Throwable ignored) {}
        }
        if (activeMusicClip != null && activeMusicClip.isRunning()) {
            try {
                activeMusicClip.stop();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void resumeMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.play();
            } catch (Throwable ignored) {}
        }
        if (activeMusicClip != null && !activeMusicClip.isRunning()) {
            try {
                activeMusicClip.start();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void stopMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.dispose();
            } catch (Throwable ignored) {
            } finally {
                musicPlayer = null;
            }
        }
        if (activeMusicClip != null) {
            try {
                if (activeMusicClip.isRunning()) {
                    activeMusicClip.stop();
                }
                activeMusicClip.close();
            } catch (Throwable ignored) {
            } finally {
                activeMusicClip = null;
            }
        }
    }

    @Override
    public void setMusicVolume(double volume) {
        this.currentMusicVolume = volume;
        if (musicPlayer != null) {
            try {
                musicPlayer.setVolume(volume);
            } catch (Throwable ignored) {}
        }
        if (activeMusicClip != null && activeMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            try {
                FloatControl gainControl = (FloatControl) activeMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                applyGain(gainControl, volume);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void playSfx(String resourcePath, double volume) {
        if (resourcePath == null || volume <= 0) {
            return;
        }
        if (!javaFXMediaAvailable) {
            playSampledSfx(resourcePath, volume);
            return;
        }
        try {
            AudioClip clip = sfxClipCache.computeIfAbsent(resourcePath, path -> {
                URL resource = getClass().getResource(path);
                if (resource != null) {
                    try {
                        return new AudioClip(resource.toExternalForm());
                    } catch (Throwable ignored) {
                        return null;
                    }
                }
                return null;
            });

            if (clip != null) {
                clip.play(volume);
            } else {
                playSampledSfx(resourcePath, volume);
            }
        } catch (Throwable t) {
            if (t instanceof UnsatisfiedLinkError || t instanceof NoClassDefFoundError) {
                javaFXMediaAvailable = false;
            }
            playSampledSfx(resourcePath, volume);
        }
    }

    @Override
    public void playNarration(String resourcePath, double volume) {
        stopNarration();
        if (resourcePath == null || volume <= 0.0) {
            return;
        }
        if (!javaFXMediaAvailable) {
            playSampledNarration(resourcePath, volume);
            return;
        }
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                return;
            }
            Media media = new Media(resource.toExternalForm());
            narrationPlayer = new MediaPlayer(media);
            narrationPlayer.setVolume(volume);
            narrationPlayer.setCycleCount(1);
            narrationPlayer.play();
        } catch (Throwable t) {
            if (t instanceof UnsatisfiedLinkError || t instanceof NoClassDefFoundError) {
                javaFXMediaAvailable = false;
            }
            narrationPlayer = null;
            playSampledNarration(resourcePath, volume);
        }
    }

    @Override
    public void pauseNarration() {
        if (narrationPlayer != null) {
            try {
                narrationPlayer.pause();
            } catch (Throwable ignored) {}
        }
        if (activeNarrationClip != null && activeNarrationClip.isRunning()) {
            try {
                activeNarrationClip.stop();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void resumeNarration() {
        if (narrationPlayer != null) {
            try {
                narrationPlayer.play();
            } catch (Throwable ignored) {}
        }
        if (activeNarrationClip != null && !activeNarrationClip.isRunning()) {
            try {
                activeNarrationClip.start();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void stopNarration() {
        if (narrationPlayer != null) {
            try {
                narrationPlayer.stop();
                narrationPlayer.dispose();
            } catch (Throwable ignored) {
            } finally {
                narrationPlayer = null;
            }
        }
        if (activeNarrationClip != null) {
            try {
                if (activeNarrationClip.isRunning()) {
                    activeNarrationClip.stop();
                }
                activeNarrationClip.close();
            } catch (Throwable ignored) {
            } finally {
                activeNarrationClip = null;
            }
        }
    }

    private void playSampledMusic(String resourcePath, double volume, boolean loop) {
        try {
            Clip clip = loadClip(resourcePath);
            if (clip == null) return;
            applyGainIfSupported(clip, volume);
            activeMusicClip = clip;
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Throwable ignored) {}
    }

    private void playSampledNarration(String resourcePath, double volume) {
        try {
            Clip clip = loadClip(resourcePath);
            if (clip == null) return;
            applyGainIfSupported(clip, volume);
            activeNarrationClip = clip;
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    try {
                        clip.close();
                    } catch (Throwable ignored) {}
                    if (activeNarrationClip == clip) {
                        activeNarrationClip = null;
                    }
                }
            });
            clip.start();
        } catch (Throwable ignored) {}
    }

    private void playSampledSfx(String resourcePath, double volume) {
        try {
            Clip clip = loadClip(resourcePath);
            if (clip == null) return;
            applyGainIfSupported(clip, volume);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    try {
                        clip.close();
                    } catch (Throwable ignored) {}
                }
            });
            clip.start();
        } catch (Throwable ignored) {}
    }

    private Clip loadClip(String resourcePath) {
        if (resourcePath == null) return null;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) return null;
        try (BufferedInputStream bis = new BufferedInputStream(is);
             AudioInputStream ais = AudioSystem.getAudioInputStream(bis)) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void applyGainIfSupported(Clip clip, double volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                applyGain(gainControl, volume);
            } catch (Throwable ignored) {}
        }
    }

    private void applyGain(FloatControl gainControl, double volume) {
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (float) (gainControl.getMinimum() + range * Math.sqrt(Math.min(1.0, Math.max(0.0, volume))));
        gainControl.setValue(gain);
    }

    @Override
    public void dispose() {
        stopMusic();
        stopNarration();
        sfxClipCache.clear();
    }
}
