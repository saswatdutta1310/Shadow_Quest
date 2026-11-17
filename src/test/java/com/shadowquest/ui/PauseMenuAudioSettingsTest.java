package com.shadowquest.ui;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.MockAudioBackend;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Pause Menu audio settings panel: volume sliders, mute/unmute control,
 * and immediate AudioManager synchronization.
 */
public class PauseMenuAudioSettingsTest {
    private MockAudioBackend mockBackend;
    private PauseMenuOverlay overlay;
    private boolean[] fullScreenState = {false};

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Platform already initialized
        }
    }

    @BeforeEach
    void setUp() {
        mockBackend = new MockAudioBackend();
        AudioManager.setInstance(new AudioManager(mockBackend));

        overlay = new PauseMenuOverlay(
                () -> {},
                () -> {},
                () -> {},
                () -> {},
                () -> fullScreenState[0] = !fullScreenState[0],
                () -> fullScreenState[0]
        );
    }

    @Test
    @DisplayName("Settings view displays real Music and SFX volume sliders")
    void testSettingsViewContainsSliders() {
        overlay.setView(PauseMenuOverlay.View.SETTINGS);

        Slider musicSlider = overlay.getMusicSlider();
        Slider sfxSlider = overlay.getSfxSlider();

        assertNotNull(musicSlider, "Music volume slider must be present");
        assertNotNull(sfxSlider, "SFX volume slider must be present");

        assertEquals(AudioManager.DEFAULT_MUSIC_VOLUME * 100, musicSlider.getValue(), 0.1);
        assertEquals(AudioManager.DEFAULT_SFX_VOLUME * 100, sfxSlider.getValue(), 0.1);
    }

    @Test
    @DisplayName("Moving Music slider immediately updates AudioManager volume")
    void testMusicSliderUpdatesVolumeImmediately() {
        overlay.setView(PauseMenuOverlay.View.SETTINGS);
        Slider musicSlider = overlay.getMusicSlider();

        musicSlider.setValue(45.0);
        assertEquals(0.45, AudioManager.getInstance().getMusicVolume(), 0.01);

        musicSlider.setValue(90.0);
        assertEquals(0.90, AudioManager.getInstance().getMusicVolume(), 0.01);
    }

    @Test
    @DisplayName("Moving SFX slider immediately updates AudioManager volume")
    void testSfxSliderUpdatesVolumeImmediately() {
        overlay.setView(PauseMenuOverlay.View.SETTINGS);
        Slider sfxSlider = overlay.getSfxSlider();

        sfxSlider.setValue(35.0);
        assertEquals(0.35, AudioManager.getInstance().getSfxVolume(), 0.01);

        sfxSlider.setValue(100.0);
        assertEquals(1.0, AudioManager.getInstance().getSfxVolume(), 0.01);
    }

    @Test
    @DisplayName("Mute button toggles global mute and restores volumes on unmute")
    void testMuteButtonToggle() {
        AudioManager.getInstance().setMusicVolume(0.60);
        AudioManager.getInstance().setSfxVolume(0.75);

        overlay.setView(PauseMenuOverlay.View.SETTINGS);

        // Find the mute button in active buttons
        Button muteBtn = overlay.getActiveButtons().stream()
                .filter(b -> b.getText().contains("Sound:"))
                .findFirst()
                .orElse(null);
        assertNotNull(muteBtn, "Mute button must exist in settings panel");
        assertTrue(muteBtn.getText().contains("ENABLED"));

        // Click to mute
        muteBtn.fire();
        assertTrue(AudioManager.getInstance().isMuted());

        // Find updated button after view rebuild
        Button updatedMuteBtn = overlay.getActiveButtons().stream()
                .filter(b -> b.getText().contains("Sound:"))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedMuteBtn);
        assertTrue(updatedMuteBtn.getText().contains("MUTED"));

        // Click to unmute
        updatedMuteBtn.fire();
        assertFalse(AudioManager.getInstance().isMuted());
        assertEquals(0.60, AudioManager.getInstance().getMusicVolume(), 0.01);
        assertEquals(0.75, AudioManager.getInstance().getSfxVolume(), 0.01);
    }

    @Test
    @DisplayName("Settings view Back button navigates back to MAIN view")
    void testBackButtonNavigation() {
        overlay.setView(PauseMenuOverlay.View.SETTINGS);

        Button backBtn = overlay.getActiveButtons().stream()
                .filter(b -> b.getText().contains("Back"))
                .findFirst()
                .orElse(null);
        assertNotNull(backBtn);

        backBtn.fire();
        assertEquals(PauseMenuOverlay.View.MAIN, overlay.getCurrentView());
    }
}
