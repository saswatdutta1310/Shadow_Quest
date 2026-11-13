package com.shadowquest.world;

import com.shadowquest.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    @Test
    void startsAtWorldOrigin() {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        assertEquals(0, camera.getX());
        assertEquals(0, camera.getY());
    }

    @ParameterizedTest
    @CsvSource({"600,400", "1440,900", "2000.25,1200.5"})
    void followsInteriorTargetAtViewportCenter(double x, double y) {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(x, y, 960, 600);
        assertEquals(x - 480, camera.getX());
        assertEquals(y - 300, camera.getY());
        assertEquals(480, camera.worldToScreenX(x));
        assertEquals(300, camera.worldToScreenY(y));
    }

    @ParameterizedTest
    @CsvSource({"0,900,0,600", "2880,900,1920,600", "1440,0,960,0", "1440,1800,960,1200",
            "0,0,0,0", "2880,0,1920,0", "0,1800,0,1200", "2880,1800,1920,1200",
            "-100,-200,0,0", "10000,10000,1920,1200"})
    void clampsAllEdgesCornersAndOutOfWorldTargets(double x, double y, double expectedX, double expectedY) {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(x, y, 960, 600);
        assertEquals(expectedX, camera.getX());
        assertEquals(expectedY, camera.getY());
        assertTrue(camera.getX() >= 0 && camera.getX() + 960 <= 2880);
        assertTrue(camera.getY() >= 0 && camera.getY() + 600 <= 1800);
    }

    @Test
    void convertsAnyWorldPointUsingTheSameOffset() {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(1000, 800, 960, 600);
        assertEquals(-520, camera.worldToScreenX(0));
        assertEquals(-500, camera.worldToScreenY(0));
        assertEquals(100, camera.worldToScreenX(620));
        assertEquals(50, camera.worldToScreenY(550));
    }

    @ParameterizedTest
    @CsvSource({"400,200,0,0", "960,600,0,0", "400,1800,0,600", "2880,200,960,0"})
    void smallAndExactWorldsNeverCreateNegativeCameraOffsets(double width, double height,
                                                           double expectedX, double expectedY) {
        Camera camera = new Camera(new GameWorld(width, height));
        camera.follow(1440, 900, 960, 600);
        assertEquals(expectedX, camera.getX());
        assertEquals(expectedY, camera.getY());
    }

    @Test
    void recalculatesForViewportResize() {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(2000, 1400, 960, 600);
        assertEquals(1520, camera.getX());
        assertEquals(1100, camera.getY());
        camera.follow(2000, 1400, 1920, 1080);
        assertEquals(960, camera.getX());
        assertEquals(720, camera.getY());
        camera.follow(2000, 1400, 4000, 3000);
        assertEquals(0, camera.getX());
        assertEquals(0, camera.getY());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidViewportDoesNotPartiallyChangeCamera(double invalid) {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(1000, 800, 960, 600);
        assertThrows(IllegalArgumentException.class, () -> camera.follow(0, 0, invalid, 600));
        assertThrows(IllegalArgumentException.class, () -> camera.follow(0, 0, 960, invalid));
        assertEquals(520, camera.getX());
        assertEquals(500, camera.getY());
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidTargetDoesNotPartiallyChangeCamera(double invalid) {
        Camera camera = new Camera(new GameWorld(2880, 1800));
        camera.follow(1000, 800, 960, 600);
        assertThrows(IllegalArgumentException.class, () -> camera.follow(invalid, 0, 960, 600));
        assertThrows(IllegalArgumentException.class, () -> camera.follow(0, invalid, 960, 600));
        assertEquals(520, camera.getX());
        assertEquals(500, camera.getY());
    }

    @Test
    void cameraObservesPlayerWithoutChangingMovement() {
        Player observed = new Player("Arin");
        Player reference = new Player("Arin");
        Camera camera = new Camera(new GameWorld(2880, 1800));
        for (int i = 0; i < 100; i++) {
            observed.move(1, 1, 0.1);
            reference.move(1, 1, 0.1);
            camera.follow(observed.getX(), observed.getY(), 960, 600);
        }
        assertEquals(reference.getX(), observed.getX());
        assertEquals(reference.getY(), observed.getY());
        assertEquals(2000, Math.hypot(observed.getX(), observed.getY()), 1e-8);
    }

    @Test
    void rejectsMissingWorld() {
        assertThrows(NullPointerException.class, () -> new Camera(null));
    }
}
