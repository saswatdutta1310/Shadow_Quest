package com.shadowquest.enemy;

/**
 * Concrete Shadow Knight enemy for Level 2 (Shadow Castle).
 *
 * A dark armored undead warrior clad in forged dark-steel plate armor
 * with subtle violet rune engravings, wielding a heavy two-handed broadsword.
 * Moves relentlessly towards the player and executes close-range melee cleaves.
 *
 * Domain logic is strictly decoupled from presentation and JavaFX.
 */
public class ShadowKnight extends Enemy {
    public static final double DEFAULT_HEALTH = 75.0;
    public static final int DEFAULT_ATTACK = 14;
    public static final int DEFAULT_DEFENSE = 4;
    public static final double DEFAULT_SPEED = 95.0;
    public static final double DEFAULT_DETECTION_RADIUS = 220.0;
    public static final double DEFAULT_SIZE = 32.0;
    public static final double DEFAULT_STOP_DISTANCE = 34.0;
    public static final int DEFAULT_REWARD_COINS = 15;
    public static final double DEFAULT_PATROL_RADIUS = 40.0;

    public static final double DEFAULT_ATTACK_RANGE = 42.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.30;
    public static final double DEFAULT_ATTACK_DURATION = 0.50;
    public static final double DEFAULT_ATTACK_COOLDOWN = 1.40;

    public ShadowKnight(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public ShadowKnight(double startX, double startY, double patrolRadius) {
        super("Shadow Knight", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }

    public ShadowKnight(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
    }
}
