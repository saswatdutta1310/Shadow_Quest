package com.shadowquest.enemy;

import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.ShadowCastle;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Castle Enemies: Shadow Knight, Dark Archer, Wraith & Castle Spawning")
class CastleEnemiesTest {

    @Test
    @DisplayName("Shadow Castle spawns only castle-themed enemies and final boss (no Goblins or Wolves)")
    void castleEnemiesExclusivity() {
        List<Enemy> enemies = ShadowCastle.createEnemies();

        assertNotNull(enemies);
        assertFalse(enemies.isEmpty());

        long goblinCount = enemies.stream().filter(e -> e instanceof Goblin).count();
        long wolfCount = enemies.stream().filter(e -> e instanceof ShadowWolf).count();
        long knightCount = enemies.stream().filter(e -> e instanceof ShadowKnight).count();
        long archerCount = enemies.stream().filter(e -> e instanceof DarkArcher).count();
        long wraithCount = enemies.stream().filter(e -> e instanceof Wraith).count();
        long lordCount = enemies.stream().filter(e -> e instanceof ShadowLord).count();

        assertEquals(0, goblinCount, "Shadow Castle must have NO Goblins");
        assertEquals(0, wolfCount, "Shadow Castle must have NO Wolves");

        assertTrue(knightCount >= 1, "Must contain Shadow Knights");
        assertTrue(archerCount >= 1, "Must contain Dark Archers");
        assertTrue(wraithCount >= 1, "Must contain Wraiths");
        assertEquals(1, lordCount, "Must contain exactly one Shadow Lord final boss");
    }

    @Test
    @DisplayName("Shadow Knight initializes with heavy melee tank stats")
    void shadowKnightStats() {
        ShadowKnight knight = new ShadowKnight(400, 300);

        assertEquals("Shadow Knight", knight.getName());
        assertEquals(75.0, knight.getMaxHealth());
        assertEquals(75.0, knight.getHealth());
        assertEquals(14, knight.getAttackPower());
        assertEquals(4, knight.getDefense());
        assertEquals(95.0, knight.getSpeed());
        assertEquals(220.0, knight.getDetectionRadius());
        assertEquals(32.0, knight.getSize());
        assertEquals(34.0, knight.getStopDistance());
        assertEquals(42.0, knight.getAttackRange());
        assertEquals(0.30, knight.getAttackWindup());
        assertEquals(0.50, knight.getAttackDuration());
        assertEquals(1.40, knight.getAttackCooldown());
    }

    @Test
    @DisplayName("Dark Archer initializes with ranged kiting stats")
    void darkArcherStats() {
        DarkArcher archer = new DarkArcher(500, 300);

        assertEquals("Dark Archer", archer.getName());
        assertEquals(50.0, archer.getMaxHealth());
        assertEquals(50.0, archer.getHealth());
        assertEquals(11, archer.getAttackPower());
        assertEquals(2, archer.getDefense());
        assertEquals(105.0, archer.getSpeed());
        assertEquals(280.0, archer.getDetectionRadius());
        assertEquals(26.0, archer.getSize());
        assertEquals(130.0, archer.getStopDistance());
        assertEquals(230.0, archer.getAttackRange());
        assertEquals(0.35, archer.getAttackWindup());
        assertEquals(0.60, archer.getAttackDuration());
        assertEquals(1.80, archer.getAttackCooldown());
    }

    @Test
    @DisplayName("Dark Archer retreats/kites backwards when player enters close proximity (< 110 units)")
    void darkArcherKitesBackwards() {
        DarkArcher archer = new DarkArcher(500, 300);
        Player player = new Player("Arin");
        player.spawnAt(550, 300, null); // 50 units away (inside KITE_RETREAT_DISTANCE 110)

        double initialArcherX = archer.getX();

        // Update AI: player is at X=550 (to the right). Archer should retreat to the left (dx < 0)!
        GameWorld emptyWorld = new GameWorld(2000, 2000, List.of());
        archer.update(0.1, player, emptyWorld);

        assertTrue(archer.getX() < initialArcherX, "Dark Archer should retreat away from player when within 110 units");
    }

    @Test
    @DisplayName("Wraith initializes with fast floating spectral stats")
    void wraithStats() {
        Wraith wraith = new Wraith(600, 300);

        assertEquals("Wraith", wraith.getName());
        assertEquals(60.0, wraith.getMaxHealth());
        assertEquals(60.0, wraith.getHealth());
        assertEquals(13, wraith.getAttackPower());
        assertEquals(1, wraith.getDefense());
        assertEquals(125.0, wraith.getSpeed());
        assertEquals(240.0, wraith.getDetectionRadius());
        assertEquals(28.0, wraith.getSize());
        assertEquals(32.0, wraith.getStopDistance());
        assertEquals(48.0, wraith.getAttackRange());
        assertEquals(0.25, wraith.getAttackWindup());
        assertEquals(0.45, wraith.getAttackDuration());
        assertEquals(1.20, wraith.getAttackCooldown());
    }
}
