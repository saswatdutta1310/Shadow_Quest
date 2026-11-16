package com.shadowquest.items;

import com.shadowquest.combat.CombatSystem;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Boss Defeat Power-Up Item & Damage Scaling")
class PowerUpItemTest {

    @Test
    @DisplayName("PowerUpItem initializes with given coordinates and default attack bonus")
    void initialState() {
        PowerUpItem powerUp = new PowerUpItem(1100, 260);

        assertEquals(1100, powerUp.getX());
        assertEquals(260, powerUp.getY());
        assertEquals(PowerUpItem.DEFAULT_ATTACK_BONUS, powerUp.getAttackBonus());
        assertEquals(PowerUpItem.DEFAULT_PICKUP_RADIUS, powerUp.getPickupRadius());
        assertFalse(powerUp.isCollected());
    }

    @Test
    @DisplayName("Player picks up power-up and receives permanent attack bonus")
    void playerPicksUpPowerUp() {
        PowerUpItem powerUp = new PowerUpItem(1100, 260, 8);
        Player player = new Player("Arin");
        assertEquals(0, player.getAttackBonus());
        assertEquals(10, player.getAttackPower()); // Base attack power

        player.spawnAt(1100, 260, null);
        boolean collected = powerUp.checkPickup(player);

        assertTrue(collected, "Power up should be collected when player in range");
        assertTrue(powerUp.isCollected());
        assertEquals(8, player.getAttackBonus());
        assertEquals(18, player.getAttackPower(), "Player attack power should be 10 + 8 = 18");
    }

    @Test
    @DisplayName("Power-up increases player combat damage dealt to enemies")
    void powerUpIncreasesCombatDamage() {
        Player player = new Player("Arin");
        Goblin goblin = new Goblin(100, 100); // Goblin defense = 2

        // Before power-up: attack = 10, damage = 10 - (2 / 2) = 9
        double baseDamage = CombatSystem.calculateDamage(player, goblin);
        assertEquals(9.0, baseDamage, 0.001);

        // Collect power-up (+8 attack)
        PowerUpItem powerUp = new PowerUpItem(100, 100, 8);
        player.spawnAt(100, 100, null);
        assertTrue(powerUp.checkPickup(player));

        // After power-up: attack = 18, damage = 18 - (2 / 2) = 17
        double boostedDamage = CombatSystem.calculateDamage(player, goblin);
        assertEquals(17.0, boostedDamage, 0.001, "Combat damage should scale to 17 with power-up");
    }

    @Test
    @DisplayName("Power-up remains uncollected when player is outside pickup range")
    void notCollectedOutsideRange() {
        PowerUpItem powerUp = new PowerUpItem(1100, 260);
        Player player = new Player("Arin");
        player.spawnAt(100, 100, null); // far away

        boolean collected = powerUp.checkPickup(player);

        assertFalse(collected);
        assertFalse(powerUp.isCollected());
        assertEquals(0, player.getAttackBonus());
        assertEquals(10, player.getAttackPower());
    }

    @Test
    @DisplayName("Power-up cannot be collected more than once")
    void cannotCollectTwice() {
        PowerUpItem powerUp = new PowerUpItem(1100, 260, 8);
        Player player = new Player("Arin");
        player.spawnAt(1100, 260, null);

        assertTrue(powerUp.checkPickup(player));
        assertEquals(18, player.getAttackPower());

        // Second pickup attempt fails and does not stack
        assertFalse(powerUp.checkPickup(player));
        assertEquals(18, player.getAttackPower());
    }

    @Test
    @DisplayName("Dead or null player cannot collect power-up")
    void deadOrNullPlayer() {
        PowerUpItem powerUp = new PowerUpItem(1100, 260);

        assertFalse(powerUp.checkPickup(null));
        assertFalse(powerUp.isCollected());

        Player deadPlayer = new Player("Arin");
        deadPlayer.spawnAt(1100, 260, null);
        deadPlayer.takeDamage(100);
        assertFalse(deadPlayer.isAlive());

        assertFalse(powerUp.checkPickup(deadPlayer));
        assertFalse(powerUp.isCollected());
    }

    @Test
    @DisplayName("Constructor parameter validation")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new PowerUpItem(Double.NaN, 100));
        assertThrows(IllegalArgumentException.class, () -> new PowerUpItem(100, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new PowerUpItem(100, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new PowerUpItem(100, 100, -5));
        assertThrows(IllegalArgumentException.class, () -> new PowerUpItem(100, 100, 8, -10));
    }
}
