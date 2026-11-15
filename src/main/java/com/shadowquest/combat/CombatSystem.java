package com.shadowquest.combat;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.enemy.DarkArcher;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.enemy.ShadowKnight;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.enemy.ShadowWolf;
import com.shadowquest.enemy.Wraith;
import com.shadowquest.player.Character;
import com.shadowquest.player.Player;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Domain combat manager coordinating player attacks, damage calculations,
 * directional hitbox detection, duplicate-hit prevention, and enemy hit reactions.
 *
 * Free of JavaFX dependencies.
 */
public class CombatSystem {
    private final Set<Enemy> hitEnemiesThisSwing = new HashSet<>();

    /**
     * Clears tracked swing hits and resets transient combat state.
     */
    public void reset() {
        hitEnemiesThisSwing.clear();
    }

    /**
     * Standard formula: Math.max(1, attacker.getAttackPower() - target.getDefense() / 2)
     */
    public static int calculateDamage(Character attacker, Character target) {
        Objects.requireNonNull(attacker, "attacker");
        Objects.requireNonNull(target, "target");
        return Math.max(1, attacker.getAttackPower() - target.getDefense() / 2);
    }

    /**
     * Triggers a sword attack if the player has an equipped sword and is ready to attack.
     * Checks for immediate hits against enemies in the attack hitbox.
     *
     * @param player active player instance
     * @param enemies active level enemies (may be null or empty)
     * @return true if the attack successfully initiated; false otherwise
     */
    public boolean triggerPlayerAttack(Player player, List<Enemy> enemies) {
        if (player == null || !player.canAttack()) {
            return false;
        }
        boolean started = player.attack();
        if (started) {
            hitEnemiesThisSwing.clear();
            processHitboxCollisions(player, enemies);
            return true;
        }
        return false;
    }

    /**
     * Advances combat timers, checks active hitboxes against enemies, and clears swing hits when complete.
     */
    public void update(double deltaTime, Player player, List<Enemy> enemies) {
        if (player != null) {
            player.update(deltaTime);
            if (player.isAttacking()) {
                processHitboxCollisions(player, enemies);
            } else {
                hitEnemiesThisSwing.clear();
            }
        }
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy != null) {
                    enemy.updateHitFlash(deltaTime);
                }
            }
        }
    }

    /**
     * Checks the player's attack hitbox against living, active enemies and applies damage once per swing.
     */
    private void processHitboxCollisions(Player player, List<Enemy> enemies) {
        if (player == null || !player.isAttacking() || enemies == null) {
            return;
        }
        AttackHitbox hitbox = player.getAttackHitbox();
        for (Enemy enemy : enemies) {
            if (enemy == null || !enemy.isAlive() || !enemy.isActive()) {
                continue; // Defeated or inactive enemies (e.g. unawakened Giant Goblin) cannot be hit
            }
            if (hitEnemiesThisSwing.contains(enemy)) {
                continue; // Prevent duplicate hits during the same swing
            }
            if (hitbox.intersects(enemy.getX(), enemy.getY(), enemy.getSize())) {
                boolean wasAlive = enemy.isAlive();
                int damage = calculateDamage(player, enemy);
                enemy.takeDamage(damage);
                enemy.triggerHitFlash();
                hitEnemiesThisSwing.add(enemy);

                if (wasAlive && !enemy.isAlive()) {
                    playEnemyDeathSound(enemy);
                } else if (enemy.isAlive()) {
                    AudioManager.getInstance().playSfx(SoundEffect.SWORD_HIT);
                }
            }
        }
    }

    private void playEnemyDeathSound(Enemy enemy) {
        if (enemy instanceof GiantGoblin) {
            AudioManager.getInstance().playSfx(SoundEffect.GIANT_GOBLIN_DEATH);
        } else if (enemy instanceof Goblin) {
            AudioManager.getInstance().playSfx(SoundEffect.GOBLIN_DEATH);
        } else if (enemy instanceof ShadowWolf) {
            AudioManager.getInstance().playSfx(SoundEffect.WOLF_DEATH);
        } else if (enemy instanceof ShadowKnight) {
            AudioManager.getInstance().playSfx(SoundEffect.SHADOW_KNIGHT_DEATH);
        } else if (enemy instanceof DarkArcher) {
            AudioManager.getInstance().playSfx(SoundEffect.DARK_ARCHER_DEATH);
        } else if (enemy instanceof Wraith) {
            AudioManager.getInstance().playSfx(SoundEffect.WRAITH_DEATH);
        } else if (enemy instanceof ShadowLord) {
            AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_DEATH);
        }
    }

    /**
     * Returns an unmodifiable set of enemies that have already taken damage during the current swing.
     */
    public Set<Enemy> getHitEnemiesThisSwing() {
        return Collections.unmodifiableSet(hitEnemiesThisSwing);
    }
}
