package com.shadowquest.cutscene;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OpeningCutsceneTest {
    @Test
    void newCutsceneStartsAtSceneOneAndIsUnfinished() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        assertEquals(OpeningScene.EXPLORATION, cutscene.scene());
        assertEquals(0, cutscene.progress());
        assertFalse(cutscene.isFinished());
    }

    @Test
    void scenesAreListedInApprovedStoryOrder() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        List<OpeningScene> scenes = cutscene.scenes();
        assertEquals(List.of(OpeningScene.EXPLORATION, OpeningScene.DISCOVERY,
                OpeningScene.ENTERING, OpeningScene.ARRIVAL, OpeningScene.MONTAGE,
                OpeningScene.MISSION, OpeningScene.GAMEPLAY), scenes);
    }

    @Test
    void narrationShowsFirstLineBeforeMidpoint() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.update(3); // 3 of 7 seconds: before the halfway point
        assertEquals(OpeningScene.EXPLORATION.firstLine(), cutscene.narration());
    }

    @Test
    void narrationShowsSecondLineAfterMidpoint() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.update(4); // 4 of 7 seconds: past the halfway point
        assertEquals(OpeningScene.EXPLORATION.secondLine(), cutscene.narration());
    }

    @Test
    void updateAdvancesWithinTheCurrentScene() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.update(3);
        assertEquals(OpeningScene.EXPLORATION, cutscene.scene());
        assertEquals(3.0 / 7, cutscene.progress());
    }

    @Test
    void updateCrossesIntoTheNextSceneAtTheBoundary() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.update(7); // Exactly Scene 1's duration
        assertEquals(OpeningScene.DISCOVERY, cutscene.scene());
        assertEquals(0, cutscene.progress());
        assertFalse(cutscene.isFinished());
    }

    @Test
    void largeDeltaConsumesWholeScenesWithoutSkippingContent() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.update(20); // Exactly Scene 1 (7) + Scene 2 (7) + Scene 3 (6)
        assertEquals(OpeningScene.ARRIVAL, cutscene.scene());
        assertEquals(0, cutscene.progress());
        assertFalse(cutscene.isFinished());
    }

    @Test
    void completingTheLastSceneRunsTheCallbackExactlyOnce() {
        AtomicInteger completions = new AtomicInteger();
        OpeningCutscene cutscene = new OpeningCutscene(completions::incrementAndGet);
        cutscene.update(55); // Total duration of all seven scenes
        assertTrue(cutscene.isFinished());
        assertEquals(1, completions.get());
    }

    @Test
    void updateAfterCompletionNeverRepeatsTheCallback() {
        AtomicInteger completions = new AtomicInteger();
        OpeningCutscene cutscene = new OpeningCutscene(completions::incrementAndGet);
        cutscene.update(60); // Overshoots the ending
        cutscene.update(10);
        assertTrue(cutscene.isFinished());
        assertEquals(1, completions.get());
    }

    @Test
    void skipFinishesImmediatelyAndRunsTheCallbackOnce() {
        AtomicInteger completions = new AtomicInteger();
        OpeningCutscene cutscene = new OpeningCutscene(completions::incrementAndGet);
        cutscene.skip();
        cutscene.skip(); // A second skip must not re-enter gameplay
        assertTrue(cutscene.isFinished());
        assertEquals(1, completions.get());
    }

    @Test
    void cancelFinishesWithoutStartingGameplay() {
        AtomicInteger completions = new AtomicInteger();
        OpeningCutscene cutscene = new OpeningCutscene(completions::incrementAndGet);
        cutscene.cancel();
        assertTrue(cutscene.isFinished());
        assertEquals(0, completions.get());
    }

    @Test
    void resetRestartsAtSceneOneForReplay() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        cutscene.skip();
        cutscene.reset();
        assertEquals(OpeningScene.EXPLORATION, cutscene.scene());
        assertEquals(0, cutscene.progress());
        assertFalse(cutscene.isFinished());
    }

    @Test
    void fadeStartsOpaqueAtSceneStartAndClearsMidScene() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        assertEquals(1, cutscene.fadeOpacity()); // Fully faded in at the boundary
        cutscene.update(0.7);
        assertEquals(0, cutscene.fadeOpacity()); // Fully visible after the cross-fade
        cutscene.update(5.6); // 6.3 of 7 seconds: 0.7 before the boundary
        assertEquals(0, cutscene.fadeOpacity()); // Fade-out completes 0.7 s before the scene ends
    }

    @Test
    void negativeTimeIsRejected() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        assertThrows(IllegalArgumentException.class, () -> cutscene.update(-0.1));
    }

    @Test
    void nonFiniteTimeIsRejected() {
        OpeningCutscene cutscene = new OpeningCutscene(() -> { });
        assertThrows(IllegalArgumentException.class, () -> cutscene.update(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> cutscene.update(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> cutscene.update(Double.NEGATIVE_INFINITY));
    }
}
