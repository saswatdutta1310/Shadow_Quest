package com.shadowquest.items;

import com.shadowquest.player.Player;

/**
 * Domain representation of a sword dropped in the game world.
 *
 * Implements proximity-based automatic pickup. When Arin approaches within
 * pickup radius, the sword is equipped once and disappears from the ground.
 * Domain logic is strictly independent of presentation and JavaFX.
 */
public class SwordItem {
    public static final double DEFAULT_PICKUP_RADIUS = 36.0;

    private final double x;
    private final double y;
    private final double pickupRadius;
    private boolean pickedUp = false;

    public SwordItem(double x, double y) {
        this(x, y, DEFAULT_PICKUP_RADIUS);
    }

    public SwordItem(double x, double y, double pickupRadius) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Sword coordinates must be finite.");
        }
        if (!Double.isFinite(pickupRadius) || pickupRadius <= 0) {
            throw new IllegalArgumentException("Pickup radius must be finite and positive.");
        }
        this.x = x;
        this.y = y;
        this.pickupRadius = pickupRadius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPickupRadius() {
        return pickupRadius;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    /**
     * Attempts automatic pickup by the player.
     * Succeeds only once when the player is alive and within pickup radius.
     *
     * @param player active player instance
     * @return true if the sword was just picked up; false otherwise
     */
    public boolean checkPickup(Player player) {
        if (pickedUp || player == null || !player.isAlive()) {
            return false;
        }
        double centerX = x + 12.0;
        double centerY = y + 12.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;

        if (Math.hypot(playerCenterX - centerX, playerCenterY - centerY) <= pickupRadius) {
            pickedUp = true;
            player.equipSword();
            return true;
        }
        return false;
    }
}
