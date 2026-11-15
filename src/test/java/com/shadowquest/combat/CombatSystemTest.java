package com.shadowquest.combat;

import com.shadowquest.core.Game;
import com.shadowquest.core.GameState;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.enemy.ShadowWolf;
import com.shadowquest.items.SwordItem;
import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Combat System, Sword Attacks, Hitboxes & Enemy Defeat")
class CombatSystemTest {
    private CombatSystem combatSystem;
    private Player player;
    private GameWorld world;

    @BeforeEach
    void setUp() {
        combatSystem = new CombatSystem();
        player = new Player("Arin");
        world = new GameWorld(2000, 2000);
        player.spawnAt(200, 200, world);
    }

    @Test
    @DisplayName("Player cannot attack before picking up the sword")
    void cannotAttackBeforeSwordPickup() {
        assertFalse(player.isSwordEquipped());
        assertFalse(player.canAttack());
        assertFalse(player.attack());
        assertFalse(combatSystem.triggerPlayerAttack(player, List.of()));
        assertFalse(player.isAttacking());
    }

    @Test
    @DisplayName("Player can attack after equipping the sword")
    void canAttackAfterSwordPickup() {
        player.equipSword();
        assertTrue(player.isSwordEquipped());
        assertTrue(player.canAttack());

        boolean started = combatSystem.triggerPlayerAttack(player, List.of());
        assertTrue(started);
        assertTrue(player.isAttacking());
        assertEquals(Player.ATTACK_DURATION, player.getAttackTimer(), 1e-4);
        assertEquals(Player.ATTACK_COOLDOWN, player.getAttackCooldownTimer(), 1e-4);
    }

    @Test
    @DisplayName("Attack hitbox correctly projects in front of Arin when facing right")
    void directionalHitboxFacingRight() {
        player.equipSword();
        // Move right to ensure facingRight is true
        player.move(1, 0, 0.1);
        assertTrue(player.isFacingRight());

        AttackHitbox hitbox = player.getAttackHitbox();
        assertEquals(player.getX() + player.getSize(), hitbox.x(), 1e-4);
        assertEquals(player.getY() + (player.getSize() - Player.ATTACK_HITBOX_HEIGHT) / 2.0, hitbox.y(), 1e-4);
        assertEquals(Player.ATTACK_HITBOX_WIDTH, hitbox.width());
        assertEquals(Player.ATTACK_HITBOX_HEIGHT, hitbox.height());
    }

    @Test
    @DisplayName("Attack hitbox correctly projects in front of Arin when facing left")
    void directionalHitboxFacingLeft() {
        player.equipSword();
        // Move left to ensure facingRight is false
        player.move(-1, 0, 0.1);
        assertFalse(player.isFacingRight());

        AttackHitbox hitbox = player.getAttackHitbox();
        assertEquals(player.getX() - Player.ATTACK_HITBOX_WIDTH, hitbox.x(), 1e-4);
        assertEquals(player.getY() + (player.getSize() - Player.ATTACK_HITBOX_HEIGHT) / 2.0, hitbox.y(), 1e-4);
        assertEquals(Player.ATTACK_HITBOX_WIDTH, hitbox.width());
        assertEquals(Player.ATTACK_HITBOX_HEIGHT, hitbox.height());
    }

    @Test
    @DisplayName("Enforces attack duration and cooldown timing via delta time")
    void attackTimingAndCooldown() {
        player.equipSword();
        assertTrue(combatSystem.triggerPlayerAttack(player, List.of()));

        // Cannot attack while already attacking
        assertFalse(combatSystem.triggerPlayerAttack(player, List.of()));

        // Advance by 0.10s (halfway through 0.15s attack)
        combatSystem.update(0.10, player, List.of());
        assertTrue(player.isAttacking());
        assertFalse(player.canAttack());

        // Advance by 0.06s (total 0.16s: attack swing finishes, but cooldown is still running)
        combatSystem.update(0.06, player, List.of());
        assertFalse(player.isAttacking());
        assertFalse(player.canAttack(), "Cannot attack while cooldown (0.35s total) is active");

        // Advance by remaining cooldown (0.35 - 0.16 = 0.19s)
        combatSystem.update(0.20, player, List.of());
        assertEquals(0.0, player.getAttackCooldownTimer());
        assertTrue(player.canAttack(), "Can attack again after cooldown elapses");
    }

