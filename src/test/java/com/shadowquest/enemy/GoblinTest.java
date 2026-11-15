package com.shadowquest.enemy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Goblin Enemy Stats & Construction")
class GoblinTest {

    @Test
    @DisplayName("Default Goblin attributes match specification")
    void defaultAttributes() {
        Goblin goblin = new Goblin(150, 200);

        assertEquals("Goblin", goblin.getName());
        assertEquals(45, goblin.getHealth());
        assertEquals(45, goblin.getMaxHealth());
        assertEquals(8, goblin.getAttackPower());
        assertEquals(2, goblin.getDefense());
        assertEquals(110.0, goblin.getSpeed());
        assertEquals(180.0, goblin.getDetectionRadius());
        assertEquals(24.0, goblin.getSize());
        assertEquals(28.0, goblin.getStopDistance());
        assertEquals(5, goblin.getRewardCoins());
        assertEquals(50.0, goblin.getPatrolRadius());
        assertEquals(150, goblin.getX());
        assertEquals(200, goblin.getY());
        assertTrue(goblin.isAlive());
        assertTrue(goblin.isFacingRight());
    }

    @Test
    @DisplayName("Custom patrol radius and custom name constructors")
    void customConstructors() {
        Goblin customGoblin = new Goblin(300, 400, 75.0);
        assertEquals(75.0, customGoblin.getPatrolRadius());
        assertEquals(300, customGoblin.getX());
        assertEquals(400, customGoblin.getY());

        Goblin namedGoblin = new Goblin("Sneaky Goblin", 50, 60, 30.0);
        assertEquals("Sneaky Goblin", namedGoblin.getName());
        assertEquals(45, namedGoblin.getHealth());
        assertEquals(30.0, namedGoblin.getPatrolRadius());
    }

    @Test
    @DisplayName("Goblin takes damage and dies appropriately")
    void goblinDamageAndDeath() {
        Goblin goblin = new Goblin(100, 100);
        goblin.takeDamage(20);
        assertEquals(25, goblin.getHealth());
        assertTrue(goblin.isAlive());

        goblin.takeDamage(30);
        assertEquals(0, goblin.getHealth());
        assertFalse(goblin.isAlive());
    }
}
