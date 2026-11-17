package com.shadowquest.enemy;

import com.shadowquest.combat.CombatSystem;
import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shadow Lord Final Boss")
class ShadowLordTest {

    @Test
    @DisplayName("Shadow Lord initializes with towering boss stats and active state")
    void initialStateAndStats() {
        ShadowLord lord = new ShadowLord(1900, 650);

        assertEquals("Shadow Lord", lord.getName());
        assertEquals(350.0, lord.getMaxHealth());
        assertEquals(350.0, lord.getHealth());
        assertEquals(24, lord.getAttackPower());
        assertEquals(8, lord.getDefense());
        assertEquals(70.0, lord.getSpeed());
        assertEquals(64.0, lord.getSize());
        assertTrue(lord.isActive(), "Shadow Lord must be active upon entering Shadow Castle");
        assertTrue(lord.isAlive());
    }

    @Test
    @DisplayName("Melee attack mode engages when player is within 65 units")
    void meleeAttackModeWithinCloseRange() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1030, 500, null); // distance ~30 units

        boolean inRange = lord.isPlayerInAttackRange(player);

        assertTrue(inRange);
        assertEquals(ShadowLord.AttackMode.MELEE, lord.getCurrentAttackMode());
    }

    @Test
    @DisplayName("Ranged attack mode engages when player is between 65 and 260 units")
    void rangedAttackModeWithinMidRange() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1150, 500, null); // distance ~150 units

        boolean inRange = lord.isPlayerInAttackRange(player);

        assertTrue(inRange);
        assertEquals(ShadowLord.AttackMode.RANGED, lord.getCurrentAttackMode());
    }

    @Test
    @DisplayName("Player outside 260 units is not in attack range")
    void outsideAttackRange() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin");
        player.spawnAt(1400, 500, null); // distance 400 units

        assertFalse(lord.isPlayerInAttackRange(player));
    }

    @Test
    @DisplayName("Damage calculation against Arin deals heavy boss damage")
    void combatDamageAgainstPlayer() {
        ShadowLord lord = new ShadowLord(1000, 500);
        Player player = new Player("Arin"); // Player defense = 5

        // Formula: attacker.getAttack() - target.getDefense() / 2 = 24 - 5/2 = 22
        double damage = CombatSystem.calculateDamage(lord, player);
        assertEquals(22.0, damage, 0.001);
    }

    @Test
    @DisplayName("Arin with upgraded sword deals scaled damage to Shadow Lord and defeats him")
    void arinWithUpgradedSwordDamagesAndDefeatsShadowLord() {
        ShadowLord lord = new ShadowLord(1000, 500); // Shadow Lord defense = 8
        Player player = new Player("Arin");
        player.equipSword();
        player.upgradeSword(5); // Attack power = 10 + 5 = 15

        // Upgraded damage: 15 - (8 / 2) = 11 per hit
        double damage = CombatSystem.calculateDamage(player, lord);
        assertEquals(11.0, damage, 0.001);

        // Deal full damage to defeat the boss
        lord.takeDamage(350.0);
        assertFalse(lord.isAlive());
    }
}
