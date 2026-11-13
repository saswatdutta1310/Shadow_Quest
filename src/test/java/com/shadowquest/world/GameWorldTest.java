package com.shadowquest.world;

import com.shadowquest.core.Game;
import com.shadowquest.core.GameConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {
    @Test
    void storesDimensionsIndependentlyFromViewport() {
        GameWorld world = new GameWorld(2880.5, 1800.25);
        assertEquals(2880.5, world.getWidth());
        assertEquals(1800.25, world.getHeight());
    }

    @Test
    void gameUsesConfiguredWorldAndPreservesOriginOnRestart() {
        Game game = new Game(new GameWorld(2880, 1800));
        GameWorld world = game.getWorld();
        assertEquals(GameConfig.WORLD_WIDTH, world.getWidth());
        assertEquals(GameConfig.WORLD_HEIGHT, world.getHeight());
        assertTrue(world.getWidth() > GameConfig.VIEWPORT_WIDTH);
        assertTrue(world.getHeight() > GameConfig.VIEWPORT_HEIGHT);
        game.startNewGame();
        assertEquals(0, game.getPlayer().getX());
        assertEquals(0, game.getPlayer().getY());
        game.getPlayer().takeDamage(100);
        game.update(0);
        game.startNewGame();
        assertSame(world, game.getWorld());
        assertEquals(0, game.getPlayer().getX());
        assertEquals(0, game.getPlayer().getY());
    }

    @Test
    void gameRestrictsMovementToWorldMinusPlayerSize() {
        Game game = new Game(new GameWorld(2880, 1800));
        game.startNewGame();
        game.movePlayer(-1, -1, 1);
        assertEquals(0, game.getPlayer().getX());
        assertEquals(0, game.getPlayer().getY());
        game.movePlayer(1, 1, 100);
        assertEquals(game.getWorld().getWidth() - game.getPlayer().getSize(), game.getPlayer().getX());
        assertEquals(game.getWorld().getHeight() - game.getPlayer().getSize(), game.getPlayer().getY());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInvalidDimensions(double invalid) {
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(invalid, 600));
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(960, invalid));
    }
}
