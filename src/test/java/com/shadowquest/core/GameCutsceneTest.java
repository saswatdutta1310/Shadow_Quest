package com.shadowquest.core;

import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Covers the CUTSCENE state rules: entry, one-time completion, cancel, and safety. */
class GameCutsceneTest {
    @Test
    void startOpeningCinematicFromMenuEntersCutscene() {
        Game game = new Game();
        game.startOpeningCinematic();
        assertEquals(GameState.CUTSCENE, game.getState());
        assertNull(game.getPlayer());
    }

    @Test
    void startOpeningCinematicOutsideMenuIsIgnored() {
        Game game = new Game();
        game.startNewGame();
        game.startOpeningCinematic();
        assertEquals(GameState.PLAYING, game.getState());
        game.pause();
        game.startOpeningCinematic();
        assertEquals(GameState.PAUSED, game.getState());
    }

    @Test
    void completeCutsceneStartsGameplayWithArinInsideTheWorld() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.completeCutscene();
        assertEquals(GameState.PLAYING, game.getState());
        assertNotNull(game.getPlayer());
        assertEquals("Arin", game.getPlayer().getName());
        assertTrue(game.getPlayer().isAlive());
        assertTrue(game.getPlayer().getX() >= 0);
        assertTrue(game.getPlayer().getY() >= 0);
    }

    @Test
    void completeCutsceneTransitionsExactlyOnce() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.completeCutscene();
        Player player = game.getPlayer();
        game.completeCutscene(); // A late frame or double skip must not restart the session
        assertEquals(GameState.PLAYING, game.getState());
        assertSame(player, game.getPlayer());
    }

    @Test
    void completeCutsceneOutsideCutsceneIsIgnored() {
        Game game = new Game();
        game.completeCutscene();
        assertEquals(GameState.MENU, game.getState());
        assertNull(game.getPlayer());
    }

    @Test
    void cancelCutsceneReturnsToMenuWithoutAPlayer() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.cancelCutscene();
        assertEquals(GameState.MENU, game.getState());
        assertNull(game.getPlayer());
    }

    @Test
    void cancelCutsceneOutsideCutsceneIsIgnored() {
        Game game = new Game();
        game.startNewGame();
        Player player = game.getPlayer();
        game.cancelCutscene();
        assertEquals(GameState.PLAYING, game.getState());
        assertSame(player, game.getPlayer());
    }

    @Test
    void startNewGameDuringCutsceneIsIgnored() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.startNewGame();
        assertEquals(GameState.CUTSCENE, game.getState());
        assertNull(game.getPlayer());
    }

    @Test
    void movementIsIgnoredDuringTheCutscene() {
        Game game = new Game(new GameWorld(2880, 1800));
        game.startOpeningCinematic();
        assertDoesNotThrow(() -> game.movePlayer(1, 0, 0.5));
        assertNull(game.getPlayer());
    }

    @Test
    void updateIsSafeDuringTheCutscene() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.update(1);
        assertEquals(GameState.CUTSCENE, game.getState());
    }

    @Test
    void invalidTimeIsStillValidatedDuringTheCutscene() {
        Game game = new Game();
        game.startOpeningCinematic();
        assertThrows(IllegalArgumentException.class, () -> game.update(Double.NaN));
        assertEquals(GameState.CUTSCENE, game.getState());
    }

    @Test
    void pauseAndResumeDoNotDisturbTheCutscene() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.pause();
        game.resume();
        assertEquals(GameState.CUTSCENE, game.getState());
    }

    @Test
    void movementWorksAfterTheCutsceneCompletes() {
        Game game = new Game(new GameWorld(2880, 1800));
        game.startOpeningCinematic();
        game.completeCutscene();
        game.movePlayer(1, 0, 0.5); // 0.5 s at the 200 units-per-second speed
        assertEquals(100, game.getPlayer().getX());
    }

    @Test
    void cancelledCutsceneStillAllowsANormalNewGame() {
        Game game = new Game();
        game.startOpeningCinematic();
        game.cancelCutscene();
        game.startNewGame();
        assertEquals(GameState.PLAYING, game.getState());
        assertNotNull(game.getPlayer());
    }
}
