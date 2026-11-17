package com.shadowquest.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests AudioManager state management, volume channels, mute/unmute,
 * cooldown throttling, lifecycle, and reset behaviours using MockAudioBackend.
 */
public class AudioManagerTest {
    private MockAudioBackend mockBackend;
    private AudioManager audioManager;

    @BeforeEach
    void setUp() {
        mockBackend = new MockAudioBackend();
        audioManager = new AudioManager(mockBackend);
    }

    @Test
    @DisplayName("1. AudioManager initializes with sensible default volumes")
    void testInitializationDefaults() {
        assertEquals(0.70, audioManager.getMusicVolume(), 0.001);
        assertEquals(0.85, audioManager.getSfxVolume(), 0.001);
        assertFalse(audioManager.isMuted());
        assertNull(audioManager.getCurrentTrack());
        assertFalse(audioManager.isMusicPaused());
    }

    @Test
    @DisplayName("2. Missing resource handling does not crash")
    void testMissingResourceHandling() {
        assertDoesNotThrow(() -> {
            mockBackend.playMusic(null, 0.5, true);
            mockBackend.playSfx(null, 0.5);
            mockBackend.playNarration(null, 0.5);
        });
        assertFalse(mockBackend.isMusicPlaying());
    }

    @Test
    @DisplayName("3. Music playback state transitions: play, pause, resume, stop")
    void testMusicPlaybackState() {
        audioManager.playMusic(AudioTrack.MENU, true);
        assertEquals(AudioTrack.MENU, audioManager.getCurrentTrack());
        assertTrue(mockBackend.isMusicPlaying());
        assertFalse(mockBackend.isMusicPaused());
        assertEquals(AudioTrack.MENU.getResourcePath(), mockBackend.getCurrentMusicPath());
        assertTrue(mockBackend.isMusicLooping());

        audioManager.pauseMusic();
        assertTrue(audioManager.isMusicPaused());
        assertTrue(mockBackend.isMusicPaused());

        audioManager.resumeMusic();
        assertFalse(audioManager.isMusicPaused());
        assertFalse(mockBackend.isMusicPaused());

        audioManager.stopMusic();
        assertNull(audioManager.getCurrentTrack());
        assertFalse(mockBackend.isMusicPlaying());
    }

    @Test
    @DisplayName("4. Independent Music volume control")
    void testMusicVolumeControl() {
        audioManager.setMusicVolume(0.40);
        assertEquals(0.40, audioManager.getMusicVolume(), 0.001);

        audioManager.playMusic(AudioTrack.FOREST);
        assertEquals(0.40, mockBackend.getCurrentMusicVolume(), 0.001);

        // Clamping to [0, 1]
        audioManager.setMusicVolume(1.50);
        assertEquals(1.0, audioManager.getMusicVolume(), 0.001);
        audioManager.setMusicVolume(-0.25);
        assertEquals(0.0, audioManager.getMusicVolume(), 0.001);
    }

    @Test
    @DisplayName("5. Independent SFX volume control")
    void testSfxVolumeControl() {
        audioManager.setSfxVolume(0.55);
        assertEquals(0.55, audioManager.getSfxVolume(), 0.001);

        audioManager.playSfx(SoundEffect.SWORD_SWING);
        assertEquals(1, mockBackend.getPlayedSfx().size());
        assertEquals(SoundEffect.SWORD_SWING.getResourcePath(), mockBackend.getPlayedSfx().get(0));

        // Clamping to [0, 1]
        audioManager.setSfxVolume(2.0);
        assertEquals(1.0, audioManager.getSfxVolume(), 0.001);
        audioManager.setSfxVolume(-0.5);
        assertEquals(0.0, audioManager.getSfxVolume(), 0.001);
    }

    @Test
    @DisplayName("6. Global mute silences output, unmuting restores previous volume")
    void testMuteAndUnmute() {
        audioManager.setMusicVolume(0.65);
        audioManager.setSfxVolume(0.80);
        audioManager.playMusic(AudioTrack.CASTLE);

        audioManager.setMuted(true);
        assertTrue(audioManager.isMuted());
        assertEquals(0.0, mockBackend.getCurrentMusicVolume(), 0.001);

        // SFX does not play when muted
        mockBackend.clearPlayedSfx();
        audioManager.playSfx(SoundEffect.SWORD_HIT);
        assertTrue(mockBackend.getPlayedSfx().isEmpty(), "SFX should not play when muted");

        // Unmute restores exact previous volume levels
        audioManager.setMuted(false);
        assertFalse(audioManager.isMuted());
        assertEquals(0.65, audioManager.getMusicVolume(), 0.001);
        assertEquals(0.80, audioManager.getSfxVolume(), 0.001);
        assertEquals(0.65, mockBackend.getCurrentMusicVolume(), 0.001);
    }

    @Test
    @DisplayName("7. Duplicate music playback is prevented when same track is requested")
    void testDuplicateMusicPlaybackPrevention() {
        audioManager.playMusic(AudioTrack.BOSS);
        String firstPath = mockBackend.getCurrentMusicPath();

        // Calling play on the same track again should be a no-op
        audioManager.playMusic(AudioTrack.BOSS);
        assertSame(firstPath, mockBackend.getCurrentMusicPath());
        assertEquals(AudioTrack.BOSS, audioManager.getCurrentTrack());
    }

    @Test
    @DisplayName("11. Audio cleanup on level restart resets state cleanly")
    void testLevelRestartCleanup() {
        audioManager.playNarration(2);
        assertTrue(mockBackend.isNarrationPlaying());

        audioManager.resetForLevel();
        assertFalse(mockBackend.isNarrationPlaying(), "Narration must be stopped on level reset");
    }

    @Test
    @DisplayName("12. Audio cleanup on return to main menu restores menu music")
    void testReturnToMainMenuCleanup() {
        audioManager.playMusic(AudioTrack.CASTLE);
        audioManager.playNarration(3);

        audioManager.resetForMainMenu();
        assertFalse(mockBackend.isNarrationPlaying());
        assertEquals(AudioTrack.MENU, audioManager.getCurrentTrack());
        assertTrue(mockBackend.isMusicPlaying());
    }

    @Test
    @DisplayName("13. PauseAll and ResumeAll pause and restore both music and narration")
    void testPauseAndResumeAll() {
        audioManager.playMusic(AudioTrack.FOREST);
        audioManager.playNarration(1);

        audioManager.pauseAll();
        assertTrue(audioManager.isMusicPaused());
        assertTrue(mockBackend.isMusicPaused());
        assertTrue(mockBackend.isNarrationPaused());

        audioManager.resumeAll();
        assertFalse(audioManager.isMusicPaused());
        assertFalse(mockBackend.isMusicPaused());
        assertFalse(mockBackend.isNarrationPaused());
    }
}
