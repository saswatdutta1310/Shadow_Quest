package com.shadowquest.audio;

/**
 * Coordinates voice narration playback for the seven-scene opening cinematic.
 * Ensures scenes advance without overlapping previous lines, stops on skip/cancel,
 * and restarts cleanly on cinematic replay.
 */
public class NarrationManager {
    private final AudioBackend backend;
    private int currentSceneIndex = 0;
    private boolean active = false;

    public NarrationManager(AudioBackend backend) {
        this.backend = backend;
    }

    /**
     * Plays the voice narration for the specified scene index (1 to 7).
     * Avoids duplicate restart if the scene is already playing.
     *
     * @param sceneIndex 1-based opening scene index
     * @param volume volume level (0.0 to 1.0)
     */
    public void playScene(int sceneIndex, double volume) {
        if (sceneIndex < 1 || sceneIndex > 7) {
            return;
        }
        if (active && currentSceneIndex == sceneIndex) {
            return; // Already playing this scene
        }

        this.currentSceneIndex = sceneIndex;
        this.active = true;
        String path = "/assets/audio/narration/scene" + sceneIndex + ".wav";
        backend.playNarration(path, volume);
    }

    public void pause() {
        if (active) {
            backend.pauseNarration();
        }
    }

    public void resume() {
        if (active) {
            backend.resumeNarration();
        }
    }

    public void stop() {
        backend.stopNarration();
        active = false;
        currentSceneIndex = 0;
    }

    public void reset() {
        stop();
    }

    public int getCurrentSceneIndex() {
        return currentSceneIndex;
    }

    public boolean isActive() {
        return active;
    }
}
