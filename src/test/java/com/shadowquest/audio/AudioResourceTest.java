package com.shadowquest.audio;

import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that all audio tracks, sound effects, and voice narration assets
 * exist on the classpath, are non-empty, and contain valid standard WAV headers.
 */
public class AudioResourceTest {

    @Test
    @DisplayName("All background music tracks exist and contain valid WAV data")
    void allMusicTracksExist() throws Exception {
        for (AudioTrack track : AudioTrack.values()) {
            assertValidWav(track.getResourcePath());
        }
    }

    @Test
    @DisplayName("All sound effects exist and contain valid WAV data")
    void allSoundEffectsExist() throws Exception {
        for (SoundEffect sfx : SoundEffect.values()) {
            assertValidWav(sfx.getResourcePath());
        }
    }

    @Test
    @DisplayName("All seven opening cinematic narration files exist and contain valid WAV data")
    void allNarrationFilesExist() throws Exception {
        for (int i = 1; i <= 7; i++) {
            assertValidWav("/assets/audio/narration/scene" + i + ".wav");
        }
    }

    private void assertValidWav(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Audio resource must exist on classpath: " + resourcePath);

            byte[] header = new byte[12];
            int read = is.read(header);
            assertEquals(12, read, "Audio file must contain at least 12 header bytes: " + resourcePath);

            // Verify RIFF and WAVE magic signatures
            String riff = new String(header, 0, 4);
            String wave = new String(header, 8, 4);
            assertEquals("RIFF", riff, "Audio file must start with RIFF: " + resourcePath);
            assertEquals("WAVE", wave, "Audio file format must be WAVE: " + resourcePath);
        }
    }
}
