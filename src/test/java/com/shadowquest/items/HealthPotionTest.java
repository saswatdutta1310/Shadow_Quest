package com.shadowquest.items;

import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Health Potion Pickup & Healing")
class HealthPotionTest {

    @Test
    @DisplayName("Health potion initializes with given coordinates and default heal amount")
    void initialState() {
        HealthPotion potion = new HealthPotion(480, 150);

        assertEquals(480, potion.getX());
        assertEquals(150, potion.getY());
        assertEquals(HealthPotion.DEFAULT_HEAL_AMOUNT, potion.getHealAmount());
        assertEquals(HealthPotion.DEFAULT_PICKUP_RADIUS, potion.getPickupRadius());
        assertFalse(potion.isConsumed());
    }

    @Test
    @DisplayName("Custom heal amount and pickup radius")
    void customConstructor() {
        HealthPotion potion = new HealthPotion(200, 300, 50, 40);

        assertEquals(200, potion.getX());
        assertEquals(300, potion.getY());
        assertEquals(50, potion.getHealAmount());
        assertEquals(40, potion.getPickupRadius());
        assertFalse(potion.isConsumed());
    }

    @Test
    @DisplayName("Wounded player picks up potion and heals by heal amount")
    void healsWoundedPlayer() {
        HealthPotion potion = new HealthPotion(480, 150, 35);
        Player player = new Player("Arin");
        player.spawnAt(480, 150, null);
        player.takeDamage(50); // HP: 50/100

        boolean consumed = potion.checkPickup(player);

        assertTrue(consumed, "Potion should be consumed by wounded player in range");
        assertTrue(potion.isConsumed());
        assertEquals(85, player.getHealth(), 0.001, "Player health should increase from 50 to 85");
    }

    @Test
    @DisplayName("Healing does not exceed player max health")
    void healingCapsAtMaxHealth() {
        HealthPotion potion = new HealthPotion(480, 150, 35);
        Player player = new Player("Arin");
        player.spawnAt(480, 150, null);
        player.takeDamage(10); // HP: 90/100

        boolean consumed = potion.checkPickup(player);

        assertTrue(consumed);
        assertTrue(potion.isConsumed());
        assertEquals(100, player.getHealth(), 0.001, "Player health cannot exceed max 100");
    }

    @Test
    @DisplayName("Full health player does NOT consume potion so it is preserved on ground")
    void fullHealthPlayerPreservesPotion() {
        HealthPotion potion = new HealthPotion(480, 150);
        Player player = new Player("Arin");
        player.spawnAt(480, 150, null);
        assertEquals(100, player.getHealth(), 0.001);

        boolean consumed = potion.checkPickup(player);

        assertFalse(consumed, "Full health player must not consume potion");
        assertFalse(potion.isConsumed(), "Potion must remain unconsumed on the ground");
        assertEquals(100, player.getHealth(), 0.001);
    }

    @Test
    @DisplayName("Potion is not consumed when player is outside pickup range")
    void notConsumedOutsideRange() {
        HealthPotion potion = new HealthPotion(480, 150);
        Player player = new Player("Arin");
        player.spawnAt(100, 100, null); // far away
        player.takeDamage(50);

        boolean consumed = potion.checkPickup(player);

        assertFalse(consumed);
        assertFalse(potion.isConsumed());
        assertEquals(50, player.getHealth(), 0.001);
    }

    @Test
    @DisplayName("Cannot consume potion more than once")
    void cannotConsumeTwice() {
        HealthPotion potion = new HealthPotion(480, 150, 35);
        Player player = new Player("Arin");
        player.spawnAt(480, 150, null);
        player.takeDamage(80); // HP: 20

        assertTrue(potion.checkPickup(player));
        assertEquals(55, player.getHealth(), 0.001);

        // Try picking up again
        assertFalse(potion.checkPickup(player));
        assertEquals(55, player.getHealth(), 0.001);
    }

    @Test
    @DisplayName("Dead or null player cannot consume potion")
    void deadOrNullPlayer() {
        HealthPotion potion = new HealthPotion(480, 150);

        assertFalse(potion.checkPickup(null));
        assertFalse(potion.isConsumed());

        Player deadPlayer = new Player("Arin");
        deadPlayer.spawnAt(480, 150, null);
        deadPlayer.takeDamage(100);
        assertFalse(deadPlayer.isAlive());

        assertFalse(potion.checkPickup(deadPlayer));
        assertFalse(potion.isConsumed());
    }

    @Test
    @DisplayName("Constructor parameter validation")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new HealthPotion(Double.NaN, 100));
        assertThrows(IllegalArgumentException.class, () -> new HealthPotion(100, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new HealthPotion(100, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new HealthPotion(100, 100, -10));
        assertThrows(IllegalArgumentException.class, () -> new HealthPotion(100, 100, 30, -5));
    }
}