    @Test
    @DisplayName("Calculates damage using Math.max(1, attack - defense / 2)")
    void damageFormulaVerification() {
        // Player attack is 10
        Goblin goblin = new Goblin(0, 0); // defense = 2 -> 10 - 2/2 = 9
        assertEquals(9, CombatSystem.calculateDamage(player, goblin));

        ShadowWolf wolf = new ShadowWolf(0, 0); // defense = 1 -> 10 - 1/2 = 10
        assertEquals(10, CombatSystem.calculateDamage(player, wolf));

        GiantGoblin boss = new GiantGoblin(0, 0); // defense = 6 -> 10 - 6/2 = 7
        assertEquals(7, CombatSystem.calculateDamage(player, boss));

        // High defense target yields minimum 1 damage
        Enemy armored = new Goblin(0, 0) {
            @Override
            public int getDefense() {
                return 100;
            }
        };
        assertEquals(1, CombatSystem.calculateDamage(player, armored));
    }

    @Test
    @DisplayName("Hits enemy inside attack hitbox and ignores enemies outside or behind")
    void hitsInsideHitboxOnly() {
        player.equipSword();
        player.spawnAt(200, 200, world);
        // Facing right: hitbox is [224, 272] x [192, 232]

        Goblin inRange = new Goblin(230, 200); // Inside hitbox
        Goblin behind = new Goblin(150, 200);  // Behind Arin
        Goblin farAway = new Goblin(600, 600); // Far away

        List<Enemy> enemies = List.of(inRange, behind, farAway);

        combatSystem.triggerPlayerAttack(player, enemies);

        // inRange was struck: HP reduced from 45 to 36 (took 9 damage)
        assertEquals(36.0, inRange.getHealth());
        assertTrue(inRange.isHitFlashing());

        // behind was not struck: HP remains 45
        assertEquals(45.0, behind.getHealth());
        assertFalse(behind.isHitFlashing());

        // farAway was not struck: HP remains 45
        assertEquals(45.0, farAway.getHealth());
        assertFalse(farAway.isHitFlashing());
    }

    @Test
    @DisplayName("Prevents duplicate hits on the same enemy during a single swing")
    void preventsDuplicateHitsDuringSingleSwing() {
        player.equipSword();
        player.spawnAt(200, 200, world);
        Goblin goblin = new Goblin(230, 200);
        List<Enemy> enemies = List.of(goblin);

        // Initial hit: deals 9 damage
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(36.0, goblin.getHealth());

        // Subsequent update frames during the same 0.15s swing must NOT deal damage again
        combatSystem.update(0.05, player, enemies);
        assertEquals(36.0, goblin.getHealth());
        combatSystem.update(0.05, player, enemies);
        assertEquals(36.0, goblin.getHealth());

        // Advance until swing finishes and cooldown expires
        combatSystem.update(0.30, player, enemies);
        assertFalse(player.isAttacking());
        assertTrue(player.canAttack());

        // A new, separate attack swing CAN hit the enemy again
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(27.0, goblin.getHealth(), "Second swing should deal damage again");
    }

    @Test
    @DisplayName("Enemy takes damage, flashes, dies at zero health, and stops updating")
    void enemyDeathLifecycle() {
        player.equipSword();
        player.spawnAt(200, 200, world);
        ShadowWolf wolf = new ShadowWolf(230, 200); // 35 HP, takes 10 damage per hit
        List<Enemy> enemies = List.of(wolf);

        // Hit 1: 35 -> 25
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(25.0, wolf.getHealth());
        assertTrue(wolf.isAlive());

        // Hit 2: 25 -> 15
        combatSystem.update(0.40, player, enemies);
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(15.0, wolf.getHealth());
        assertTrue(wolf.isAlive());

        // Hit 3: 15 -> 5
        combatSystem.update(0.40, player, enemies);
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(5.0, wolf.getHealth());
        assertTrue(wolf.isAlive());

        // Hit 4: 5 -> 0 (Death!)
        combatSystem.update(0.40, player, enemies);
        combatSystem.triggerPlayerAttack(player, enemies);
        assertEquals(0.0, wolf.getHealth());
        assertFalse(wolf.isAlive(), "Enemy must be marked dead at 0 HP");

        // Defeated enemy cannot move or update
        wolf.update(1.0, player, world);
        assertFalse(wolf.isMoving());
    }

