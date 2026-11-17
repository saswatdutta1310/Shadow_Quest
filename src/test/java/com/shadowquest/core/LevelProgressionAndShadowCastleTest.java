package com.shadowquest.core;

import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.player.Player;
import com.shadowquest.world.ShadowCastle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Two-Level Progression: Mystic Forest to Shadow Castle & Victory")
class LevelProgressionAndShadowCastleTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startNewGame();
    }

    @Test
    @DisplayName("Level starts at 1 (Mystic Forest) with portal inactive and base sword power 10")
    void initialLevelState() {
        assertEquals(1, game.getCurrentLevel());
        assertNotNull(game.getPortal());
        assertFalse(game.getPortal().isActive());
        assertFalse(game.isSwordUpgraded());
        assertEquals(10, game.getPlayer().getAttackPower());
    }

    @Test
    @DisplayName("Defeating Giant Goblin awards +5 sword upgrade and activates the portal")
    void defeatingGiantGoblinAwardsUpgradeAndUnlocksPortal() {
        Player player = game.getPlayer();
        player.equipSword();

        GiantGoblin boss = findGiantGoblin();
        assertNotNull(boss);
        boss.setActive(true);

        // Boss defeat
        boss.takeDamage(boss.getMaxHealth());
        assertFalse(boss.isAlive());

        game.update(0.016);

        assertTrue(game.isSwordUpgraded(), "Sword must be upgraded after boss defeat");
        assertEquals(15, player.getAttackPower(), "Arin's attack power must increase by +5 (10 -> 15)");
        assertTrue(game.getSwordUpgradeMessageTimer() > 0, "Notification timer must be active");
        assertTrue(game.getPortal().isActive(), "Portal must unlock after boss defeat");
    }

    @Test
    @DisplayName("Sword upgrade is awarded exactly once and does not stack repeatedly")
    void swordUpgradeAwardedExactlyOnce() {
        Player player = game.getPlayer();
        player.equipSword();

        GiantGoblin boss = findGiantGoblin();
        boss.setActive(true);
        boss.takeDamage(boss.getMaxHealth());

        game.update(0.016);
        assertEquals(15, player.getAttackPower());

        // Subsequent frames do not add more attack bonus
        game.update(0.016);
        game.update(0.016);
        assertEquals(15, player.getAttackPower());
    }

    @Test
    @DisplayName("Cannot enter portal before boss defeat or when far away")
    void cannotEnterPortalPrematurelyOrFromDistance() {
        Player player = game.getPlayer();

        // 1. Far away at spawn (60, 110) while portal is inactive
        assertFalse(game.enterPortal());
        assertFalse(game.isTransitioning());

        // 2. Teleport near portal (1440, 480) while portal is still inactive
        player.spawnAt(1440, 480, game.getWorld());
        assertFalse(game.enterPortal());
        assertFalse(game.isTransitioning());
    }

    @Test
    @DisplayName("Entering active portal triggers 2.0-second black transition and prevents duplicate entries")
    void enterPortalStartsTimedTransition() {
        Player player = game.getPlayer();
        player.equipSword();

        GiantGoblin boss = findGiantGoblin();
        boss.setActive(true);
        boss.takeDamage(boss.getMaxHealth());
        game.update(0.016);
        assertTrue(game.getPortal().isActive());

        // Step beside portal and trigger interaction (Press E)
        player.spawnAt(game.getPortal().getX(), game.getPortal().getY(), game.getWorld());
        boolean entered = game.enterPortal();

        assertTrue(entered, "Entering active portal must succeed");
        assertTrue(game.isTransitioning());
        assertEquals(2.0, game.getTransitionTimer(), 0.001);

        // Immediate repeat call is blocked (prevents duplicate transitions)
        assertFalse(game.enterPortal());
    }

    @Test
    @DisplayName("Transition timer completes over 2 seconds and loads Shadow Castle with health and upgraded sword preserved")
    void transitionLoadsShadowCastleWithPreservedStats() {
        Player player = game.getPlayer();
        player.equipSword();
        player.takeDamage(30); // Player has 70 HP

        GiantGoblin boss = findGiantGoblin();
        boss.setActive(true);
        boss.takeDamage(boss.getMaxHealth());
        game.update(0.016);

        player.spawnAt(game.getPortal().getX(), game.getPortal().getY(), game.getWorld());
        assertTrue(game.enterPortal());
        assertTrue(game.isTransitioning());

        // Advance 1 second: still in transition
        game.update(1.0);
        assertTrue(game.isTransitioning());
        assertEquals(1, game.getCurrentLevel());

        // Advance 1.05 seconds: transition finishes, Level 2 loads!
        game.update(1.05);
        assertFalse(game.isTransitioning(), "Transition must conclude");
        assertEquals(2, game.getCurrentLevel(), "Must now be Level 2: Shadow Castle");

        // Player health (70) and upgraded sword damage (15) must be preserved
        assertEquals(70.0, player.getHealth(), 0.001, "Player health must carry over intact");
        assertEquals(15, player.getAttackPower(), "Upgraded sword power (15) must carry over into Castle");
        assertTrue(player.isSwordUpgraded());

        // Castle environment and enemies loaded
        assertEquals(ShadowCastle.WORLD_WIDTH, game.getWorld().getWidth());
        assertEquals(ShadowCastle.WORLD_HEIGHT, game.getWorld().getHeight());
        assertEquals(ShadowCastle.PLAYER_SPAWN_X, player.getX(), 0.001);
        assertEquals(ShadowCastle.PLAYER_SPAWN_Y, player.getY(), 0.001);

        // Shadow Lord exists and is active
        ShadowLord shadowLord = findShadowLord();
        assertNotNull(shadowLord, "Shadow Lord must be present in Shadow Castle");
        assertTrue(shadowLord.isActive());
        assertTrue(shadowLord.isAlive());
    }

    @Test
    @DisplayName("Defeating Shadow Lord triggers GameState.VICTORY")
    void defeatingShadowLordTriggersFinalVictory() {
        game.loadShadowCastle();
        assertEquals(2, game.getCurrentLevel());
        assertEquals(GameState.PLAYING, game.getState());

        ShadowLord shadowLord = findShadowLord();
        assertNotNull(shadowLord);

        // Defeat final boss
        shadowLord.takeDamage(shadowLord.getMaxHealth());
        assertFalse(shadowLord.isAlive());

        game.update(0.016);
        assertEquals(GameState.VICTORY, game.getState(), "Defeating Shadow Lord must trigger final victory state");
    }

    @Test
    @DisplayName("Level restart in Level 2 preserves upgraded sword power")
    void restartLevelInLevel2PreservesUpgradedSword() {
        game.loadShadowCastle();
        Player player = game.getPlayer();
        player.upgradeSword(5);
        assertEquals(15, player.getAttackPower());

        // Player dies in Level 2
        player.takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        // Restart level
        game.restartLevel();
        assertEquals(2, game.getCurrentLevel());
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(100.0, game.getPlayer().getHealth());
        assertEquals(15, game.getPlayer().getAttackPower(), "Upgraded sword must remain preserved when restarting Level 2");
    }

    @Test
    @DisplayName("Fresh game restart from main menu resets level to 1 and restores base sword power")
    void freshGameResetsUpgradeToBase() {
        game.loadShadowCastle();
        game.getPlayer().upgradeSword(5);
        assertEquals(15, game.getPlayer().getAttackPower());

        // Player is defeated or session ends
        game.getPlayer().takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        // Fresh game from Game Over or Menu
        game.startNewGame();
        assertEquals(1, game.getCurrentLevel(), "Fresh game must reset to Level 1");
        assertEquals(10, game.getPlayer().getAttackPower(), "Fresh game must restore base attack power to 10");
        assertFalse(game.isSwordUpgraded());
    }

    private GiantGoblin findGiantGoblin() {
        for (Enemy enemy : game.getEnemies()) {
            if (enemy instanceof GiantGoblin gg) {
                return gg;
            }
        }
        return null;
    }

    private ShadowLord findShadowLord() {
        for (Enemy enemy : game.getEnemies()) {
            if (enemy instanceof ShadowLord sl) {
                return sl;
            }
        }
        return null;
    }
}
