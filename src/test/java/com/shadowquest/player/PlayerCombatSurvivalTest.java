package com.shadowquest.player;

import com.shadowquest.world.GameWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerCombatSurvivalTest {
    private Player player;
    private GameWorld world;

    @BeforeEach
    void setUp() {
        world = new GameWorld(1000, 1000);
        player = new Player("Arin");
        player.spawnAt(100, 100, world);
    }

    @Test
    void playerHealthReducesOnCombatDamage() {
        assertEquals(100.0, player.getHealth());
        boolean applied = player.takeCombatDamage(15.0);
        assertTrue(applied);
        assertEquals(85.0, player.getHealth());
    }

    @Test
    void combatDamageTriggersInvulnerabilityAndDamageFlash() {
        assertFalse(player.isInvulnerable());
        assertFalse(player.isDamageFlashing());

        player.takeCombatDamage(10.0);
        assertTrue(player.isInvulnerable());
        assertTrue(player.isDamageFlashing());
        assertEquals(Player.INVULNERABILITY_DURATION, player.getInvulnerabilityTimer(), 1e-4);
        assertEquals(Player.DAMAGE_FLASH_DURATION, player.getDamageFlashTimer(), 1e-4);
    }

    @Test
    void playerDoesNotTakeRepeatedDamageDuringInvulnerability() {
        player.takeCombatDamage(10.0);
        assertEquals(90.0, player.getHealth());
        assertTrue(player.isInvulnerable());

        // Repeated damage during invulnerability must be rejected
        boolean appliedSecondHit = player.takeCombatDamage(15.0);
        assertFalse(appliedSecondHit);
        assertEquals(90.0, player.getHealth(), "Health must not decrease during invulnerability period");

        boolean appliedThirdHit = player.takeCombatDamage(20.0);
        assertFalse(appliedThirdHit);
        assertEquals(90.0, player.getHealth());
    }

    @Test
    void invulnerabilityTimerDecrementsWithDeltaTime() {
        player.takeCombatDamage(10.0);
        assertTrue(player.isInvulnerable());
        assertTrue(player.isDamageFlashing());

        // Advance past damage flash duration (0.20s)
        player.update(0.25);
        assertFalse(player.isDamageFlashing(), "Damage flash expires after 0.20s");
        assertTrue(player.isInvulnerable(), "Invulnerability persists for 0.90s");
        assertEquals(Player.INVULNERABILITY_DURATION - 0.25, player.getInvulnerabilityTimer(), 1e-4);

        // Advance to full expiration (0.90 - 0.25 = 0.65 remaining)
        player.update(0.70);
        assertFalse(player.isInvulnerable(), "Invulnerability must expire after duration");
        assertEquals(0.0, player.getInvulnerabilityTimer(), 1e-4);
    }

    @Test
    void playerCanTakeDamageAgainAfterInvulnerabilityExpires() {
        player.takeCombatDamage(10.0);
        assertEquals(90.0, player.getHealth());

        player.update(Player.INVULNERABILITY_DURATION + 0.05);
        assertFalse(player.isInvulnerable());

        boolean appliedNextHit = player.takeCombatDamage(15.0);
        assertTrue(appliedNextHit);
        assertEquals(75.0, player.getHealth());
    }

    @Test
    void healthCannotBecomeNegative() {
        player.takeCombatDamage(60.0);
        assertEquals(40.0, player.getHealth());

        player.update(Player.INVULNERABILITY_DURATION + 0.05);
        player.takeCombatDamage(100.0);
        assertEquals(0.0, player.getHealth(), "Health must clamp at 0 and never become negative");
        assertFalse(player.isAlive());
    }

    @Test
    void zeroOrNegativeDamageRejected() {
        boolean appliedZero = player.takeCombatDamage(0.0);
        assertFalse(appliedZero);
        assertEquals(100.0, player.getHealth());

        boolean appliedNegative = player.takeCombatDamage(-10.0);
        assertFalse(appliedNegative);
        assertEquals(100.0, player.getHealth());
    }

    @Test
    void deadPlayerCannotTakeDamage() {
        player.takeDamage(100.0);
        assertFalse(player.isAlive());

        boolean applied = player.takeCombatDamage(20.0);
        assertFalse(applied);
        assertEquals(0.0, player.getHealth());
    }
}
