package com.shadowquest.enemy;

import java.util.List;

/**
 * Concrete GiantGoblin Level 1 Boss.
 * Towering chieftain of the woodland goblins, armed with a spiked battle-club
 * and heavy iron-plated armor.
 *
 * Stats:
 * - Health: 180 (4x normal Goblin)
 * - Attack: 18 (heavy concussive blows)
 * - Defense: 6 (plated leather & iron)
 * - Speed: 85.0 world units/sec (slower, heavy lumbering movement)
 * - Detection Radius: 240.0 units
 * - Body Size: 48.0 units (double the physical hitbox of a normal Goblin)
 * - Stop Distance: 45.0 units
 * - Reward Coins: 30
 * - Default Patrol Radius: 40.0 units
 *
 * Boss Activation:
 * - Inactive by default upon creation.
 * - Awakens and enters the world only after all 3 normal woodland Goblins have been defeated.
 */
public class GiantGoblin extends Enemy {
    public static final double DEFAULT_HEALTH = 180.0;
    public static final int DEFAULT_ATTACK = 18;
    public static final int DEFAULT_DEFENSE = 6;
    public static final double DEFAULT_SPEED = 85.0;
    public static final double DEFAULT_DETECTION_RADIUS = 240.0;
    public static final double DEFAULT_SIZE = 48.0;
    public static final double DEFAULT_STOP_DISTANCE = 45.0;
    public static final int DEFAULT_REWARD_COINS = 30;
    public static final double DEFAULT_PATROL_RADIUS = 40.0;
    public static final double DEFAULT_ATTACK_RANGE = 55.0;
    public static final double DEFAULT_ATTACK_WINDUP = 0.45;
    public static final double DEFAULT_ATTACK_DURATION = 0.70;
    public static final double DEFAULT_ATTACK_COOLDOWN = 1.80;

    public GiantGoblin(double startX, double startY) {
        this(startX, startY, DEFAULT_PATROL_RADIUS);
    }

    public GiantGoblin(double startX, double startY, double patrolRadius) {
        super("Giant Goblin", DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
        setActive(false);
    }

    public GiantGoblin(String name, double startX, double startY, double patrolRadius) {
        super(name, DEFAULT_HEALTH, DEFAULT_ATTACK, DEFAULT_DEFENSE,
                DEFAULT_SPEED, DEFAULT_DETECTION_RADIUS, DEFAULT_SIZE,
                DEFAULT_STOP_DISTANCE, DEFAULT_REWARD_COINS,
                startX, startY, patrolRadius,
                DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WINDUP, DEFAULT_ATTACK_DURATION, DEFAULT_ATTACK_COOLDOWN);
        setActive(false);
    }

    /**
     * Evaluates whether the prerequisite conditions for boss awakening have been met.
     * The boss awakens when all normal Goblins in the given enemy collection are defeated (!isAlive).
     *
     * @param enemies collection of enemies in the level
     * @return true if all normal Goblins are defeated and at least one was evaluated; false otherwise
     */
    public boolean checkActivationConditions(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return false;
        }
        int normalGoblinCount = 0;
        for (Enemy enemy : enemies) {
            if (enemy instanceof Goblin && !(enemy instanceof GiantGoblin)) {
                normalGoblinCount++;
                if (enemy.isAlive()) {
                    return false;
                }
            }
        }
        return normalGoblinCount > 0;
    }
}
