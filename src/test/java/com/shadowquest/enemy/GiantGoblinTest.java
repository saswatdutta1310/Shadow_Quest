package com.shadowquest.enemy;

import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GiantGoblin Level 1 Boss Stats & Activation")
class GiantGoblinTest {
    private GameWorld testWorld;

    @BeforeEach
    void setUp() {
        testWorld = new GameWorld(2000, 2000);
    }

    @Test
    @DisplayName("Default Giant Goblin attributes match boss specification")
    void defaultBossAttributes() {
        GiantGoblin boss = new GiantGoblin(500, 600);

        assertEquals("Giant Goblin", boss.getName());
        assertEquals(180.0, boss.getHealth());
        assertEquals(180.0, boss.getMaxHealth());
        assertEquals(18, boss.getAttackPower());
        assertEquals(6, boss.getDefense());
        assertEquals(85.0, boss.getSpeed());
        assertEquals(240.0, boss.getDetectionRadius());
        assertEquals(48.0, boss.getSize());
        assertEquals(45.0, boss.getStopDistance());
        assertEquals(30, boss.getRewardCoins());
        assertEquals(40.0, boss.getPatrolRadius());
        assertEquals(500.0, boss.getX());
        assertEquals(600.0, boss.getY());
        assertTrue(boss.isAlive());
        assertFalse(boss.isActive(), "Giant Goblin boss must start inactive");
    }

    @Test
    @DisplayName("Boss is visibly larger and more formidable than normal Goblin")
    void bossStatComparisonWithNormalGoblin() {
        Goblin normal = new Goblin(100, 100);
        GiantGoblin boss = new GiantGoblin(100, 100);

        assertTrue(boss.getSize() > normal.getSize(), "Boss physical size must exceed normal goblin");
        assertEquals(48.0, boss.getSize());
        assertEquals(24.0, normal.getSize());

        assertTrue(boss.getHealth() > normal.getHealth(), "Boss health must exceed normal goblin");
        assertTrue(boss.getAttackPower() > normal.getAttackPower(), "Boss attack must exceed normal goblin");
        assertTrue(boss.getDefense() > normal.getDefense(), "Boss defense must exceed normal goblin");
        assertTrue(boss.getSpeed() < normal.getSpeed(), "Boss should move slower than normal goblin");
    }

    @Test
    @DisplayName("Inactive boss does not move, chase, or update animation")
    void inactiveBossDoesNotMove() {
        GiantGoblin boss = new GiantGoblin(300, 300);
        assertFalse(boss.isActive());

        Player player = new Player("Arin");
        player.spawnAt(350, 300, testWorld);

        boss.update(1.0, player, testWorld);

        assertEquals(300.0, boss.getX());
        assertEquals(300.0, boss.getY());
        assertFalse(boss.isMoving());
        assertEquals(0.0, boss.getAnimationTime());
    }

    @Test
    @DisplayName("Active boss pursues player within detection radius")
    void activeBossPursuesPlayer() {
        GiantGoblin boss = new GiantGoblin(300, 300);
        boss.setActive(true);
        assertTrue(boss.isActive());

        Player player = new Player("Arin");
        player.spawnAt(450, 300, testWorld); // 150 units away (within 240 radius)

        boss.update(0.5, player, testWorld);

        assertTrue(boss.getX() > 300.0, "Boss should move right towards player");
        assertTrue(boss.isMoving());
        assertTrue(boss.isFacingRight());
        assertTrue(boss.getAnimationTime() > 0);
    }

    @Test
    @DisplayName("Activation condition requires all normal Goblins to be defeated")
    void activationConditionChecksNormalGoblins() {
        GiantGoblin boss = new GiantGoblin(500, 500);

        Goblin g1 = new Goblin(100, 100);
        Goblin g2 = new Goblin(200, 200);
        Goblin g3 = new Goblin(300, 300);
        ShadowWolf wolf = new ShadowWolf(400, 400);

        List<Enemy> enemies = List.of(g1, g2, g3, wolf, boss);

        // All 3 alive: cannot activate
        assertFalse(boss.checkActivationConditions(enemies));

        // 1 defeated: cannot activate
        g1.takeDamage(100);
        assertFalse(g1.isAlive());
        assertFalse(boss.checkActivationConditions(enemies));

        // 2 defeated: cannot activate
        g2.takeDamage(100);
        assertFalse(g2.isAlive());
        assertFalse(boss.checkActivationConditions(enemies));

        // All 3 defeated: can activate!
        g3.takeDamage(100);
        assertFalse(g3.isAlive());
        assertTrue(boss.checkActivationConditions(enemies));

        // Wolf status does not block goblin boss activation
        assertTrue(wolf.isAlive());
        assertTrue(boss.checkActivationConditions(enemies));
    }

    @Test
    @DisplayName("Activation condition rejects null or empty enemy lists")
    void activationConditionRejectsEmpty() {
        GiantGoblin boss = new GiantGoblin(500, 500);
        assertFalse(boss.checkActivationConditions(null));
        assertFalse(boss.checkActivationConditions(List.of()));
        assertFalse(boss.checkActivationConditions(List.of(boss)));
    }
}
