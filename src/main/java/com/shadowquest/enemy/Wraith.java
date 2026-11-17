package com.shadowquest.enemy;

/**
 * Concrete Wraith enemy for Level 2 (Shadow Castle).
 *
 * A floating spectral undead apparition wrapped in ragged, semi-translucent
 * ethereal shrouds with an eerie pale green and violet aura.
 * Glides rapidly and silently towards Arin and strikes with a chilling spectral siphon.
 *
 * Domain logic is strictly decoupled from presentation and JavaFX.
 */
public class Wraith extends Enemy {
    public static final double DEFAULT_HEALTH = 60.0;
    public static final int DEFAULT_ATTACK = 13;
    public static final int DEFAULT_DEFENSE = 1;
    public static final double DEFAULT_SPEED = 125.0;
    public static final double DEFAULT_DETECTION_RADIUS = 240.0;
    public static final double DEFAULT_SIZE = 28.0;
    public static final double DEFAULT_STOP_DISTANCE = 32.0;
    public static final int DEFAULT_REWARD_COINS = 20;
    public static final double DEFAULT_PATROL_RADIUS = 50.0;

    public static final double DEFAULT_ATTACK_RANGE = 48.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.25;
    public static final double DEFAULT_ATTACK_DURATION = 0.45;
    public static final double DEFAULT_ATTACK_COOLDOWN = 1.20;

    public Wraith(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public Wraith(double startX, double startY, double patrolRadius) {
        super("Wraith", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }

    public Wraith(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }
}
