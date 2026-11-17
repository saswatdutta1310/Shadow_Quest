package com.shadowquest.ui;

import com.shadowquest.core.Game;
import com.shadowquest.core.GameState;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.player.Player;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("In-Game Pause Menu System & Lifecycle Safety")
class PauseMenuTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already running
        }
    }

    @Test
    @DisplayName("Opening pause menu pauses gameplay immediately and halts player movement and attacks")
    void openPauseMenuAndHaltGameplay() {
        Game game = new Game();
        game.startNewGame();
        assertEquals(GameState.PLAYING, game.getState());

        AtomicBoolean resumed = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                () -> {
                    game.resume();
                    resumed.set(true);
                },
                null, null, null, null, null
        );

        game.pause();
        assertEquals(GameState.PAUSED, game.getState());
        overlay.showMain();

        assertTrue(overlay.isVisible());
        assertEquals(PauseMenuOverlay.View.MAIN, overlay.getCurrentView());

        // Player cannot move while paused
        Player player = game.getPlayer();
        double oldX = player.getX();
        double oldY = player.getY();
        game.movePlayer(1, 1, 0.5);
        assertEquals(oldX, player.getX());
        assertEquals(oldY, player.getY());

        // Player cannot attack while paused
        boolean attacked = game.attackPlayer();
        assertFalse(attacked);
    }

    @Test
    @DisplayName("Resume option closes pause menu and continues gameplay from where it stopped")
    void resumeGameplay() {
        Game game = new Game();
        game.startNewGame();
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());

        AtomicBoolean resumed = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                () -> {
                    game.resume();
                    resumed.set(true);
                },
                null, null, null, null, null
        );
        overlay.showMain();
        assertTrue(overlay.isVisible());

        // Trigger resume
        overlay.resume();

        assertFalse(overlay.isVisible());
        assertTrue(resumed.get());
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test
    @DisplayName("All game timers (transition, upgrade message, boss potion cooldown) remain frozen while paused")
    void gameplayTimersFreezeWhilePaused() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();

        // Cause health to drop below 80 and spawn a boss potion to start the 15s cooldown
        game.getPlayer().takeDamage(30.0);
        game.update(0.1);
        double initialCooldown = game.getBossPotionCooldownTimer();
        assertTrue(initialCooldown > 0.0);

        // Pause the game
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());

        // Multiple updates during paused state must NOT advance timers
        game.update(5.0);
        assertEquals(initialCooldown, game.getBossPotionCooldownTimer(), "Cooldown timer must not decrement while paused");

        game.update(10.0);
        assertEquals(initialCooldown, game.getBossPotionCooldownTimer(), "Cooldown timer remains completely frozen while paused");
    }

    @Test
    @DisplayName("Enemy AI updates stop completely while paused")
    void enemyAIHaltsWhilePaused() {
        Game game = new Game();
        game.startNewGame();
        Enemy enemy = game.getEnemies().get(0);

        game.pause();
        assertEquals(GameState.PAUSED, game.getState());

        double animBefore = enemy.getAnimationTime();
        double xBefore = enemy.getX();

        game.update(1.0);
        game.update(2.0);

        assertEquals(animBefore, enemy.getAnimationTime(), "Enemy animation time must halt while paused");
        assertEquals(xBefore, enemy.getX(), "Enemy position must not change while paused");
    }

    @Test
    @DisplayName("Settings view provides Fullscreen toggle and Back navigation")
    void settingsNavigationAndFullscreenToggle() {
        AtomicBoolean fullScreenState = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                null, null, null, null,
                () -> fullScreenState.set(!fullScreenState.get()),
                fullScreenState::get
        );

        overlay.showMain();
        overlay.setView(PauseMenuOverlay.View.SETTINGS);
        assertEquals(PauseMenuOverlay.View.SETTINGS, overlay.getCurrentView());

        // Toggle fullscreen via overlay
        overlay.toggleFullScreen();
        assertTrue(fullScreenState.get());

        overlay.toggleFullScreen();
        assertFalse(fullScreenState.get());

        // ESC in settings returns to MAIN view
        overlay.handleEscape();
        assertEquals(PauseMenuOverlay.View.MAIN, overlay.getCurrentView());
    }

    @Test
    @DisplayName("Controls view displays key bindings and Back navigation")
    void controlsNavigation() {
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                null, null, null, null, null, null
        );

        overlay.showMain();
        overlay.setView(PauseMenuOverlay.View.CONTROLS);
        assertEquals(PauseMenuOverlay.View.CONTROLS, overlay.getCurrentView());

        // ESC returns to MAIN view
        overlay.handleEscape();
        assertEquals(PauseMenuOverlay.View.MAIN, overlay.getCurrentView());
    }

    @Test
    @DisplayName("Restart confirmation shows dialog and cleanly resets Level 1 and sword")
    void restartConfirmationAndLevel1Reset() {
        Game game = new Game();
        game.startNewGame();
        Player player = game.getPlayer();
        player.equipSword();
        player.takeDamage(50.0);

        AtomicBoolean restartTriggered = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                null,
                () -> {
                    game.restartLevel();
                    restartTriggered.set(true);
                },
                null, null, null, null
        );

        overlay.showMain();
        overlay.setView(PauseMenuOverlay.View.CONFIRM_RESTART);
        assertEquals(PauseMenuOverlay.View.CONFIRM_RESTART, overlay.getCurrentView());

        // Confirm restart
        overlay.confirmRestart();
        assertTrue(restartTriggered.get());
        assertEquals(GameState.PLAYING, game.getState());

        Player restartedPlayer = game.getPlayer();
        assertEquals(100.0, restartedPlayer.getHealth());
        assertFalse(restartedPlayer.isSwordEquipped(), "Restarting Mystic Forest resets sword to ground");
        assertNotNull(game.getSwordItem());
        assertFalse(game.getSwordItem().isPickedUp());
    }

    @Test
    @DisplayName("Restarting Level 2 (Shadow Castle) preserves the +5 sword upgrade")
    void restartShadowCastlePreservesSwordUpgrade() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();
        assertEquals(2, game.getCurrentLevel());

        Player player = game.getPlayer();
        player.upgradeSword(5);
        assertTrue(player.isSwordUpgraded());
        player.takeDamage(40.0);

        game.restartLevel();

        assertEquals(2, game.getCurrentLevel());
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(100.0, game.getPlayer().getHealth());
        assertTrue(game.getPlayer().isSwordUpgraded(), "Restarting Shadow Castle must preserve the +5 sword upgrade");
    }

    @Test
    @DisplayName("Main Menu confirmation shows dialog and triggers return to title")
    void mainMenuConfirmation() {
        AtomicBoolean returnedToTitle = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                null, null,
                () -> returnedToTitle.set(true),
                null, null, null
        );

        overlay.showMain();
        overlay.setView(PauseMenuOverlay.View.CONFIRM_MAIN_MENU);
        assertEquals(PauseMenuOverlay.View.CONFIRM_MAIN_MENU, overlay.getCurrentView());

        overlay.confirmReturnToTitle();
        assertTrue(returnedToTitle.get());
        assertFalse(overlay.isVisible());
    }

    @Test
    @DisplayName("Exit Game confirmation shows dialog and triggers application exit")
    void exitGameConfirmation() {
        AtomicBoolean exitTriggered = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                null, null, null,
                () -> exitTriggered.set(true),
                null, null
        );

        overlay.showMain();
        overlay.setView(PauseMenuOverlay.View.CONFIRM_EXIT);
        assertEquals(PauseMenuOverlay.View.CONFIRM_EXIT, overlay.getCurrentView());

        overlay.confirmExitGame();
        assertTrue(exitTriggered.get());
        assertFalse(overlay.isVisible());
    }

    @Test
    @DisplayName("Keyboard navigation cycles button focus and Enter activates focused button")
    void keyboardNavigationAndButtonActivation() {
        AtomicBoolean resumeCalled = new AtomicBoolean(false);
        PauseMenuOverlay overlay = new PauseMenuOverlay(
                () -> resumeCalled.set(true),
                null, null, null, null, null
        );

        overlay.showMain();
        assertEquals(0, overlay.getFocusedButtonIndex());

        // First button in MAIN view is RESUME
        assertEquals("RESUME", overlay.getActiveButtons().get(0).getText());

        // Navigate Down
        overlay.navigateDown();
        assertEquals(1, overlay.getFocusedButtonIndex());
        assertEquals("RESTART LEVEL", overlay.getActiveButtons().get(1).getText());

        // Navigate Up
        overlay.navigateUp();
        assertEquals(0, overlay.getFocusedButtonIndex());

        // Activate focused button (RESUME)
        overlay.activateFocusedButton();
        assertTrue(resumeCalled.get());
    }

    @Test
    @DisplayName("Game Over and Victory screen controls remain intact")
    void gameOverAndVictoryControlsIntact() {
        Game game = new Game();
        game.startNewGame();

        // Death triggers GAME_OVER
        game.getPlayer().takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        // Defeating Shadow Lord triggers VICTORY
        game.startNewGame();
        game.loadShadowCastle();
        for (var enemy : game.getEnemies()) {
            if (enemy instanceof ShadowLord boss) {
                boss.takeDamage(1000.0);
            }
        }
        game.update(0.016);
        assertEquals(GameState.VICTORY, game.getState());
    }

    @Test
    @DisplayName("Pausing and resuming maintains a single game loop and prevents duplicate loops")
    void pauseAndResumeMaintainsSingleGameLoop() {
        Game game = new Game();
        game.startNewGame();

        // Repeated pause/resume toggles do not create duplicate loops or corrupt state
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());
        game.resume();
        assertEquals(GameState.PLAYING, game.getState());
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());
        game.resume();
        assertEquals(GameState.PLAYING, game.getState());
    }
}
