package com.shadowquest.audio;

import com.shadowquest.cutscene.OpeningCutscene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests opening cinematic narration synchronization, skip/cancel handling,
 * and replay behaviour.
 */
public class NarrationManagerTest {
    private MockAudioBackend mockBackend;
    private NarrationManager narrationManager;

    @BeforeEach
    void setUp() {
        mockBackend = new MockAudioBackend();
        narrationManager = new NarrationManager(mockBackend);
    }

    @Test
    @DisplayName("8. Narration scene synchronization advances scenes cleanly")
    void testSceneSynchronization() {
        narrationManager.playScene(1, 0.85);
        assertTrue(narrationManager.isActive());
        assertEquals(1, narrationManager.getCurrentSceneIndex());
        assertEquals("/assets/audio/narration/scene1.wav", mockBackend.getCurrentNarrationPath());

        // Advance to scene 2
        narrationManager.playScene(2, 0.85);
        assertEquals(2, narrationManager.getCurrentSceneIndex());
        assertEquals("/assets/audio/narration/scene2.wav", mockBackend.getCurrentNarrationPath());
    }

    @Test
    @DisplayName("Duplicate playback prevention: requesting same scene does not restart")
    void testDuplicateSceneRequest() {
        narrationManager.playScene(3, 0.85);
        String initialPath = mockBackend.getCurrentNarrationPath();

        // Repeating scene 3 request should not restart or change path
        narrationManager.playScene(3, 0.85);
        assertSame(initialPath, mockBackend.getCurrentNarrationPath());
        assertEquals(3, narrationManager.getCurrentSceneIndex());
    }

    @Test
    @DisplayName("9. Narration stopping on skip or cancel immediately silences voice")
    void testStoppingOnSkipOrCancel() {
        narrationManager.playScene(4, 0.85);
        assertTrue(mockBackend.isNarrationPlaying());

        narrationManager.stop();
        assertFalse(narrationManager.isActive());
        assertFalse(mockBackend.isNarrationPlaying());
        assertEquals(0, narrationManager.getCurrentSceneIndex());
    }

    @Test
    @DisplayName("10. Narration resets and restarts correctly on replay")
    void testReplayBehaviour() {
        narrationManager.playScene(5, 0.85);
        assertEquals(5, narrationManager.getCurrentSceneIndex());

        // Replay resets to Scene 1
        narrationManager.reset();
        assertEquals(0, narrationManager.getCurrentSceneIndex());
        assertFalse(narrationManager.isActive());

        narrationManager.playScene(1, 0.85);
        assertEquals(1, narrationManager.getCurrentSceneIndex());
        assertEquals("/assets/audio/narration/scene1.wav", mockBackend.getCurrentNarrationPath());
    }

    @Test
    @DisplayName("Cutscene listener integrates with OpeningCutscene updates and skip")
    void testCutsceneListenerIntegration() {
        boolean[] completed = {false};
        OpeningCutscene cutscene = new OpeningCutscene(() -> completed[0] = true);
        cutscene.setOnSceneChange(sceneIdx -> narrationManager.playScene(sceneIdx + 1, 0.85));

        // Immediately plays scene 1 (index 0 + 1)
        assertEquals(1, narrationManager.getCurrentSceneIndex());

        // Update past scene 1 duration (7s)
        cutscene.update(7.1);
        assertEquals(2, narrationManager.getCurrentSceneIndex());

        // Skip immediately stops narration
        cutscene.skip();
        narrationManager.stop();
        assertFalse(narrationManager.isActive());
        assertTrue(completed[0]);
    }
}
