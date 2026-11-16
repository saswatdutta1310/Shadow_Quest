package com.shadowquest.world;

import com.shadowquest.player.Player;

/**
 * Domain representation of a magical portal that transitions the player between realms.
 * Appears as a supernatural tear integrated into an ancient tree or structure upon defeating a realm boss.
 * Domain logic is strictly decoupled from presentation and JavaFX.
 */
public class Portal {
    public static final double DEFAULT_INTERACTION_RADIUS = 60.0;

    private final double x;
    private final double y;
    private final double interactionRadius;
    private boolean active = false;
    private boolean entered = false;

    public Portal(double x, double y) {
        this(x, y, DEFAULT_INTERACTION_RADIUS);
    }

    public Portal(double x, double y, double interactionRadius) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Portal coordinates must be finite.");
        }
        if (!Double.isFinite(interactionRadius) || interactionRadius <= 0) {
            throw new IllegalArgumentException("Interaction radius must be positive and finite.");
        }
        this.x = x;
        this.y = y;
        this.interactionRadius = interactionRadius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getInteractionRadius() {
        return interactionRadius;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    /**
     * Checks if the player is within interaction distance of the portal.
     *
     * @param player active player instance
     * @return true if player is within interaction radius; false otherwise
     */
    public boolean isPlayerInRange(Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        return Math.hypot(playerCenterX - x, playerCenterY - y) <= interactionRadius;
    }

    /**
     * Determines whether the portal can currently be entered by the player.
     *
     * @param player active player instance
     * @return true if portal is active, not already entered, and player is in range; false otherwise
     */
    public boolean canEnter(Player player) {
        return active && !entered && isPlayerInRange(player);
    }
}
