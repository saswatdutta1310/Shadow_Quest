package com.shadowquest.enemy;

/**
 * Concrete Goblin enemy.
 * Quick, cunning woodland creature with moderate health and sharp senses.
 *
 * Stats:
 * - Health: 45
 * - Attack: 8
 * - Defense: 2
 * - Speed: 110 world units/sec
 * - Detection Radius: 180 units
 * - Body Size: 24 units
 * - Stop Distance: 28 units
 * - Reward Coins: 5
 * - Default Patrol Radius: 50 units
 */
public class Goblin extends Enemy {
    public static final double DEFAULT_HEALTH = 45;
    public static final int DEFAULT_ATTACK = 8;
    public static final int DEFAULT_DEFENSE = 2;
    public static final double DEFAULT_SPEED = 110.0;
    public static final double DEFAULT_DETECTION_RADIUS = 180.0;
    public static final double DEFAULT_SIZE = 24.0;
    public static final double DEFAULT_STOP_DISTANCE = 28.0;
    public static final int DEFAULT_REWARD_COINS = 5;
    public static final double DEFAULT_PATROL_RADIUS = 50.0;
    public static final double DEFAULT_ATTACK_RANGE = 34.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.25;
    public static final double DEFAULT_ATTACK_DURATION = 0.40;
    public static final double DEFAULT_ATTACK_COOLDOWN = 1.20;

    public Goblin(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public Goblin(double startX, double startY, double patrolRadius) {
        super("Goblin", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }

    public Goblin(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }
}
