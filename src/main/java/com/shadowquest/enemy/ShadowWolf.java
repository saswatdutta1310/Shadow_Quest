package com.shadowquest.enemy;

/**
 * Concrete ShadowWolf enemy.
 * Swift, predatory beast veiled in shadow essence with high speed and large detection range.
 *
 * Stats:
 * - Health: 35
 * - Attack: 10
 * - Defense: 1
 * - Speed: 160 world units/sec
 * - Detection Radius: 220 units
 * - Body Size: 28 units
 * - Stop Distance: 32 units
 * - Reward Coins: 8
 * - Default Patrol Radius: 70 units
 */
public class ShadowWolf extends Enemy {
    public static final double DEFAULT_HEALTH = 35;
    public static final int DEFAULT_ATTACK = 10;
    public static final int DEFAULT_DEFENSE = 1;
    public static final double DEFAULT_SPEED = 160.0;
    public static final double DEFAULT_DETECTION_RADIUS = 220.0;
    public static final double DEFAULT_SIZE = 28.0;
    public static final double DEFAULT_STOP_DISTANCE = 32.0;
    public static final int DEFAULT_REWARD_COINS = 8;
    public static final double DEFAULT_PATROL_RADIUS = 70.0;
    public static final double DEFAULT_ATTACK_RANGE = 38.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.18;
    public static final double DEFAULT_ATTACK_DURATION = 0.30;
    public static final double DEFAULT_ATTACK_COOLDOWN = 0.90;

    public ShadowWolf(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public ShadowWolf(double startX, double startY, double patrolRadius) {
        super("ShadowWolf", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }

    public ShadowWolf(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }
}
