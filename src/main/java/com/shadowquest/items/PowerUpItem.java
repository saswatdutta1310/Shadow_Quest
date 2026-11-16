package com.shadowquest.items;

import com.shadowquest.player.Player;

/**
 * Domain representation of a legendary power-up dropped upon defeating the level boss.
 *
 * Grants a permanent attack damage increase for subsequent combat encounters and the next level.
 * Domain logic is strictly independent of presentation and JavaFX.
 */
public class PowerUpItem {
    public static final double DEFAULT_PICKUP_RADIUS = 40.0;
    public static final int DEFAULT_ATTACK_BONUS = 8;

    private final String name;
    private final double x;
    private final double y;
    private final int attackBonus;
    private final double pickupRadius;
    private boolean pickedUp = false;

    public PowerUpItem(double x, double y) {
        this("Titan Strength Crest", x, y, DEFAULT_ATTACK_BONUS, DEFAULT_PICKUP_RADIUS);
    }

    public PowerUpItem(double x, double y, int attackBonus) {
        this("Titan Strength Crest", x, y, attackBonus, DEFAULT_PICKUP_RADIUS);
    }

    public PowerUpItem(double x, double y, int attackBonus, double pickupRadius) {
        this("Titan Strength Crest", x, y, attackBonus, pickupRadius);
    }

    public PowerUpItem(String name, double x, double y, int attackBonus, double pickupRadius) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Power-up name must not be null or blank.");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Power-up coordinates must be finite.");
        }
        if (attackBonus <= 0) {
            throw new IllegalArgumentException("Attack bonus must be positive.");
        }
        if (!Double.isFinite(pickupRadius) || pickupRadius <= 0) {
            throw new IllegalArgumentException("Pickup radius must be finite and positive.");
        }
        this.name = name.strip();
        this.x = x;
        this.y = y;
        this.attackBonus = attackBonus;
        this.pickupRadius = pickupRadius;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public double getPickupRadius() {
        return pickupRadius;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public boolean isCollected() {
        return pickedUp;
    }

    /**
     * Checks if the player approaches the dropped power-up and absorbs its strength bonus.
     *
     * @param player active player instance
     * @return true if the power-up was just collected; false otherwise
     */
    public boolean checkPickup(Player player) {
        if (pickedUp || player == null || !player.isAlive()) {
            return false;
        }
        double centerX = x + 16.0;
        double centerY = y + 16.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;

        if (Math.hypot(playerCenterX - centerX, playerCenterY - centerY) <= pickupRadius) {
            pickedUp = true;
            player.addAttackBonus(attackBonus);
            return true;
        }
        return false;
    }
}
