package com.shadowquest.enemy;

import com.shadowquest.player.Player;
import com.shadowquest.world.EnvironmentObject;
import com.shadowquest.world.GameWorld;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shadow Lord Flamethrower Attack & Melee Retention")
class ShadowLordFlamethrowerTest {

    @Test
    @DisplayName("Flamethrower engages at mid-range (65 < dist <= 280) with 0.55s windup and 3.50s cooldown")
    void flamethrowerStatsAndTimings() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1150, 500, null); // 150 units away

        boolean inRange = lord.isPlayerInAttackRange(player);
        assertTrue(inRange);
        assertTrue(lord.isFlamethrowerMode());
        assertEquals(0.55, lord.getAttackWindup(), 0.001);
        assertEquals(1.20, lord.getAttackDuration(), 0.001);
        assertEquals(3.50, lord.getAttackCooldown(), 0.001);
    }

    @Test
    @DisplayName("Melee engages at close range (dist <= 65) with 0.35s windup and 1.60s cooldown")
    void meleeStatsAndTimings() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1030, 500, null); // 30 units away

        boolean inRange = lord.isPlayerInAttackRange(player);
        assertTrue(inRange);
        assertEquals(ShadowLord.AttackMode.MELEE, lord.getCurrentAttackMode());
        assertEquals(0.35, lord.getAttackWindup(), 0.001);
        assertEquals(0.65, lord.getAttackDuration(), 0.001);
        assertEquals(1.60, lord.getAttackCooldown(), 0.001);
    }

    @Test
    @DisplayName("Flamethrower applies damage exactly once during sustained attack (controlled single hit)")
    void singleControlledDamageApplication() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1150, 500, null); // 150 units away
        double initialHp = player.getHealth();

        GameWorld emptyWorld = new GameWorld(2000, 2000, List.of());

        // Frame 1: starts attack
        lord.update(0.1, player, emptyWorld);
        assertTrue(lord.isAttacking());
        assertEquals(initialHp, player.getHealth(), "No damage during windup");

        // Advance to hit frame (windup is 0.55s, so at t=0.6s damage applies)
        lord.update(0.5, player, emptyWorld);
        assertTrue(lord.isAttackDamageApplied());
        double damagedHp = player.getHealth();
        assertTrue(damagedHp < initialHp, "Player takes damage on windup complete");

        // Advance several subsequent frames during active fire duration (1.20s total)
        lord.update(0.1, player, emptyWorld);
        lord.update(0.1, player, emptyWorld);
        lord.update(0.1, player, emptyWorld);

        assertEquals(damagedHp, player.getHealth(), "Player must NOT take per-frame damage during flamethrower stream");
    }

    @Test
    @DisplayName("Dodging out of cone or behind boss during windup avoids flamethrower damage")
    void dodgingAvoidsDamage() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1150, 500, null); // 150 units to the right
        double initialHp = player.getHealth();

        GameWorld emptyWorld = new GameWorld(2000, 2000, List.of());

        // Start attack aiming right
        lord.update(0.1, player, emptyWorld);
        assertTrue(lord.isAttacking());

        // Player dodges far behind boss (X=800, to the left) before hit frame
        player.spawnAt(800, 500, null);

        // Advance past windup
        lord.update(0.5, player, emptyWorld);
        assertTrue(lord.isAttackDamageApplied());
        assertEquals(initialHp, player.getHealth(), "Player dodged behind boss, so no damage taken!");
    }

    @Test
    @DisplayName("Solid fortress wall blocking line-of-sight protects player from flamethrower damage")
    void wallBlocksFlamethrowerDamage() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1200, 500, null); // 200 units away
        double initialHp = player.getHealth();

        // Solid rock wall between boss and player at X=1100
        EnvironmentObject wall = new EnvironmentObject(EnvironmentObject.Kind.ROCK, 1100, 450, 40, 100);
        GameWorld world = new GameWorld(2000, 2000, List.of(wall));

        // Start attack
        lord.update(0.1, player, world);
        assertTrue(lord.isAttacking());

        // Advance past windup
        lord.update(0.5, player, world);
        assertTrue(lord.isAttackDamageApplied());
        assertEquals(initialHp, player.getHealth(), "Solid wall must block flamethrower damage");
    }
}
