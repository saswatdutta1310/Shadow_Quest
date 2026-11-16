package com.shadowquest.core;

import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.items.SwordItem;
import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameOverAndRestartTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startNewGame();
    }

    @Test
    void playerDeathTransitionsGameToGameOverOnUpdate() {
        assertEquals(GameState.PLAYING, game.getState());
        Player player = game.getPlayer();
        player.takeDamage(100.0);
        assertFalse(player.isAlive());

        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    void gameOverHaltsPlayerMovementAndAttacks() {
        Player player = game.getPlayer();
        player.spawnAt(100, 100, game.getWorld());
        player.takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        double oldX = player.getX();
        double oldY = player.getY();
        game.movePlayer(1, 0, 0.5);
        assertEquals(oldX, player.getX(), "Player must not move during Game Over");
        assertEquals(oldY, player.getY());

        boolean attacked = game.attackPlayer();
        assertFalse(attacked, "Player must not attack during Game Over");
    }

    @Test
    void gameOverHaltsEnemyAIUpdates() {
        Player player = game.getPlayer();
        player.spawnAt(100, 100, game.getWorld());
        List<Enemy> enemies = game.getEnemies();
        assertFalse(enemies.isEmpty());

        Enemy firstEnemy = enemies.get(0);
        double enemyXBeforeDeath = firstEnemy.getX();
        double enemyYBeforeDeath = firstEnemy.getY();

        player.takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        // Subsequent game updates during GAME_OVER must not advance enemy AI
        game.update(1.0);
        assertEquals(enemyXBeforeDeath, firstEnemy.getX(), "Enemy AI must halt during Game Over");
        assertEquals(enemyYBeforeDeath, firstEnemy.getY());
    }

    @Test
    void restartLevelRestoresPlayerHealthAndSpawnPosition() {
        Player player = game.getPlayer();
        player.takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        game.restartLevel();
        assertEquals(GameState.PLAYING, game.getState());
        Player restartedPlayer = game.getPlayer();
        assertNotNull(restartedPlayer);
        assertTrue(restartedPlayer.isAlive());
        assertEquals(100.0, restartedPlayer.getHealth());
        assertEquals(60.0, restartedPlayer.getX());
        assertEquals(110.0, restartedPlayer.getY());
        assertFalse(restartedPlayer.isSwordEquipped());
    }

    @Test
    void restartLevelResetsEnemiesAndGiantGoblinInactive() {
        // Kill all normal goblins to activate Giant Goblin
        for (Enemy enemy : game.getEnemies()) {
            if (enemy instanceof Goblin && !(enemy instanceof GiantGoblin)) {
                enemy.takeDamage(100.0);
            }
        }
        game.update(0.016);

        // Find giant goblin and verify it awakened
        GiantGoblin boss = null;
        for (Enemy enemy : game.getEnemies()) {
            if (enemy instanceof GiantGoblin gg) {
                boss = gg;
            }
        }
        assertNotNull(boss);
        assertTrue(boss.isActive());

        // Trigger game over
        game.getPlayer().takeDamage(100.0);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        // Restart level
        game.restartLevel();
        assertEquals(GameState.PLAYING, game.getState());

        // Verify enemies are repopulated, all alive, and Giant Goblin is inactive
        List<Enemy> refreshedEnemies = game.getEnemies();
        assertEquals(7, refreshedEnemies.size()); // 3 Goblins, 3 Wolves, 1 Boss
        for (Enemy enemy : refreshedEnemies) {
            assertTrue(enemy.isAlive(), enemy.getName() + " should be alive after restart");
            if (enemy instanceof GiantGoblin gg) {
                assertFalse(gg.isActive(), "Giant Goblin boss must reset to inactive");
            }
        }
    }

    @Test
    void restartLevelResetsSwordToGround() {
        SwordItem sword = game.getSwordItem();
        assertNotNull(sword);
        Player player = game.getPlayer();
        player.spawnAt(sword.getX(), sword.getY(), game.getWorld());
        game.update(0.016);
        assertTrue(sword.isPickedUp());
        assertTrue(player.isSwordEquipped());

        // Trigger game over and restart
        player.takeDamage(100.0);
        game.update(0.016);
        game.restartLevel();

        SwordItem refreshedSword = game.getSwordItem();
        assertNotNull(refreshedSword);
        assertFalse(refreshedSword.isPickedUp(), "Sword must be back on the ground");
        assertFalse(game.getPlayer().isSwordEquipped(), "Player must not have sword equipped initially");
    }

    @Test
    void restartLevelResetsHealthPotionsAndPowerUp() {
        assertEquals(3, game.getHealthPotions().size());
        com.shadowquest.items.HealthPotion potion = game.getHealthPotions().get(0);
        Player player = game.getPlayer();
        player.spawnAt(potion.getX(), potion.getY(), game.getWorld());
        player.takeDamage(50);
        game.update(0.016);
        assertTrue(potion.isConsumed(), "Potion should be consumed");
        assertEquals(85, player.getHealth(), 0.001);

        // Simulate Giant Goblin defeat to spawn PowerUpItem
        GiantGoblin boss = null;
        for (Enemy enemy : game.getEnemies()) {
            if (enemy instanceof GiantGoblin gg) {
                boss = gg;
            }
        }
        assertNotNull(boss);
        boss.setActive(true);
        boss.takeDamage(200); // Defeat boss
        assertFalse(boss.isAlive());

        game.update(0.016);
        assertNotNull(game.getPowerUpItem(), "Defeating Giant Goblin must spawn PowerUpItem");

        // Collect power up
        player.spawnAt(game.getPowerUpItem().getX(), game.getPowerUpItem().getY(), game.getWorld());
        game.update(0.016);
        assertTrue(game.getPowerUpItem().isCollected());
        assertEquals(13, player.getAttackBonus());

        // Now trigger game over and restart
        player.takeDamage(100);
        game.update(0.016);
        assertEquals(GameState.GAME_OVER, game.getState());

        game.restartLevel();
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(3, game.getHealthPotions().size());
        for (com.shadowquest.items.HealthPotion p : game.getHealthPotions()) {
            assertFalse(p.isConsumed(), "All potions must be unconsumed after restart");
        }
        assertNull(game.getPowerUpItem(), "PowerUpItem must be cleared on restart");
        assertEquals(0, game.getPlayer().getAttackBonus(), "Player attack bonus must reset to 0");
    }
}
