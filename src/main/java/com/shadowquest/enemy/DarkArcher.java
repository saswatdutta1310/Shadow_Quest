package com.shadowquest.enemy;

import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;

/**
 * Concrete Dark Archer enemy for Level 2 (Shadow Castle).
 *
 * A hooded dark marksman who attacks Arin from a distance using shadow arrows.
 * Rather than rushing blindly into melee, the Dark Archer maintains tactical
 * distance by kiting backwards when Arin draws too near (< 110 units) and
 * lining up ranged shadow arrow shots within 230 units.
 *
 * Domain logic is strictly decoupled from presentation and JavaFX.
 */
public class DarkArcher extends Enemy {
    public static final double DEFAULT_HEALTH = 50.0;
    public static final int DEFAULT_ATTACK = 11;
    public static final int DEFAULT_DEFENSE = 2;
    public static final double DEFAULT_SPEED = 105.0;
    public static final double DEFAULT_DETECTION_RADIUS = 280.0;
    public static final double DEFAULT_SIZE = 26.0;
    public static final double DEFAULT_STOP_DISTANCE = 130.0;
    public static final int DEFAULT_REWARD_COINS = 18;
    public static final double DEFAULT_PATROL_RADIUS = 45.0;

    public static final double DEFAULT_ATTACK_RANGE = 230.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.35;
    public static final double DEFAULT_ATTACK_DURATION = 0.60;
    public static final double DEFAULT_ATTACK_COOLDOWN = 1.80;

    public static final double KITE_RETREAT_DISTANCE = 110.0;

    public DarkArcher(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public DarkArcher(double startX, double startY, double patrolRadius) {
        super("Dark Archer", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }

    public DarkArcher(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
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
        double dist = Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        // Dark Archer attacks strictly from a tactical distance (110 - 230 units), not point-blank
        return dist >= KITE_RETREAT_DISTANCE && dist <= getAttackRange();
    }

    @Override
    protected boolean shouldStandGround(Player player) {
        if (player == null || !player.isAlive()) return false;
        double centerX = getX() + getSize() / 2.0;
        double centerY = getY() + getSize() / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        double dist = Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        // If player is too close (< KITE_RETREAT_DISTANCE), do NOT stand ground: kite away!
        return dist >= KITE_RETREAT_DISTANCE && isPlayerInAttackRange(player);
    }

    @Override
    protected void chasePlayer(double deltaTime, Player player, GameWorld world) {
        double centerX = getX() + getSize() / 2.0;
        double centerY = getY() + getSize() / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;

        double dx = playerCenterX - centerX;
        double dy = playerCenterY - centerY;
        double dist = Math.hypot(dx, dy);

        if (dist > 0.001) {
            setFacingRight(dx >= 0);
        }

        // Kiting logic: if Arin gets too close (< KITE_RETREAT_DISTANCE), retreat away!
        if (dist < KITE_RETREAT_DISTANCE && dist > 0.001) {
            double retreatX = -dx / dist;
            double retreatY = -dy / dist;
            double moveDist = getSpeed() * deltaTime;
            setMoving(true);
            moveWithCollision(retreatX * moveDist, retreatY * moveDist, world);
        } else if (dist > getStopDistance() + 10.0) {
            // Player is outside comfortable archer range: advance into firing position
            super.chasePlayer(deltaTime, player, world);
        } else {
            // In sweet spot (between KITE_RETREAT_DISTANCE and stopDistance): stand ground and aim
            setMoving(false);
        }
    }
}
