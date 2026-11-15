package com.shadowquest.enemy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShadowWolf Enemy Stats & Construction")
class ShadowWolfTest {

    @Test
    @DisplayName("Default ShadowWolf attributes match specification")
    void defaultAttributes() {
        ShadowWolf wolf = new ShadowWolf(250, 350);

        assertEquals("ShadowWolf", wolf.getName());
        assertEquals(35, wolf.getHealth());
        assertEquals(35, wolf.getMaxHealth());
        assertEquals(10, wolf.getAttackPower());
        assertEquals(1, wolf.getDefense());
        assertEquals(160.0, wolf.getSpeed());
        assertEquals(220.0, wolf.getDetectionRadius());
        assertEquals(28.0, wolf.getSize());
        assertEquals(32.0, wolf.getStopDistance());
        assertEquals(8, wolf.getRewardCoins());
        assertEquals(70.0, wolf.getPatrolRadius());
        assertEquals(250, wolf.getX());
        assertEquals(350, wolf.getY());
        assertTrue(wolf.isAlive());
        assertTrue(wolf.isFacingRight());
    }

    @Test
    @DisplayName("Custom patrol radius and custom name constructors")
    void customConstructors() {
        ShadowWolf customWolf = new ShadowWolf(500, 600, 90.0);
        assertEquals(90.0, customWolf.getPatrolRadius());
        assertEquals(500, customWolf.getX());
        assertEquals(600, customWolf.getY());

        ShadowWolf namedWolf = new ShadowWolf("Dire Wolf", 100, 150, 45.0);
        assertEquals("Dire Wolf", namedWolf.getName());
        assertEquals(35, namedWolf.getHealth());
        assertEquals(45.0, namedWolf.getPatrolRadius());
    }

    @Test
    @DisplayName("ShadowWolf takes damage and dies appropriately")
    void wolfDamageAndDeath() {
        ShadowWolf wolf = new ShadowWolf(100, 100);
        wolf.takeDamage(15);
        assertEquals(20, wolf.getHealth());
        assertTrue(wolf.isAlive());

        wolf.takeDamage(25);
        assertEquals(0, wolf.getHealth());
        assertFalse(wolf.isAlive());
    }
}
