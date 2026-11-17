package com.shadowquest.core;

import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.items.HealthPotion;
import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Random Health Potion Spawns During Level 2 Shadow Lord Fight")
class BossFightPotionSpawnTest {

    @Test
    @DisplayName("Health potion does NOT spawn during Level 1 even if health is low")
    void doesNotSpawnInLevel1() {
        Game game = new Game();
        game.startNewGame();
        assertEquals(1, game.getCurrentLevel());

        Player player = game.getPlayer();
        player.takeDamage(35.0); // HP = 65 (< 80)
        assertTrue(player.getHealth() < 80.0);

        int initialPotions = game.getHealthPotions().size();
        game.update(1.0);

        assertEquals(initialPotions, game.getHealthPotions().size(), "Should NOT spawn boss fight potion in Level 1");
    }

    @Test
    @DisplayName("Health potion does NOT spawn in Level 2 if Arin health >= 80")
    void doesNotSpawnWhenHealthAbove80() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();
        assertEquals(2, game.getCurrentLevel());

        Player player = game.getPlayer();
        assertEquals(100.0, player.getHealth());

        int initialPotions = game.getHealthPotions().size();
        game.update(1.0);

        assertEquals(initialPotions, game.getHealthPotions().size(), "Should NOT spawn boss potion when HP >= 80");
        assertNull(game.getBossFightPotion());
    }

    @Test
    @DisplayName("Health potion spawns nearby (120-220 units) when HP < 80 during active Shadow Lord fight")
    void spawnsNearbyWhenHealthLowDuringBossFight() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();
        assertEquals(2, game.getCurrentLevel());

        Player player = game.getPlayer();
        player.takeDamage(30.0); // HP = 70.0 (< 80)

        int initialPotions = game.getHealthPotions().size();
        game.update(0.1);

        assertEquals(initialPotions + 1, game.getHealthPotions().size(), "Must spawn 1 boss health potion");
        HealthPotion potion = game.getBossFightPotion();
        assertNotNull(potion);
        assertFalse(potion.isPickedUp());
        assertEquals(35.0, potion.getHealAmount());

        double dist = Math.hypot(potion.getX() - player.getX(), potion.getY() - player.getY());
        assertTrue(dist >= 115.0 && dist <= 230.0, "Potion must spawn between 120 and 220 units from Arin (found: " + dist + ")");
    }

    @Test
    @DisplayName("Boss potion enforces 15s cooldown and max 1 active spawned potion at a time")
    void enforcesCooldownAndMaxOneActive() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();

        Player player = game.getPlayer();
        player.takeDamage(40.0); // HP = 60 (< 80)

        // Spawn first potion
        game.update(0.1);
        HealthPotion firstPotion = game.getBossFightPotion();
        assertNotNull(firstPotion);
        int potionCountAfterFirst = game.getHealthPotions().size();

        // Advance 5 seconds (still on cooldown, potion unpicked)
        game.update(5.0);
        assertEquals(potionCountAfterFirst, game.getHealthPotions().size(), "Must not spawn while on cooldown");

        // Advance past 15 seconds (cooldown expires, but first potion is STILL active / not picked up)
        game.update(12.0);
        assertEquals(potionCountAfterFirst, game.getHealthPotions().size(), "Must not spawn if previous boss potion is still active");

        // Pick up the first potion (move player to potion position)
        player.spawnAt(firstPotion.getX(), firstPotion.getY(), null);
        firstPotion.checkPickup(player);
        assertTrue(firstPotion.isPickedUp());

        // Ensure health remains < 80 for the next spawn trigger
        if (player.getHealth() >= 80.0) {
            player.takeDamage(25.0);
        }

        // Now with potion picked up and cooldown expired, next update can spawn a new one!
        game.update(0.1);
        assertEquals(potionCountAfterFirst + 1, game.getHealthPotions().size(), "New potion spawns after pickup and cooldown");
    }

    @Test
    @DisplayName("Health potions stop spawning once Shadow Lord is defeated")
    void stopsSpawningWhenBossDefeated() {
        Game game = new Game();
        game.startNewGame();
        game.loadShadowCastle();

        // Defeat Shadow Lord
        for (var enemy : game.getEnemies()) {
            if (enemy instanceof ShadowLord lord) {
                lord.takeDamage(1000.0);
                assertFalse(lord.isAlive());
            }
        }

        Player player = game.getPlayer();
        player.takeDamage(50.0); // HP = 50 (< 80)

        int initialPotions = game.getHealthPotions().size();
        game.update(0.1);

        assertEquals(initialPotions, game.getHealthPotions().size(), "Must NOT spawn potions once Shadow Lord is defeated");
    }
}
