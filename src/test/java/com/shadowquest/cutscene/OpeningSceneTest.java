package com.shadowquest.cutscene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpeningSceneTest {
    @Test
    void containsExactlyTheSevenApprovedScenesInOrder() {
        OpeningScene[] scenes = OpeningScene.values();
        assertEquals(7, scenes.length);
        assertEquals(OpeningScene.EXPLORATION, scenes[0]);
        assertEquals(OpeningScene.DISCOVERY, scenes[1]);
        assertEquals(OpeningScene.ENTERING, scenes[2]);
        assertEquals(OpeningScene.ARRIVAL, scenes[3]);
        assertEquals(OpeningScene.MONTAGE, scenes[4]);
        assertEquals(OpeningScene.MISSION, scenes[5]);
        assertEquals(OpeningScene.GAMEPLAY, scenes[6]);
    }

    @Test
    void everySceneHasAPositiveDuration() {
        for (OpeningScene scene : OpeningScene.values()) {
            assertTrue(scene.duration() > 0, scene + " must have a positive duration");
        }
    }

    @Test
    void everySceneHasCompleteNarrationText() {
        for (OpeningScene scene : OpeningScene.values()) {
            assertFalse(scene.title().isBlank(), scene + " must have a title");
            assertFalse(scene.firstLine().isBlank(), scene + " must have a first line");
            assertFalse(scene.secondLine().isBlank(), scene + " must have a second line");
        }
    }

    @Test
    void narrationSwitchesLinesAtTheMidpoint() {
        for (OpeningScene scene : OpeningScene.values()) {
            assertEquals(scene.firstLine(), scene.narration(0));
            assertEquals(scene.firstLine(), scene.narration(0.49));
            assertEquals(scene.secondLine(), scene.narration(0.5));
            assertEquals(scene.secondLine(), scene.narration(1));
        }
    }

    @Test
    void gameplaySceneIsLastAndShort() {
        OpeningScene last = OpeningScene.values()[OpeningScene.values().length - 1];
        assertEquals(OpeningScene.GAMEPLAY, last);
        assertTrue(last.duration() < OpeningScene.values()[0].duration());
    }
}
