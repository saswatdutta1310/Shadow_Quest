package com.shadowquest.cutscene;

import java.util.List;
import java.util.Objects;

/** Deterministic sequence state; its owner supplies time and owns the JavaFX loop. */
public final class OpeningCutscene {
    private static final List<OpeningScene> SCENES = List.of(OpeningScene.values());
    private final Runnable onComplete;
    private java.util.function.IntConsumer onSceneChange;
    private int index;
    private double elapsed;
    private boolean finished;

    public OpeningCutscene(Runnable onComplete) {
        this.onComplete = Objects.requireNonNull(onComplete, "onComplete");
    }

    public void setOnSceneChange(java.util.function.IntConsumer onSceneChange) {
        this.onSceneChange = onSceneChange;
        if (onSceneChange != null && !finished) {
            onSceneChange.accept(index);
        }
    }

    public List<OpeningScene> scenes() { return SCENES; }
    public OpeningScene scene() { return SCENES.get(index); }
    public double progress() { return Math.min(1, elapsed / scene().duration()); }
    public String narration() { return scene().narration(progress()); }
    public boolean isFinished() { return finished; }

    public void update(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) {
            throw new IllegalArgumentException("Cutscene time must be finite and non-negative.");
        }
        if (finished) return;
        // Consume scene-sized portions, so even a large finite delta cannot overflow elapsed.
        while (seconds >= scene().duration() - elapsed) {
            seconds -= scene().duration() - elapsed;
            if (index == SCENES.size() - 1) {
                elapsed = scene().duration();
                finish();
                return;
            }
            index++;
            elapsed = 0;
            if (onSceneChange != null) {
                onSceneChange.accept(index);
            }
        }
        elapsed += seconds;
    }

    public void skip() { finish(); }

    /** Restarts the sequence at Scene 1; used when a new game replays the cinematic. */
    public void reset() {
        index = 0;
        elapsed = 0;
        finished = false;
        if (onSceneChange != null) {
            onSceneChange.accept(index);
        }
    }

    /** Leaving the screen cancels playback without starting gameplay. */
    public void cancel() { finished = true; }

    private void finish() {
        if (finished) return;
        finished = true;
        onComplete.run();
    }

    public double fadeOpacity() {
        double remaining = scene().duration() - elapsed;
        return Math.max(0, 1 - Math.min(elapsed, remaining) / .7);
    }
}