    @Test
    @DisplayName("Giant Goblin cannot be damaged while inactive")
    void inactiveGiantGoblinImmune() {
        player.equipSword();
        player.spawnAt(200, 200, world);
        GiantGoblin boss = new GiantGoblin(230, 200);
        assertFalse(boss.isActive());

        List<Enemy> enemies = List.of(boss);

        combatSystem.triggerPlayerAttack(player, enemies);

        assertEquals(180.0, boss.getHealth(), "Inactive boss must take no damage");
        assertFalse(boss.isHitFlashing());
    }

    @Test
    @DisplayName("Active Giant Goblin takes damage from player attacks")
    void activeGiantGoblinTakesDamage() {
        player.equipSword();
        player.spawnAt(200, 200, world);
        GiantGoblin boss = new GiantGoblin(230, 200);
        boss.setActive(true);
        assertTrue(boss.isActive());

        List<Enemy> enemies = List.of(boss);

        combatSystem.triggerPlayerAttack(player, enemies);

        // 180 HP - 7 damage = 173 HP
        assertEquals(173.0, boss.getHealth());
        assertTrue(boss.isHitFlashing());
    }

    @Test
    @DisplayName("Complete flow: Defeating all 3 normal Goblins awakens Giant Goblin, ShadowWolves do not")
    void completeGoblinDefeatBossAwakeningFlow() {
        Game game = new Game();
        game.startNewGame();
        assertEquals(GameState.PLAYING, game.getState());

        Player p = game.getPlayer();
        SwordItem sword = game.getSwordItem();
        assertNotNull(sword);

        // Walk to sword to pick it up
        p.spawnAt(sword.getX(), sword.getY(), game.getWorld());
        game.update(0.01);
        assertTrue(p.isSwordEquipped());

        List<Enemy> enemies = game.getEnemies();
        GiantGoblin boss = enemies.stream()
                .filter(e -> e instanceof GiantGoblin)
                .map(e -> (GiantGoblin) e)
                .findFirst()
                .orElseThrow();
        assertFalse(boss.isActive());

        List<Goblin> normalGoblins = enemies.stream()
                .filter(e -> e instanceof Goblin && !(e instanceof GiantGoblin))
                .map(e -> (Goblin) e)
                .toList();
        assertEquals(3, normalGoblins.size());

        List<ShadowWolf> wolves = enemies.stream()
                .filter(e -> e instanceof ShadowWolf)
                .map(e -> (ShadowWolf) e)
                .toList();

        // 1. Defeat a ShadowWolf: boss must NOT awaken
        ShadowWolf wolf = wolves.get(0);
        wolf.takeDamage(100);
        assertFalse(wolf.isAlive());
        game.update(0.1);
        assertFalse(boss.isActive(), "ShadowWolf death must not awaken boss");

        // 2. Defeat normal Goblin 1: boss still inactive
        Goblin g1 = normalGoblins.get(0);
        g1.takeDamage(100);
        assertFalse(g1.isAlive());
        game.update(0.1);
        assertFalse(boss.isActive());

        // 3. Defeat normal Goblin 2: boss still inactive
        Goblin g2 = normalGoblins.get(1);
        g2.takeDamage(100);
        assertFalse(g2.isAlive());
        game.update(0.1);
        assertFalse(boss.isActive());

        // 4. Defeat normal Goblin 3 (all 3 normal Goblins now defeated!): boss awakens!
        Goblin g3 = normalGoblins.get(2);
        g3.takeDamage(100);
        assertFalse(g3.isAlive());
        game.update(0.1);
        assertTrue(boss.isActive(), "Giant Goblin boss must awaken when all 3 normal Goblins are defeated");
    }
}
