package com.shadowquest.enemy;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.combat.CombatSystem;
import com.shadowquest.player.Player;
import com.shadowquest.world.CollisionManager;
import com.shadowquest.world.GameWorld;

/**
 * Concrete Shadow Lord Final Boss for Level 2 (Shadow Castle).
 *
 * An ancient, terrifying hooded dark sorcerer/reaper of the void:
 * - Tall, intimidating silhouette draped in deep black/purple ragged robes.
 * - Two large curved demonic horns curling from the hood.
 * - Glowing orange eyes burning inside a pitch-black hood void.
 * - Two distinct attack styles:
 *   1. Close-range sweeping Void Scythe strike (MELEE, range 65.0)
 *   2. Devastating continuous Flamethrower cone attack (RANGED / FLAMETHROWER, range 280.0):
 *      - Distinct charging/windup telegraph (0.55s) with hands raised and gathering flame.
 *      - Continuous stream of roaring hellfire (1.20s duration) along aimed angle.
 *      - Controlled single-damage application on hit frame (no per-frame damage).
 *      - Can be dodged during windup.
 *      - Blocked by solid obstacles / fortress walls.
 *      - Strategic 3.50s cooldown.
 *
 * Domain logic is strictly independent of presentation and JavaFX.
 */
public class ShadowLord extends Enemy {
    public static final double DEFAULT_HEALTH = 350.0;
    public static final int DEFAULT_ATTACK = 24;
    public static final int DEFAULT_DEFENSE = 8;
    public static final double DEFAULT_SPEED = 70.0;
    public static final double DEFAULT_DETECTION_RADIUS = 340.0;
    public static final double DEFAULT_SIZE = 64.0;
    public static final double DEFAULT_STOP_DISTANCE = 50.0;
    public static final int DEFAULT_REWARD_COINS = 100;
    public static final double DEFAULT_PATROL_RADIUS = 50.0;

    public static final double MELEE_ATTACK_RANGE = 65.0;
    public static final double MELEE_WINDUP = 0.35;
    public static final double MELEE_DURATION = 0.65;
    public static final double MELEE_COOLDOWN = 1.60;

    public static final double FLAMETHROWER_RANGE = 280.0;
    public static final double RANGED_ATTACK_RANGE = 280.0;
    public static final double FLAMETHROWER_WINDUP = 0.55;
    public static final double FLAMETHROWER_DURATION = 1.20;
    public static final double FLAMETHROWER_COOLDOWN = 3.50;
    public static final double FLAMETHROWER_CONE_HALF_ANGLE = Math.toRadians(35.0);

    public enum AttackMode {
        MELEE,
        RANGED,
        FLAMETHROWER
    }

    private AttackMode currentAttackMode = AttackMode.MELEE;
    private double flamethrowerAngle = 0.0;

    public ShadowLord(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public ShadowLord(double startX, double startY, double patrolRadius) {
        super("Shadow Lord", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                FLAMETHROWER_RANGE, FLAMETHROWER_WINDUP, FLAMETHROWER_DURATION, FLAMETHROWER_COOLDOWN);
        setActive(true);
    }

    public ShadowLord(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                FLAMETHROWER_RANGE, FLAMETHROWER_WINDUP, FLAMETHROWER_DURATION, FLAMETHROWER_COOLDOWN);
        setActive(true);
    }

    public AttackMode getCurrentAttackMode() {
        return currentAttackMode;
    }

    public void setAttackMode(AttackMode mode) {
        if (mode != null) {
            this.currentAttackMode = mode;
        }
    }

    public boolean isFlamethrowerMode() {
        return currentAttackMode == AttackMode.RANGED || currentAttackMode == AttackMode.FLAMETHROWER;
    }

    public double getFlamethrowerAngle() {
        return flamethrowerAngle;
    }

    public boolean isFlamethrowerCharging() {
        return isAttacking() && isFlamethrowerMode() && getAttackTimer() < getAttackWindup();
    }

    public boolean isFlamethrowerFiring() {
        return isAttacking() && isFlamethrowerMode() && getAttackTimer() >= getAttackWindup();
    }

    @Override
    public double getAttackWindup() {
        return isFlamethrowerMode() ? FLAMETHROWER_WINDUP : MELEE_WINDUP;
    }

    @Override
    public double getAttackDuration() {
        return isFlamethrowerMode() ? FLAMETHROWER_DURATION : MELEE_DURATION;
    }

    @Override
    public double getAttackCooldown() {
        return isFlamethrowerMode() ? FLAMETHROWER_COOLDOWN : MELEE_COOLDOWN;
    }

    @Override
    public boolean isPlayerInAttackRange(Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        double centerX = getX() + getSize() / 2.0;
        double centerY = getY() + getSize() / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        double distance = Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        if (distance <= MELEE_ATTACK_RANGE) {
            currentAttackMode = AttackMode.MELEE;
            return true;
        } else if (distance <= FLAMETHROWER_RANGE) {
            currentAttackMode = AttackMode.RANGED;
            return true;
        }
        return false;
    }

    @Override
    protected void onAttackStart(Player player) {
        if (player == null) return;
        double centerX = getX() + getSize() / 2.0;
        double centerY = getY() + getSize() / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        double distance = Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        if (distance <= MELEE_ATTACK_RANGE) {
            currentAttackMode = AttackMode.MELEE;
            AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_MELEE);
        } else {
            currentAttackMode = AttackMode.RANGED;
            flamethrowerAngle = Math.atan2(playerCenterY - centerY, playerCenterX - centerX);
            AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_FIRE);
        }
    }

    @Override
    protected void onAttackTick(double deltaTime, Player player, GameWorld world) {
        // During flamethrower telegraph/windup, track the player so Arin sees the boss aiming
        if (isFlamethrowerMode() && getAttackTimer() < getAttackWindup() && player != null) {
            double centerX = getX() + getSize() / 2.0;
            double centerY = getY() + getSize() / 2.0;
            double playerCenterX = player.getX() + player.getSize() / 2.0;
            double playerCenterY = player.getY() + player.getSize() / 2.0;
            flamethrowerAngle = Math.atan2(playerCenterY - centerY, playerCenterX - centerX);
            setFacingRight(playerCenterX >= centerX);
        }
    }

    @Override
    protected void applyAttackDamage(Player player, GameWorld world) {
        if (player == null || !player.isAlive()) return;

        double centerX = getX() + getSize() / 2.0;
        double centerY = getY() + getSize() / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        double distance = Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        if (currentAttackMode == AttackMode.MELEE) {
            if (distance <= MELEE_ATTACK_RANGE) {
                int damage = CombatSystem.calculateDamage(this, player);
                player.takeCombatDamage(damage);
            }
        } else if (isFlamethrowerMode()) {
            // Cone hit detection: within range, within cone half-angle, and line of sight not blocked by walls
            if (distance <= FLAMETHROWER_RANGE) {
                double angleToPlayer = Math.atan2(playerCenterY - centerY, playerCenterX - centerX);
                double angleDiff = Math.abs(normalizeAngle(angleToPlayer - flamethrowerAngle));
                if (angleDiff <= FLAMETHROWER_CONE_HALF_ANGLE) {
                    if (CollisionManager.hasLineOfSight(world, centerX, centerY, playerCenterX, playerCenterY)) {
                        int damage = CombatSystem.calculateDamage(this, player);
                        player.takeCombatDamage(damage);
                        AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_FIRE_HIT);
                    }
                }
            }
        }
    }

    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
