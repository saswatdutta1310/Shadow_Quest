package com.shadowquest.core;

import com.shadowquest.input.InputHandler;
import com.shadowquest.player.Player;
import com.shadowquest.world.Camera;
import com.shadowquest.world.GameWorld;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerWorldBoundaryTest {
    private static final double EPSILON = 1e-8;

    @ParameterizedTest
    @CsvSource({"-1,0", "1,0", "0,-1", "0,1"})
    void reachesExactEdgeStaysWhileHeldAndMovesAway(int horizontal, int vertical) {
        Game game = centeredGame();
        Player player = game.getPlayer();
        double maxX = game.getWorld().getWidth() - player.getSize();
        double maxY = game.getWorld().getHeight() - player.getSize();
        double expectedX = horizontal < 0 ? 0 : horizontal > 0 ? maxX : player.getX();
        double expectedY = vertical < 0 ? 0 : vertical > 0 ? maxY : player.getY();
        double distance = Math.hypot(expectedX - player.getX(), expectedY - player.getY());
        game.movePlayer(horizontal, vertical, distance / player.getSpeed());
        assertPosition(player, expectedX, expectedY);
        for (int frame = 0; frame < 120; frame++) {
            game.movePlayer(horizontal, vertical, 1.0 / 60);
            assertPosition(player, expectedX, expectedY);
        }
        game.movePlayer(-horizontal, -vertical, 0.25);
        assertPosition(player, expectedX - horizontal * 50, expectedY - vertical * 50);
    }

    @ParameterizedTest
    @CsvSource({"-1,-1", "1,-1", "-1,1", "1,1"})
    void reachesExactCornerCannotEscapeAndCanMoveAway(int horizontal, int vertical) {
        Game game = new Game(new GameWorld(2880, 1800));
        game.startNewGame();
        Player player = game.getPlayer();
        double edgeX = horizontal < 0 ? 0 : game.getWorld().getWidth() - player.getSize();
        double edgeY = vertical < 0 ? 0 : game.getWorld().getHeight() - player.getSize();
        player.move(1, 0, (edgeX - horizontal * 100) / player.getSpeed());
        player.move(0, 1, (edgeY - vertical * 100) / player.getSpeed());
        game.movePlayer(horizontal, vertical, 100 * Math.sqrt(2) / player.getSpeed());
        assertPosition(player, edgeX, edgeY);
        for (int frame = 0; frame < 120; frame++) {
            game.movePlayer(horizontal, vertical, 1.0 / 60);
            assertPosition(player, edgeX, edgeY);
        }
        game.movePlayer(-horizontal, -vertical, 0.25);
        double component = 50 / Math.sqrt(2);
        assertPosition(player, edgeX - horizontal * component, edgeY - vertical * component);
    }

    @ParameterizedTest
    @CsvSource({"-1,0", "1,0", "0,-1", "0,1", "-1,-1", "1,-1", "-1,1", "1,1"})
    void overshootClampsAndCameraKeepsWholePlayerVisible(int horizontal, int vertical) {
        Game game = centeredGame();
        Player player = game.getPlayer();
        game.movePlayer(horizontal, vertical, 100);
        double expectedX = horizontal < 0 ? 0 : horizontal > 0
                ? game.getWorld().getWidth() - player.getSize() : 1000;
        double expectedY = vertical < 0 ? 0 : vertical > 0
                ? game.getWorld().getHeight() - player.getSize() : 600;
        assertPosition(player, expectedX, expectedY);
        Camera camera = new Camera(game.getWorld());
        camera.follow(player.getX(), player.getY(), 960, 550);
        double screenX = camera.worldToScreenX(player.getX());
        double screenY = camera.worldToScreenY(player.getY());
        assertTrue(screenX >= 0 && screenX + player.getSize() <= 960);
        assertTrue(screenY >= 0 && screenY + player.getSize() <= 550);
        if (horizontal != 0) {
            assertEquals(horizontal < 0 ? 0 : game.getWorld().getWidth() - 960, camera.getX());
        }
        if (vertical != 0) {
            assertEquals(vertical < 0 ? 0 : game.getWorld().getHeight() - 550, camera.getY());
        }
        assertPosition(player, expectedX, expectedY);
    }

    @ParameterizedTest
    @CsvSource({"A,W", "D,W", "A,S", "D,S", "LEFT,UP", "RIGHT,UP", "LEFT,DOWN", "RIGHT,DOWN"})
    void bothKeySchemesHoldAtCornersReleaseAndMoveAway(KeyCode horizontalKey, KeyCode verticalKey) {
        Game game = centeredGame();
        InputHandler input = new InputHandler();
        input.press(horizontalKey);
        input.press(verticalKey);
        int horizontal = input.getHorizontal();
        int vertical = input.getVertical();
        for (int frame = 0; frame < 2000; frame++) {
            game.movePlayer(input.getHorizontal(), input.getVertical(), 0.02);
        }
        Player player = game.getPlayer();
        double edgeX = horizontal < 0 ? 0 : game.getWorld().getWidth() - player.getSize();
        double edgeY = vertical < 0 ? 0 : game.getWorld().getHeight() - player.getSize();
        assertPosition(player, edgeX, edgeY);
        input.release(horizontalKey);
        input.release(verticalKey);
        game.movePlayer(input.getHorizontal(), input.getVertical(), 1);
        assertPosition(player, edgeX, edgeY);
        input.press(horizontal < 0 ? KeyCode.RIGHT : KeyCode.LEFT);
        input.press(vertical < 0 ? KeyCode.DOWN : KeyCode.UP);
        game.movePlayer(input.getHorizontal(), input.getVertical(), 0.25);
        assertPosition(player, edgeX - horizontal * 50 / Math.sqrt(2),
                edgeY - vertical * 50 / Math.sqrt(2));
    }

    @ParameterizedTest
    @CsvSource({"-1,0", "1,0", "0,-1", "0,1", "-1,-1", "1,-1", "-1,1", "1,1"})
    void interiorMovementRetainsSpeedNormalizationAndFrameIndependence(int horizontal, int vertical) {
        Game single = centeredGame();
        Game split = centeredGame();
        single.movePlayer(horizontal, vertical, 1);
        for (int frame = 0; frame < 100; frame++) {
            split.movePlayer(horizontal, vertical, 0.01);
        }
        Player player = single.getPlayer();
        double length = Math.hypot(horizontal, vertical);
        assertPosition(player, 1000 + horizontal / length * 200, 600 + vertical / length * 200);
        assertPosition(split.getPlayer(), player.getX(), player.getY());
        assertEquals(200, Math.hypot(player.getX() - 1000, player.getY() - 600), EPSILON);
        single.movePlayer(horizontal, vertical, 100);
        for (int frame = 0; frame < 1000; frame++) {
            split.movePlayer(horizontal, vertical, 0.1);
        }
        assertPosition(split.getPlayer(), single.getPlayer().getX(), single.getPlayer().getY());
    }

    @Test
    void diagonalIntoOneEdgeSlidesAlongTheOtherAxisWithoutSpeedBoost() {
        Game game = centeredGame();
        game.movePlayer(-1, 0, 100);
        game.movePlayer(-1, 1, 1);
        assertPosition(game.getPlayer(), 0, 600 + 200 / Math.sqrt(2));
    }

    @Test
    void fractionalAndExactFitWorldsUseBodySize() {
        Player player = new Player("Arin");
        assertEquals(24, player.getSize());
        player.move(1, 1, 100);
        player.constrainToWorld(new GameWorld(100.5, 80.25));
        assertPosition(player, 76.5, 56.25);
        player.constrainToWorld(new GameWorld(player.getSize(), player.getSize()));
        assertPosition(player, 0, 0);
    }

    @Test
    void invalidWorldDoesNotMutatePlayer() {
        Player player = new Player("Arin");
        player.move(1, 0, 1);
        assertThrows(NullPointerException.class, () -> player.constrainToWorld(null));
        assertThrows(IllegalArgumentException.class,
                () -> player.constrainToWorld(new GameWorld(23, 100)));
        assertThrows(IllegalArgumentException.class,
                () -> player.constrainToWorld(new GameWorld(100, 23)));
        assertPosition(player, 200, 0);
    }

    @Test
    void veryLargeFiniteOvershootStillLandsOnExactBoundary() {
        Game game = centeredGame();
        game.movePlayer(1, 1, 1e100);
        assertPosition(game.getPlayer(), game.getWorld().getWidth() - game.getPlayer().getSize(),
                game.getWorld().getHeight() - game.getPlayer().getSize());
    }

    private static Game centeredGame() {
        Game game = new Game(new GameWorld(2880, 1800));
        game.startNewGame();
        game.movePlayer(1, 0, 5);
        game.movePlayer(0, 1, 3);
        return game;
    }

    private static void assertPosition(Player player, double x, double y) {
        assertEquals(x, player.getX(), EPSILON);
        assertEquals(y, player.getY(), EPSILON);
    }
}
