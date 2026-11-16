package com.shadowquest.items;

import com.shadowquest.player.Player;

/**
 * Domain representation of a health potion in the game world.
 *
 * Restores player health when approached by a wounded player.
 * Preserves the potion on the ground if the player is already at maximum health.
 * Domain logic is strictly independent of presentation and JavaFX.
 */
public class HealthPotion {
    public static final double DEFAULT_PICKUP_RADIUS = 32.0;
    public static final double DEFAULT_HEAL_AMOUNT = 35.0;

    private final double x;
    private final double y;
    private final double healAmount;
    private final double pickupRadius;
    private boolean pickedUp = false;

    public HealthPotion(double x, double y) {
        this(x, y, DEFAULT_HEAL_AMOUNT, DEFAULT_PICKUP_RADIUS);
    }

    public HealthPotion(double x, double y, double healAmount) {
        this(x, y, healAmount, DEFAULT_PICKUP_RADIUS);
    }

    public HealthPotion(double x, double y, double healAmount, double pickupRadius) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Potion coordinates must be finite.");
        }
        if (!Double.isFinite(healAmount) || healAmount <= 0) {
            throw new IllegalArgumentException("Heal amount must be finite and positive.");
        }
        if (!Double.isFinite(pickupRadius) || pickupRadius <= 0) {
            throw new IllegalArgumentException("Pickup radius must be finite and positive.");
        }
        this.x = x;
        this.y = y;
        this.healAmount = healAmount;
        this.pickupRadius = pickupRadius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getHealAmount() {
        return healAmount;
    }

    public double getPickupRadius() {
        return pickupRadius;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public boolean isConsumed() {
        return pickedUp;
    }

    /**
     * Checks if a wounded player is within pickup radius and consumes the potion to restore health.
     * Does not consume if the player is at maximum health.
     *
     * @param player active player instance
     * @return true if the potion was consumed; false otherwise
     */
    public boolean checkPickup(Player player) {
        if (pickedUp || player == null || !player.isAlive()) {
            return false;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return false;
        }
        double centerX = x + 10.0;
        double centerY = y + 10.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;

        if (Math.hypot(playerCenterX - centerX, playerCenterY - centerY) <= pickupRadius) {
            pickedUp = true;
            player.heal(healAmount);
            return true;
        }
        return false;
    }
}
