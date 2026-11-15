package com.shadowquest.enemy;

import com.shadowquest.combat.CombatSystem;
import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyAttackTest {
    private GameWorld world;
    private Player player;

    @BeforeEach
    void setUp() {
        world = new GameWorld(1000, 1000);
        player = new Player("Arin");
        player.spawnAt(100, 100, world);
    }

    @Test
    void goblinDefaultAttackStatsAndRange() {
        Goblin goblin = new Goblin(100, 100);
        assertEquals(Goblin.DEFAULT_ATTACK_RANGE, goblin.getAttackRange());
        assertEquals(Goblin.DEFAULT_ATTACK_WINDUP, goblin.getAttackWindup());
        assertEquals(Goblin.DEFAULT_ATTACK_DURATION, goblin.getAttackDuration());
        assertEquals(Goblin.DEFAULT_ATTACK_COOLDOWN, goblin.getAttackCooldown());
        assertFalse(goblin.isAttacking());
        assertEquals(0.0, goblin.getAttackTimer());
        assertEquals(0.0, goblin.getAttackCooldownTimer());
    }

    @Test
    void shadowWolfDefaultAttackStatsAndRange() {
        ShadowWolf wolf = new ShadowWolf(100, 100);
        assertEquals(ShadowWolf.DEFAULT_ATTACK_RANGE, wolf.getAttackRange());
        assertEquals(ShadowWolf.DEFAULT_ATTACK_WINDUP, wolf.getAttackWindup());
        assertEquals(ShadowWolf.DEFAULT_ATTACK_DURATION, wolf.getAttackDuration());
        assertEquals(ShadowWolf.DEFAULT_ATTACK_COOLDOWN, wolf.getAttackCooldown());
        assertTrue(wolf.getAttackRange() > Goblin.DEFAULT_ATTACK_RANGE);
        assertTrue(wolf.getAttackWindup() < Goblin.DEFAULT_ATTACK_WINDUP);
    }

    @Test
    void giantGoblinDefaultAttackStatsAndRange() {
        GiantGoblin boss = new GiantGoblin(100, 100);
        assertEquals(GiantGoblin.DEFAULT_ATTACK_RANGE, boss.getAttackRange());
        assertEquals(GiantGoblin.DEFAULT_ATTACK_WINDUP, boss.getAttackWindup());
        assertEquals(GiantGoblin.DEFAULT_ATTACK_DURATION, boss.getAttackDuration());
        assertEquals(GiantGoblin.DEFAULT_ATTACK_COOLDOWN, boss.getAttackCooldown());
        assertTrue(boss.getAttackRange() > Goblin.DEFAULT_ATTACK_RANGE);
        assertTrue(boss.getAttackWindup() > Goblin.DEFAULT_ATTACK_WINDUP);
        assertTrue(boss.getAttackDuration() > Goblin.DEFAULT_ATTACK_DURATION);
    }

    @Test
    void enemyApproachesUntilWithinAttackRangeThenStops() {
        // Goblin at (160, 100), distance to player (100, 100) is 60 (within detection 180, outside attack range 34)
        Goblin goblin = new Goblin(160, 100);
        assertTrue(goblin.isPlayerDetected(player));
        assertFalse(goblin.isPlayerInAttackRange(player));

        // Update moves goblin closer
        goblin.update(0.1, player, world);
        assertTrue(goblin.isMoving());
        assertTrue(goblin.getX() < 160);

        // Position goblin directly in attack range
        goblin.moveWithCollision(player.getX() + 10 - goblin.getX(), 0, world);
        assertTrue(goblin.isPlayerInAttackRange(player));
        goblin.update(0.01, player, world);
        assertFalse(goblin.isMoving());
    }

    @Test
    void enemyBeginsAttackWhenPlayerInAttackRange() {
        Goblin goblin = new Goblin(110, 100);
        assertTrue(goblin.isPlayerInAttackRange(player));
        assertFalse(goblin.isAttacking());

        goblin.update(0.01, player, world);
        assertTrue(goblin.isAttacking());
        assertFalse(goblin.isAttackDamageApplied());
        assertEquals(0.01, goblin.getAttackTimer(), 1e-4);
    }

    @Test
    void enemyAppliesDamageAtHitFrameAndNotBefore() {
        // Goblin deals Math.max(1, 8 - 5/2) = 6 damage to Arin (100 -> 94)
        Goblin goblin = new Goblin(110, 100);
        assertEquals(100.0, player.getHealth());

        // Update to start attack
        goblin.update(0.05, player, world);
        assertTrue(goblin.isAttacking());
        assertEquals(100.0, player.getHealth(), "No damage before windup hit frame");
        assertFalse(goblin.isAttackDamageApplied());

        // Advance to just before windup (windup is 0.25)
        goblin.update(0.15, player, world);
        assertEquals(0.20, goblin.getAttackTimer(), 1e-4);
        assertEquals(100.0, player.getHealth(), "Still no damage before windup threshold");
        assertFalse(goblin.isAttackDamageApplied());

        // Advance past windup hit frame (0.20 + 0.06 = 0.26 >= 0.25)
        goblin.update(0.06, player, world);
        assertTrue(goblin.isAttackDamageApplied());
        int expectedDamage = CombatSystem.calculateDamage(goblin, player);
        assertEquals(100.0 - expectedDamage, player.getHealth());
        assertTrue(player.isInvulnerable());
    }

    @Test
    void enemyCannotDamagePlayerIfPlayerMovesOutOfRangeBeforeHitFrame() {
        Goblin goblin = new Goblin(110, 100);
        goblin.update(0.10, player, world);
        assertTrue(goblin.isAttacking());

        // Player retreats far out of range before hit frame
        player.spawnAt(400, 400, world);
        assertFalse(goblin.isPlayerInAttackRange(player));

        // Advance past windup
        goblin.update(0.20, player, world);
        assertTrue(goblin.isAttackDamageApplied());
        assertEquals(100.0, player.getHealth(), "Player took no damage because they dodged out of range");
    }

    @Test
    void enemyRespectsAttackCooldown() {
        Goblin goblin = new Goblin(110, 100);
        goblin.update(0.01, player, world);
        assertTrue(goblin.isAttacking());

        // Complete the attack animation (total duration 0.40s)
        goblin.update(0.40, player, world);
        assertFalse(goblin.isAttacking(), "Attack finished");
        assertEquals(goblin.getAttackCooldown(), goblin.getAttackCooldownTimer(), 1e-4);

        // Even though player is still in range, enemy is on cooldown and cannot attack immediately
        goblin.update(0.50, player, world);
        assertFalse(goblin.isAttacking(), "Enemy must not attack while cooldown timer is active");
        assertTrue(goblin.getAttackCooldownTimer() > 0);

        // Advance 0.69s (just before cooldown ends)
        goblin.update(0.69, player, world);
        assertFalse(goblin.isAttacking(), "Still on cooldown");
        assertTrue(goblin.getAttackCooldownTimer() > 0);

        // Advance 0.05s (cooldown expires and new attack begins)
        goblin.update(0.05, player, world);
        assertTrue(goblin.isAttacking(), "New attack initiates once cooldown is cleared");
        assertEquals(0.0, goblin.getAttackCooldownTimer(), 1e-4);
    }

    @Test
    void deadEnemyCannotAttack() {
        Goblin goblin = new Goblin(110, 100);
        goblin.takeDamage(100);
        assertFalse(goblin.isAlive());

        goblin.update(0.1, player, world);
        assertFalse(goblin.isAttacking());
        assertFalse(goblin.isMoving());
    }

    @Test
    void inactiveEnemyCannotAttack() {
        GiantGoblin boss = new GiantGoblin(110, 100);
        assertFalse(boss.isActive());
        assertTrue(boss.isPlayerInAttackRange(player));

        boss.update(0.1, player, world);
        assertFalse(boss.isAttacking());
        assertFalse(boss.isMoving());
    }

    @Test
    void invalidAttackParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Enemy("BadRange", 50, 10, 2, 100, 150, 24, 20, 5, 0, 0, 50, -5, 0.2, 0.4, 1.0) {}
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Enemy("BadDuration", 50, 10, 2, 100, 150, 24, 20, 5, 0, 0, 50, 30, 0.5, 0.2, 1.0) {}
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Enemy("BadCooldown", 50, 10, 2, 100, 150, 24, 20, 5, 0, 0, 50, 30, 0.2, 0.4, -1.0) {}
        );
    }
}
